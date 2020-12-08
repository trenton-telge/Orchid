package com.subgraph.orchid.sockets;

import com.subgraph.orchid.logging.Logger;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

@SuppressWarnings("SameParameterValue")
public class AndroidSocket extends Socket {
    private static final Logger logger = Logger.getInstance(AndroidSocket.class);
	
    private final Field isConnectedField;
    private final OrchidSocketImpl impl;
    private final Object lock = new Object();
    private boolean isSocketConnected;

    AndroidSocket(OrchidSocketImpl impl) throws SocketException {
        super(impl);
        this.impl = impl;
        this.isConnectedField = getField("isConnected");
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        connect(endpoint, 0);
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        synchronized(lock) {
            if(isSocketConnected) {
                throw new SocketException("Already connected");
            }
            try {
                impl.connect(endpoint, timeout);
                setIsConnected();
            } catch(IOException e) {
                impl.close();
                throw e;
            }
        }
    }

    protected void setIsConnected() {
        isSocketConnected = true;
        try {
            if(isConnectedField != null) {
                isConnectedField.setBoolean(this,  true);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Illegal argument trying to reflect value into isConnected field of Socket : "+ e.getMessage());
        } catch (IllegalAccessException e) {
            logger.warn("Illegal access trying to reflect value into isConnected field of Socket : "+ e.getMessage());
        }
    }

    private Field getField(String name) {
        try {
            final Field f = Socket.class.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            logger.warn("Could not locate field '"+ name +"' in Socket class, disabling Android reflection");
            return null;
        } catch (SecurityException e) {
            logger.warn("Reflection access to field '"+ name +"' in Socket class not permitted."+ e.getMessage());
            return null;
        }
    }
}
