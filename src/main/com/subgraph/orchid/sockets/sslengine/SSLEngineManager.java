package com.subgraph.orchid.sockets.sslengine;

import com.subgraph.orchid.logging.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

public class SSLEngineManager {
    private static final Logger logger = Logger.getInstance(SSLEngineManager.class);
	
    private final SSLEngine engine;
    private final InputStream input;
    private final OutputStream output;

    private final ByteBuffer peerApplicationBuffer;
    private final ByteBuffer peerNetworkBuffer;
    private final ByteBuffer myApplicationBuffer;
    private final ByteBuffer myNetworkBuffer;

    private final HandshakeCallbackHandler handshakeCallback;

    private boolean handshakeStarted = false;


    SSLEngineManager(SSLEngine engine, HandshakeCallbackHandler handshakeCallback, InputStream input, OutputStream output) {
        this.engine = engine;
        this.handshakeCallback = handshakeCallback;
        this.input = input;
        this.output = output;
        final SSLSession session = engine.getSession();
        this.peerApplicationBuffer = createApplicationBuffer(session);
        this.peerNetworkBuffer = createPacketBuffer(session);
        this.myApplicationBuffer = createApplicationBuffer(session);
        this.myNetworkBuffer = createPacketBuffer(session);
    }
	
    private static ByteBuffer createApplicationBuffer(SSLSession session) {
        return createBuffer(session.getApplicationBufferSize());
    }
	
    private static ByteBuffer createPacketBuffer(SSLSession session) {
        return createBuffer(session.getPacketBufferSize());
    }
	
    private static ByteBuffer createBuffer(int sz) {
        final byte[] array = new byte[sz];
        return ByteBuffer.wrap(array);
    }

    void startHandshake() throws IOException {
        logger.debug("startHandshake()");
        handshakeStarted = true;
        engine.beginHandshake();
        runHandshake();
    }

    ByteBuffer getSendBuffer() {
        return myApplicationBuffer;
    }

    ByteBuffer getRecvBuffer() {
        return peerApplicationBuffer;
    }


    void write() throws IOException {
        logger.debug("write()");
        if(!handshakeStarted) {
            startHandshake();
        }
        final int p = myApplicationBuffer.position();
        if(p == 0) {
            return;
        }
        myNetworkBuffer.clear();
        myApplicationBuffer.flip();
        final SSLEngineResult result = engine.wrap(myApplicationBuffer, myNetworkBuffer);
        myApplicationBuffer.compact();
        logResult(result);

        switch(result.getStatus()) {
        case BUFFER_OVERFLOW:
            throw new BufferOverflowException();
        case BUFFER_UNDERFLOW:
            throw new BufferUnderflowException();
        case CLOSED:
            throw new SSLException("SSLEngine is closed");

            case OK:
            default:
            break;
        }

        flush();
        if(runHandshake()) {
            write();
        }

    }

    // either return -1 or peerApplicationBuffer has data to read
    int read() throws IOException {
        logger.debug("read()");
        if(!handshakeStarted) {
            startHandshake();
        }

        if(engine.isInboundDone()) {
            return -1;
        }

        final int n = networkReadBuffer(peerNetworkBuffer);
        if(n == -1) {
            return -1;
        }
        final int p = peerApplicationBuffer.position();

        peerNetworkBuffer.flip();
        final SSLEngineResult result = engine.unwrap(peerNetworkBuffer, peerApplicationBuffer);
        peerNetworkBuffer.compact();
        logResult(result);

        switch(result.getStatus()) {
        case BUFFER_OVERFLOW:
            throw new BufferOverflowException();

        case BUFFER_UNDERFLOW:
            return 0; // <-- illegal return according to invariant

        case CLOSED:
            input.close();
            break;
            case OK:
            default:
            break;
        }

        runHandshake();

        if(engine.isInboundDone()) {
            return -1;
        }
        return peerApplicationBuffer.position() - p;
    }

    void close() throws IOException {
        try {
            flush();
            if(!engine.isOutboundDone()) {
                engine.closeOutbound();
                runHandshake();
            } else if(!engine.isInboundDone()) {
                engine.closeInbound();
                runHandshake();
            }
        } finally {
            output.close();
        }
    }

