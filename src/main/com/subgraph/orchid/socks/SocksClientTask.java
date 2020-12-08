package com.subgraph.orchid.socks;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeoutException;

import com.subgraph.orchid.CircuitManager;
import com.subgraph.orchid.OpenFailedException;
import com.subgraph.orchid.Stream;
import com.subgraph.orchid.TorConfig;
import com.subgraph.orchid.TorException;
import com.subgraph.orchid.logging.Logger;

public class SocksClientTask implements Runnable {
    private static final Logger logger = Logger.getInstance(SocksClientTask.class);
	
    private final TorConfig config;
    private final Socket socket;
    private final CircuitManager circuitManager;

    SocksClientTask(TorConfig config, Socket socket, CircuitManager circuitManager) {
        this.config = config;
        this.socket = socket;
        this.circuitManager = circuitManager;
    }

    @Override
    public void run() {
        final int version = readByte();
        dispatchRequest(version);
        closeSocket();
    }

    private int readByte() {
        try {
            return socket.getInputStream().read();
        } catch (IOException e) {
            logger.warn("IO error reading version byte: "+ e.getMessage());
            return -1;
        }
    }

    private void dispatchRequest(int versionByte) {
        switch(versionByte) {
        case 'H':
        case 'G':
        case 'P':
            sendHttpPage();
            break;
        case 4:
            processRequest(new Socks4Request(config, socket));
            break;
        case 5:
            processRequest(new Socks5Request(config, socket));
            break;
        default:
            // fall through, do nothing
            break;
        }	
    }

    private void processRequest(SocksRequest request) {
        try {
            request.readRequest();
            if(!request.isConnectRequest()) {
                logger.warn("Non connect command ("+ request.getCommandCode() + ")");
                request.sendError(true);
                return;
            }

            try {
                final Stream stream = openConnectStream(request);
                logger.debug("SOCKS CONNECT to "+ request.getTarget()+ " completed");
                request.sendSuccess();
                runOpenConnection(stream);
            } catch (InterruptedException e) {
                logger.info("SOCKS CONNECT to "+ request.getTarget() + " was thread interrupted");
                Thread.currentThread().interrupt();
                request.sendError(false);
            } catch (TimeoutException e) {
                logger.info("SOCKS CONNECT to "+ request.getTarget() + " timed out");
                request.sendError(false);
            } catch (OpenFailedException e) {
                logger.info("SOCKS CONNECT to "+ request.getTarget() + " failed: "+ e.getMessage());
                request.sendConnectionRefused();
            }
        } catch (SocksRequestException e) {
            logger.warn("Failure reading SOCKS request: "+ e.getMessage());
            try {
                request.sendError(false);
                socket.close();
            } catch (Exception ignore) {
                //swallow
            }
        } 
    }


    private void runOpenConnection(Stream stream) {
        SocksStreamConnection.runConnection(socket, stream);
    }

    private Stream openConnectStream(SocksRequest request) throws InterruptedException, TimeoutException, OpenFailedException {
        if(request.hasHostname()) {
            logger.debug("SOCKS CONNECT request to "+ request.getHostname() +":"+ request.getPort());
            return circuitManager.openExitStreamTo(request.getHostname(), request.getPort());
        } else {
            logger.debug("SOCKS CONNECT request to "+ request.getAddress() +":"+ request.getPort());
            return circuitManager.openExitStreamTo(request.getAddress(), request.getPort());
        }
    }

    private void sendHttpPage() {
        throw new TorException("Returning HTTP page not implemented");
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.warn("Error closing SOCKS socket: "+ e.getMessage());
        }
    }
}
