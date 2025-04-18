package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class for parsing HTTP requests.
 * <p>
 * This class provides methods to parse an HTTP request from a BufferedReader
 * and extract various components like the HTTP method, URI, headers, and body.
 * It breaks down the request into structured components for easier handling
 * by servlets.
 */
public class RequestParser {

    /**
     * Parses an HTTP request from a BufferedReader.
     * <p>
     * This method reads the request line, headers, and body from the reader
     * and returns a structured RequestInfo object containing all components
     * of the request.
     *
     * @param reader The BufferedReader containing the HTTP request
     * @return A RequestInfo object representing the parsed request
     * @throws IOException If an I/O error occurs while reading from the reader
     */
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

    /**
     * Reads the first line of the HTTP request.
     *
     * @param reader The BufferedReader to read from
     * @return The request line as a string
     * @throws IOException If an I/O error occurs or the request line is empty
     */
    private static String readRequestLine(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();

        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Empty or null request line received.");
        }

        return requestLine;
    }

    /**
     * Splits the request line into its components.
     *
     * @param line The request line to split
     * @return An array of strings containing the HTTP method, URI, and HTTP version
     */
    private static String[] splitRequestLine(String line) {
        return line.split("\\s+", 3);
    }

    /**
     * Validates that the request line has the minimum required parts.
     *
     * @param parts The array of parts from the request line
     * @throws IOException If the request line is invalid
     */
    private static void validateRequestLine(String[] parts) throws IOException {
        if (parts.length < 2) {
            throw new IOException("Invalid request line: insufficient parts.");
        }
    }

    /**
     * Extracts the HTTP method from the request line parts.
     *
     * @param parts The array of parts from the request line
     * @return The HTTP method in uppercase
     */
    private static String extractHttpCommand(String[] parts) {
        return parts[0].toUpperCase();
    }

    /**
     * Extracts the URI from the request line parts.
     *
     * @param parts The array of parts from the request line
     * @return The URI or an empty string if not present
     */
    private static String extractUri(String[] parts) {
        if (parts.length > 1) {
            return parts[1];
        }

        return "";
    }

    /**
     * Extracts the resource part of the URI (without query parameters).
     *
     * @param uri The full URI
     * @return The resource part of the URI
     */
    private static String extractResourceUri(String uri) {
        if (uri.contains("?")) {
            return uri.substring(0, uri.indexOf('?'));
        }

        return uri;
    }

    /**
     * Extracts the segments of the resource URI.
     * <p>
     * For example, "/api/resource" would yield ["api", "resource"].
     *
     * @param resourceUri The resource URI
     * @return An array of URI segments
     */
    private static String[] extractUriSegments(String resourceUri) {
        return Arrays.stream(resourceUri.split("/"))
                .filter(part -> !part.isEmpty())
                .toArray(String[]::new);
    }

    /**
     * Extracts query parameters from the URI.
     *
     * @param uri The full URI
     * @return A map of parameter names to values
     */
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

    /**
     * Parses a single parameter from a query string.
     *
     * @param param The parameter string in the format "key=value"
     * @param parameters The map to add the parsed parameter to
     */
    private static void parseParameter(String param, Map<String, String> parameters) {
        String[] keyValuePair = param.split("=", 2);

        if (keyValuePair.length == 0 || keyValuePair[0].trim().isEmpty()) {
            return;
        }
        String key = keyValuePair[0].trim();
        String value = keyValuePair.length > 1 ? keyValuePair[1].trim() : "";
        parameters.put(key, value);
    }

    /**
     * Reads HTTP headers from the request.
     *
     * @param reader The BufferedReader to read from
     * @return A map of header names to values
     * @throws IOException If an I/O error occurs while reading
     */
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

    /**
     * Reads the request body content.
     *
     * @param reader The BufferedReader to read from
     * @param headers The request headers
     * @return The request body as a byte array
     * @throws IOException If an I/O error occurs while reading
     */
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

    /**
     * Parses the Content-Length header value.
     *
     * @param headers The request headers
     * @return The content length as an integer
     * @throws IOException If the Content-Length header is invalid
     */
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

    /**
     * Class representing a parsed HTTP request.
     * <p>
     * Contains all the components of an HTTP request in a structured format,
     * including the HTTP method, URI, headers, and body.
     */
    public static class RequestInfo {
        /** The HTTP method (e.g., GET, POST, DELETE) */
        private final String httpCommand;
        
        /** The full URI including query parameters (e.g., /api/resource?id=123&name=test) */
        private final String uri;
        
        /** The resource part of the URI without query parameters (e.g., /api/resource) */
        private final String resourceUri;
        
        /** The segments of the resource URI (e.g., ["api", "resource"]) */
        private final String[] uriSegments;
        
        /** The query parameters as a map (e.g., {id=123, name=test}) */
        private final Map<String, String> parameters;
        
        /** The HTTP headers as a map */
        private final Map<String, String> headers;
        
        /** The request body as a byte array */
        private final byte[] content;

        /**
         * Creates a new RequestInfo object.
         *
         * @param httpCommand The HTTP method
         * @param uri The full URI
         * @param resourceUri The resource part of the URI
         * @param uriSegments The segments of the resource URI
         * @param parameters The query parameters
         * @param headers The HTTP headers
         * @param content The request body
         */
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

        /**
         * Gets the HTTP method.
         *
         * @return The HTTP method
         */
        public String getHttpCommand() {
            return httpCommand;
        }

        /**
         * Gets the full URI.
         *
         * @return The full URI
         */
        public String getUri() {
            return uri;
        }

        /**
         * Gets the resource part of the URI.
         *
         * @return The resource URI
         */
        public String getResourceUri() {
            return resourceUri;
        }

        /**
         * Gets the segments of the resource URI.
         *
         * @return The URI segments
         */
        public String[] getUriSegments() {
            return uriSegments;
        }

        /**
         * Gets the query parameters.
         *
         * @return The parameters as a map
         */
        public Map<String, String> getParameters() {
            return parameters;
        }

        /**
         * Gets the HTTP headers.
         *
         * @return The headers as a map
         */
        public Map<String, String> getHeaders() {
            return headers;
        }

        /**
         * Gets the request body.
         *
         * @return The content as a byte array
         */
        public byte[] getContent() {
            return content;
        }
    }
}