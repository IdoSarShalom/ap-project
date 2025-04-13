package servlets;

import server.RequestParser;

import java.io.IOException;
import java.io.OutputStream;

public interface Servlet {

    /**
     * Handle a parsed request (RequestInfo) and write output to 'toClient'.
     */
    void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException;

    /**
     * Called when shutting down the server or removing this Servlet.
     */
    void close() throws IOException;
}