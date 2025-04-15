package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RequestParser {

    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        // 1. Read Request Line
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Empty or null request line received.");
        }
        String[] requestLineParts = splitRequestLine(requestLine);
        String httpCommand = extractHttpCommand(requestLineParts);
        String uri = extractUri(requestLineParts);
        String resourceUri = extractResourceUri(uri);
        String[] uriSegments = extractUriSegments(resourceUri);
        Map<String, String> parameters = extractParameters(uri);

        // 2. Read Headers
        Map<String, String> headers = readHeaders(reader);

        // 3. Read Content (Body) based on Content-Length
        byte[] content = readContent(reader, headers);

        return new RequestInfo(httpCommand, uri, resourceUri, uriSegments, parameters, headers, content);
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
        int contentLength = 0;
        String contentLengthHeader = headers.get("Content-Length");
        if (contentLengthHeader != null) {
            try {
                contentLength = Integer.parseInt(contentLengthHeader.trim());
                if (contentLength < 0) {
                    throw new IOException("Invalid Content-Length (negative value): " + contentLengthHeader);
                }
            } catch (NumberFormatException e) {
                // If Content-Length is present but invalid, it's a bad request.
                throw new IOException("Invalid Content-Length header value: " + contentLengthHeader, e);
            }
        }

        // If Content-Length header is missing or zero, assume no body.
        if (contentLength <= 0) {
            return new byte[0];
        }

        // Read exactly contentLength characters
        char[] contentChars = new char[contentLength];
        int bytesRead = 0;
        while (bytesRead < contentLength) {
            int result = reader.read(contentChars, bytesRead, contentLength - bytesRead);
            if (result == -1) {
                // This indicates the client closed the connection before sending the full body
                throw new IOException("Unexpected end of stream while reading request body. Expected " + contentLength + " bytes, got " + bytesRead);
            }
            bytesRead += result;
        }

        // Convert char array to byte array (assuming UTF-8)
        return new String(contentChars).getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private static String[] splitRequestLine(String line) {
        return line.split("\\s+", 3);
    }

    private static String extractHttpCommand(String[] parts) {
        return parts[0].toUpperCase();
    }

    private static String extractUri(String[] parts) {
        return parts.length > 1 ? parts[1] : "";
    }

    private static String extractResourceUri(String uri) {
        return uri.contains("?") ? uri.substring(0, uri.indexOf('?')) : uri;
    }

    private static String[] extractUriSegments(String resourceUri) {
        return Arrays.stream(resourceUri.split("/"))
                .filter(part -> !part.isEmpty())
                .toArray(String[]::new);
    }

    private static Map<String, String> extractParameters(String uri) {
        String query = uri.contains("?") ? uri.substring(uri.indexOf('?') + 1) : "";
        if (query.isEmpty()) return new HashMap<>();

        Map<String, String> parameters = new LinkedHashMap<>();

        for (String param : query.split("&")) {
            if (param.trim().isEmpty()) continue;

            String[] keyValuePair = param.split("=", 2);
            if (hasInvalidKey(keyValuePair)) continue;

            addParameter(keyValuePair, parameters);
        }

        return parameters;
    }

    private static boolean hasInvalidKey(String[] keyValuePair) {
        return keyValuePair.length == 0 || keyValuePair[0].trim().isEmpty();
    }

    private static void addParameter(String[] keyValuePair, Map<String, String> parameters) {
        String key = keyValuePair[0].trim();
        String value = keyValuePair.length > 1 ? keyValuePair[1].trim() : "";
        parameters.put(key, value);
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