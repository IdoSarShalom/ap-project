import graph.ConfLoader;
import graph.EmptyLoader;
import graph.TopicDisplayer;
import server.HTTPServer;
import server.MyHTTPServer;
import servlets.HtmlLoader;

public class Main {
    public static void main(String[] args) throws Exception {
        HTTPServer server = new MyHTTPServer(8080, 5);
        server.addServlet("GET", "/publish", new TopicDisplayer());
        server.addServlet("POST", "/upload", new ConfLoader());
        server.addServlet("POST", "/adam", new EmptyLoader());
        server.addServlet("GET", "/app/", new HtmlLoader("web"));
        server.start();
        System.in.read();
        server.close();
        System.out.println("done");
    }
}