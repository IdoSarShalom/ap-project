package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RequestParser {

    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        String requestLine = readRequestLine(reader);
        String[] requestLineParts = splitRequestLine(requestLine);
        validateRequestLine(requestLineParts);
        String httpCommand = extractHttpCommand(requestLineParts);
        String uri = extractUri(requestLineParts);
        String resourceUri = extractResourceUri(uri);
        String[] uriSegments = extractUriSegments(resourceUri);
        Map<String, String> parameters = extractParameters(uri);
        Map<String, String> headers = readHeaders(reader);
        byte[] content = readContent(reader, headers);

        return new RequestInfo(httpCommand, uri, resourceUri, uriSegments, parameters, headers, content);
    }

    private static String readRequestLine(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();

        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Empty or null request line received.");
        }

        return requestLine;
    }

    private static String[] splitRequestLine(String line) {
        return line.split("\\s+", 3);
    }

    private static void validateRequestLine(String[] parts) throws IOException {
        if (parts.length < 2) {
            throw new IOException("Invalid request line: insufficient parts.");
        }
    }

    private static String extractHttpCommand(String[] parts) {
        return parts[0].toUpperCase();
    }

    private static String extractUri(String[] parts) {
        if (parts.length > 1) {
            return parts[1];
        }

        return "";
    }

    private static String extractResourceUri(String uri) {
        if (uri.contains("?")) {
            return uri.substring(0, uri.indexOf('?'));
        }

        return uri;
    }

    private static String[] extractUriSegments(String resourceUri) {
        return Arrays.stream(resourceUri.split("/"))
                .filter(part -> !part.isEmpty())
                .toArray(String[]::new);
    }

    private static Map<String, String> extractParameters(String uri) {
        String query = uri.contains("?") ? uri.substring(uri.indexOf('?') + 1) : "";

        if (query.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, String> parameters = new LinkedHashMap<>();

        for (String param : query.split("&")) {
            if (param.trim().isEmpty()) {
                continue;
            }
            parseParameter(param, parameters);
        }

        return parameters;
    }

    private static void parseParameter(String param, Map<String, String> parameters) {
        String[] keyValuePair = param.split("=", 2);

        if (keyValuePair.length == 0 || keyValuePair[0].trim().isEmpty()) {
            return;
        }
        String key = keyValuePair[0].trim();
        String value = keyValuePair.length > 1 ? keyValuePair[1].trim() : "";
        parameters.put(key, value);
    }

    private static Map<String, String> readHeaders(BufferedReader reader) throws IOException {
        Map<String, String> headers = new LinkedHashMap<>();
        String line;

        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            String[] headerParts = line.split(":", 2);

            if (headerParts.length == 2) {
                headers.put(headerParts[0].trim(), headerParts[1].trim());
            }
        }

        return headers;
    }

    private static byte[] readContent(BufferedReader reader, Map<String, String> headers) throws IOException {
        int contentLength = parseContentLength(headers);

        if (contentLength <= 0) {
            return new byte[0];
        }

        char[] contentChars = new char[contentLength];
        int bytesRead = 0;

        while (bytesRead < contentLength) {
            int result = reader.read(contentChars, bytesRead, contentLength - bytesRead);

            if (result == -1) {
                throw new IOException("Unexpected end of stream while reading request body. Expected " + contentLength + " bytes, got " + bytesRead);
            }
            bytesRead += result;
        }

        return new String(contentChars).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private static int parseContentLength(Map<String, String> headers) throws IOException {
        String contentLengthHeader = headers.get("Content-Length");

        if (contentLengthHeader == null) {
            return 0;
        }

        try {
            int contentLength = Integer.parseInt(contentLengthHeader.trim());

            if (contentLength < 0) {
                throw new IOException("Invalid Content-Length (negative value): " + contentLengthHeader);
            }

            return contentLength;
        } catch (NumberFormatException e) {
            throw new IOException("Invalid Content-Length header value: " + contentLengthHeader, e);
        }
    }

    public static class RequestInfo {
        private final String httpCommand; // e.g. GET, POST, DELETE
        private final String uri; // e.g. /api/resource?id=123&name=test
        private final String resourceUri; // /api/resource
        private final String[] uriSegments; // e.g. "api, resource"
        private final Map<String, String> parameters; // e.g. {id=123, name=test, filename=hello_world.txt}
        private final Map<String, String> headers;
        private final byte[] content;

        public RequestInfo(String httpCommand, String uri, String resourceUri, String[] uriSegments,
                           Map<String, String> parameters, Map<String, String> headers, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.resourceUri = resourceUri;
            this.uriSegments = uriSegments;
            this.parameters = parameters;
            this.headers = headers;
            this.content = content;
        }

        public String getHttpCommand() {
            return httpCommand;
        }

        public String getUri() {
            return uri;
        }

        public String getResourceUri() {
            return resourceUri;
        }

        public String[] getUriSegments() {
            return uriSegments;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public byte[] getContent() {
            return content;
        }
    }
}