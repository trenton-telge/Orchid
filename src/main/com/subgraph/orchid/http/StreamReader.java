package com.subgraph.orchid.http;

import com.subgraph.orchid.logging.Logger;
import java.io.IOException;
import java.io.InputStream;

public class StreamReader {
    private static final Logger logger = Logger.getInstance(StreamReader.class);
    public static String read(InputStream stream) {
        StringBuilder value = new StringBuilder();
        int character;
        try (stream) {
            while ((character = stream.read()) != -1) {
                value.append((char) character);
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        //Could not close stream
        return value.toString();
    }    
}