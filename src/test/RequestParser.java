package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RequestParser {

    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        List<String> allLines = readAllLines(reader);
        ListIterator<String> iterator = allLines.listIterator();
        String[] requestLineParts = parseRequestLineParts(iterator);
        String httpCommand = extractHttpCommand(requestLineParts);
        String uri = extractUri(requestLineParts);
        String resourceUri = extractResourceUri(uri);
        String[] uriSegments = extractUriSegments(resourceUri);
        Map<String, String> parameters = extractParameters(uri);
        Map<String, String> headers = buildHeaders(iterator);
        buildOtherParameters(iterator);
        byte[] content = buildContentFromIterator(iterator, headers);

        return new RequestInfo(httpCommand, uri, resourceUri, uriSegments, parameters, headers, content);
    }

    private static List<String> readAllLines(BufferedReader reader) throws IOException {
        List<String> lines = new ArrayList<>();
        String line;

        while (reader.ready() && (line = reader.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    private static String[] parseRequestLineParts(ListIterator<String> iterator) {
        String line = getNextLine(iterator);
        return splitRequestLine(line);
    }

    private static String getNextLine(ListIterator<String> iterator) {
        return iterator.hasNext() ? iterator.next() : "";
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
        return Arrays.stream(resourceUri.split(File.separator))
                .filter(part -> !part.isEmpty())
                .toArray(String[]::new);
    }

    private static Map<String, String> extractParameters(String uri) {
        String query = uri.contains("?") ? uri.substring(uri.indexOf('?') + 1) : "";
        return query.isEmpty()
                ? new HashMap<>()
                : Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2))
                .collect(Collectors.toMap(
                        keyValuePair -> keyValuePair[0],
                        keyValuePair -> keyValuePair.length > 1 ? keyValuePair[1] : ""));
    }

    private static Map<String, String> buildHeaders(ListIterator<String> iterator) {
        Map<String, String> headers = new LinkedHashMap<>();

        while (iterator.hasNext()) {
            String line = iterator.next();
            if (line.isEmpty()) {
                break;
            }
            String[] headerParts = line.split(":", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0].trim(), headerParts[1].trim());
            }
        }
        return headers;
    }

    private static void buildOtherParameters(ListIterator<String> iterator) {

        while (iterator.hasNext()) {
            String line = iterator.next();
            if (line.isEmpty()) {
                break;
            }
        }
    }

    private static byte[] buildContentFromIterator(ListIterator<String> iterator, Map<String, String> headers) {
        StringBuilder contentBuilder = new StringBuilder();

        while (iterator.hasNext()) {
            String line = iterator.next();
            contentBuilder.append(line).append("\n");
        }
        String fullContent = contentBuilder.toString();

        return fullContent.getBytes();
    }

    public static class RequestInfo {
        private final String httpCommand; // e.g. GET, POST, DELETE
        private final String uri; // e.g. /api/resource?id=123&name=test
        private final String resourceUri; // /api/resource
        private final String[] uriSegments; // e.g. "api, resource"
        private final Map<String, String> parameters; // e.g. {id=123, name=test}
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