    void flush() throws IOException {
        myNetworkBuffer.flip();
        networkWriteBuffer(myNetworkBuffer);
        myNetworkBuffer.compact();
    }


    private boolean runHandshake() throws IOException {
        boolean handshakeRan = false;
        while(true) {
            if(!processHandshake()) {
                return handshakeRan;
            } else {
                handshakeRan = true;
            }
        }
    }

    private boolean processHandshake() throws IOException {
        final HandshakeStatus hs = engine.getHandshakeStatus();
        logger.debug("processHandshake() hs = "+ hs);
        switch(hs) {
        case NEED_TASK:
            synchronousRunDelegatedTasks();
            return processHandshake();

        case NEED_UNWRAP:
            return handshakeUnwrap();

        case NEED_WRAP:
            return handshakeWrap();

        default:
            return false;
        }
    }

    private void synchronousRunDelegatedTasks() {
        logger.debug("runDelegatedTasks()");
        while(true) {
            Runnable r = engine.getDelegatedTask();
            if(r == null) {
                return;
            }
            logger.debug("Running a task: "+ r);
            r.run();
        }
    }

    private boolean handshakeUnwrap() throws IOException {
            logger.debug("handshakeUnwrap()");

            if(!engine.isInboundDone() && peerNetworkBuffer.position() == 0) {
                    if(networkReadBuffer(peerNetworkBuffer) < 0) {
                            return false;
                    }
            }
            peerNetworkBuffer.flip();
            final SSLEngineResult result = engine.unwrap(peerNetworkBuffer, peerApplicationBuffer);
            peerNetworkBuffer.compact();
            logResult(result);

            if(result.getHandshakeStatus() == HandshakeStatus.FINISHED) {
                    handshakeFinished();
            }
            switch(result.getStatus()) {

            case CLOSED:
                    if(engine.isOutboundDone()) {
                            output.close();
                    }
                    return false;
            case OK:
                    return true;
            case BUFFER_UNDERFLOW:
                return networkReadBuffer(peerNetworkBuffer) >= 0;
                default:
                    return false;
            }
    }

    private boolean handshakeWrap() throws IOException {
        logger.debug("handshakeWrap()");
        myApplicationBuffer.flip();
        final SSLEngineResult result = engine.wrap(myApplicationBuffer, myNetworkBuffer);
        myApplicationBuffer.compact();
        logResult(result);

        if(result.getHandshakeStatus() == HandshakeStatus.FINISHED) {
            handshakeFinished();
        }

        if(result.getStatus() == Status.CLOSED) {
            try {
                flush();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        } else {
            flush();
        }

        switch(result.getStatus()) {
        case CLOSED:
            if(engine.isOutboundDone()) {
                output.close();
            }
            return false;

        case OK:
            return true;

        default:
            return false;

        }
    }

    private void logResult(SSLEngineResult result) {
        logger.debug("Result status="+result.getStatus() + " hss="+ result.getHandshakeStatus() + " consumed = "+ result.bytesConsumed() + " produced = "+ result.bytesProduced());
    }

    private void handshakeFinished() {
        if(handshakeCallback != null) {
            handshakeCallback.handshakeCompleted();
        }
    }

    private void networkWriteBuffer(ByteBuffer buffer) throws IOException {
        final byte[] bs = buffer.array();
        final int off = buffer.position();
        final int len = buffer.limit() - off;
        logger.debug("networkWriteBuffer(b, "+ off + ", "+ len +")");
        output.write(bs, off, len);
        output.flush();
        buffer.position(buffer.limit());
    }

    private int networkReadBuffer(ByteBuffer buffer) throws IOException {
        final byte[] bs = buffer.array();
        final int off = buffer.position();
        final int len = buffer.limit() - off;

        final int n = input.read(bs, off, len);
        if(n != -1) {
            buffer.position(off + n);
        }
        logger.debug("networkReadBuffer(b, "+ off +", "+ len +") = "+ n);
        return n;
    }
}