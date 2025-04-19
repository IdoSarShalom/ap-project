import graph.TopicDisplayer;
import server.HTTPServer;
import server.MyHTTPServer;
import servlets.ConfLoader;
import servlets.FaviconServlet;
import servlets.HtmlLoader;

/**
 * Main class that serves as the entry point for the Pub/Sub system application.
 * This class initializes the HTTP server and registers all required servlets
 * for handling different types of HTTP requests.
 */
public class Main {
    /**
     * Main method that starts the application server.
     * 
     * @param args Command line arguments (not used)
     * @throws Exception If there's an error starting the server or registering servlets
     */
    public static void main(String[] args) throws Exception {
        HTTPServer server = new MyHTTPServer(8080, 5);
        server.addServlet("GET", "/publish", new TopicDisplayer());
        server.addServlet("POST", "/upload", new ConfLoader());
        server.addServlet("GET", "/app/", new HtmlLoader("web"));
        server.addServlet("GET", "/favicon.ico", new FaviconServlet());
        server.start();
        System.in.read();
        server.close();
        System.out.println("Server stopped.");
    }
}