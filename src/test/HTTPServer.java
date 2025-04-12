package test;

public interface HTTPServer extends Runnable {

    void addServlet(String httpMethod, String path, Servlet s);

    void removeServlet(String httpMethod, String path);

    void start();

    void close();
}