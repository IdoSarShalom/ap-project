package graph;

import server.RequestParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class App5 {
    public static void main(String[] args) {
        String httpRequest1 = """
                GET /api/resource?id=123&name=test&ido=5&m=8 HTTP/1.1
                Host: localhost
                Content-Length: 11
                
                hello world
                """;

        String httpRequest = "GET /api/resource?id=123&name=test HTTP/1.1\n" +
                "Host: example.com\n" +
                "Content-Length: 5\n"+
                "\n" +
                "filename=\"hello_world.txt\"\n"+
                "\n" +
                "hello world!\n"+
                "\n" ;

        try (BufferedReader reader = new BufferedReader(new StringReader(httpRequest))) {
            RequestParser.RequestInfo requestInfo = RequestParser.parseRequest(reader);

            System.out.println("HTTP Command: " + requestInfo.getHttpCommand());
            System.out.println("URI: " + requestInfo.getUri());
            System.out.println("Resource URI: " + requestInfo.getResourceUri());
            System.out.println("Resource URI Parts: " + String.join(", ", requestInfo.getUriSegments()));
            System.out.println("Params: " + requestInfo.getParameters());
            System.out.println("Headers: " + requestInfo.getHeaders());
            System.out.println("Content: " + new String(requestInfo.getContent()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}