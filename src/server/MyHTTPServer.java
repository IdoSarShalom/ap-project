package server;

import servlets.Servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MyHTTPServer extends Thread implements HTTPServer {
    private final int port;
    private final int nThreads;
    private volatile boolean running;
    private final ConcurrentHashMap<String, Servlet> getUriToServletMap;
    private final ConcurrentHashMap<String, Servlet> postUriToServletMap;
    private final ConcurrentHashMap<String, Servlet> deleteUriToServletMap;
    private ServerSocket serverSocket;
    private ExecutorService executor;

    public MyHTTPServer(int port, int maxThreads) {
        super("MyHTTPServer-MainThread");
        this.port = port;
        this.nThreads = maxThreads;
        this.running = false;
        this.getUriToServletMap = new ConcurrentHashMap<>();
        this.postUriToServletMap = new ConcurrentHashMap<>();
        this.deleteUriToServletMap = new ConcurrentHashMap<>();
        this.serverSocket = null;
        this.executor = null;
    }

    @Override
    public void addServlet(String httpCommand, String uri, Servlet s) {
        Map<String, Servlet> servletMap = getServletMap(httpCommand.toUpperCase());
        if (servletMap != null) {
            servletMap.put(uri, s);
        }
    }

    @Override
    public void removeServlet(String httpCommand, String uri) {
        Map<String, Servlet> servletMap = getServletMap(httpCommand.toUpperCase());
        if (servletMap != null) {
            closeAndRemoveServlet(servletMap, uri);
        }
    }

    @Override
    public void start() {
        if (!running) {
            initializeServer();
            super.start();
        }
    }

    @Override
    public void run() {
        try (ServerSocket ss = createServerSocket()) {
            acceptClientConnections(ss);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeExecutor();
        }
    }

    @Override
    public void close() {
        stopServer();
        closeAllServlets();
        closeExecutor();
    }

    private void handleClient(Socket client) {
        try (Socket c = client;
             BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
             OutputStream out = c.getOutputStream()) {
            processClientRequest(br, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Servlet> getServletMap(String httpCommand) {
        if (httpCommand.equals("GET")) {
            return getUriToServletMap;
        } else if (httpCommand.equals("POST")) {
            return postUriToServletMap;
        } else if (httpCommand.equals("DELETE")) {
            return deleteUriToServletMap;
        }
        return null;
    }

    private void closeAndRemoveServlet(Map<String, Servlet> servletMap, String uri) {
        Servlet servlet = servletMap.remove(uri);
        if (servlet != null) {
            try {
                servlet.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initializeServer() {
        running = true;
        executor = Executors.newFixedThreadPool(nThreads);
    }

    private ServerSocket createServerSocket() throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(1000);
        return serverSocket;
    }

    private void acceptClientConnections(ServerSocket ss) throws IOException {
        while (running) {
            try {
                Socket client = ss.accept();
                executor.submit(() -> handleClient(client));
            } catch (IOException e) {
                if (!running) {
                    break;
                }
            }
        }
    }

    private void processClientRequest(BufferedReader br, OutputStream out) throws IOException {
        RequestParser.RequestInfo ri = RequestParser.parseRequest(br);
        if (ri == null) {
            writeBadRequest(out, "Malformed request");
            return;
        }
        Servlet servlet = findServlet(ri.getHttpCommand(), ri.getResourceUri());
        if (servlet == null) {
            writeNotFound(out, "No servlet for " + ri.getHttpCommand() + " " + ri.getResourceUri());
            return;
        }
        servlet.handle(ri, out);
    }

    private Servlet findServlet(String httpCommand, String uri) {
        Map<String, Servlet> servletMap = getServletMap(httpCommand.toUpperCase());
        if (servletMap == null) {
            return null;
        }
        return matchServletToUri(uri, servletMap);
    }

    private Servlet matchServletToUri(String uri, Map<String, Servlet> uriToServlet) {
        Servlet matchingServlet = null;
        int longestPrefixLength = -1;
        for (String currentUri : uriToServlet.keySet()) {
            if (uri.startsWith(currentUri) && currentUri.length() > longestPrefixLength) {
                longestPrefixLength = currentUri.length();
                matchingServlet = uriToServlet.get(currentUri);
            }
        }
        return matchingServlet;
    }

    private void writeBadRequest(OutputStream out, String msg) throws IOException {
        String resp = "HTTP/1.1 400 Bad Request\r\n\r\n" + msg;
        out.write(resp.getBytes());
    }

    private void writeNotFound(OutputStream out, String msg) throws IOException {
        String resp = "HTTP/1.1 404 Not Found\r\n\r\n" + msg;
        out.write(resp.getBytes());
    }

    private void stopServer() {
        running = false;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeAllServlets() {
        for (Map<String, Servlet> servletMap : new Map[]{getUriToServletMap, postUriToServletMap, deleteUriToServletMap}) {
            for (Servlet servlet : servletMap.values()) {
                try {
                    servlet.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void closeExecutor() {
        if (executor != null) {
            executor.shutdownNow();
            try {
                executor.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}