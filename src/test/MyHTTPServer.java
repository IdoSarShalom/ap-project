package test;

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


/**
 * A basic HTTP server that extends Thread:
 * - On start(), it calls super.start() to run the main loop in parallel.
 * - Maintains a thread pool for client handling.
 * - Routes requests by (command, uriPrefix) to a Servlet using longest-prefix
 * matching.
 */
public class MyHTTPServer extends Thread implements HTTPServer {
    private final int port;
    private final int nThreads;
    private volatile boolean running;
    private final Map<String, ConcurrentHashMap<String, Servlet>> httpMethodToPathServletMap;
    private ServerSocket serverSocket;
    private ExecutorService executor;

    public MyHTTPServer(int port, int maxThreads) {
        super("MyHTTPServer-MainThread"); // Name the thread if desired
        this.port = port;
        this.nThreads = maxThreads;
        this.running = false;
        this.httpMethodToPathServletMap = new ConcurrentHashMap<>();
        this.httpMethodToPathServletMap.put("GET", new ConcurrentHashMap<>());
        this.httpMethodToPathServletMap.put("POST", new ConcurrentHashMap<>());
        this.httpMethodToPathServletMap.put("DELETE", new ConcurrentHashMap<>());
        this.serverSocket = null;
        this.executor = null;
    }

    @Override
    public void addServlet(String httpMethod, String path, Servlet s) {
        httpMethod = httpMethod.toUpperCase();
        httpMethodToPathServletMap.putIfAbsent(httpMethod, new ConcurrentHashMap<>());
        httpMethodToPathServletMap.get(httpMethod).put(path, s);
    }

    @Override
    public void removeServlet(String httpMethod, String path) {
        httpMethod = httpMethod.toUpperCase();
        Map<String, Servlet> pathToServlet = httpMethodToPathServletMap.get(httpMethod);

        if (pathToServlet != null) {
            Servlet servlet = pathToServlet.remove(path);

            if (servlet != null) {

                try {
                    servlet.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void start() {

        if (!running) {
            running = true;
            this.executor = Executors.newFixedThreadPool(nThreads);
            super.start(); // Calls run() method in a new thread
        }
    }

    @Override
    public void run() {
        try (ServerSocket ss = new ServerSocket(port)) {
            this.serverSocket = ss;
            this.serverSocket.setSoTimeout(1000);

            while (running) {

                try {
                    Socket client = this.serverSocket.accept();
                    executor.submit(() -> handleClient(client));
                } catch (IOException e) {

                    if (!running) {
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            closeExecutor(); // Once run() exits, ensure resources are freed
        }
    }

    private void handleClient(Socket client) {

        try (Socket c = client;
             BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
             OutputStream out = c.getOutputStream()) {
            RequestParser.RequestInfo ri = RequestParser.parseRequest(br);

            if (ri == null) {
                writeBadRequest(out, "Malformed request");
                return;
            }
            Servlet servlet = getServlet(ri.getHttpMethod(), ri.getPath()); // Find the matching servlet

            if (servlet == null) {
                writeNotFound(out, "No servlet for " + ri.getHttpMethod() + " " + ri.getPath());
                return;
            }
            servlet.handle(ri, out); // Dispatch to the servlet

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Servlet getServlet(String httpMethod, String path) {
        httpMethod = httpMethod.toUpperCase();
        Map<String, Servlet> pathToServlet = httpMethodToPathServletMap.get(httpMethod);

        if (pathToServlet == null) {
            return null;
        }

        return matchServletToPath(path, pathToServlet);
    }

    private Servlet matchServletToPath(String path, Map<String, Servlet> pathToServlet) {
        Servlet matchingServlet = null;
        int longestPrefixLength = -1;

        for (String currentPath : pathToServlet.keySet()) {

            if (hasLongerPrefix(path, currentPath, longestPrefixLength)) {
                longestPrefixLength = currentPath.length();
                matchingServlet = pathToServlet.get(currentPath);
            }
        }

        return matchingServlet;
    }

    private boolean hasLongerPrefix(String path, String currentPath, int longestPrefixLength) {
        return path.startsWith(currentPath) && currentPath.length() > longestPrefixLength;
    }

    private void writeBadRequest(OutputStream out, String msg) throws IOException {
        String resp = "HTTP/1.1 400 Bad Request\r\n\r\n" + msg;
        out.write(resp.getBytes());
    }

    private void writeNotFound(OutputStream out, String msg) throws IOException {
        String resp = "HTTP/1.1 404 Not Found\r\n\r\n" + msg;
        out.write(resp.getBytes());
    }

    @Override
    public void close() {
        running = false;

        if (serverSocket != null) {

            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (Map<String, Servlet> httpMethodToServlet : httpMethodToPathServletMap.values()) {

            for (Servlet servlet : httpMethodToServlet.values()) {

                try {
                    servlet.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        closeExecutor();
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
