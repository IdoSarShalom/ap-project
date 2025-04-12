package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RequestParser {

    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        RequestInfo requestInfo = new RequestInfo();
        readRequestLine(requestInfo, reader);
        readHeaderLines(requestInfo, reader);

        if (reader.ready()) { // todo: checks!!
            String nextLine = reader.readLine();
            if (nextLine != null && !nextLine.isEmpty()) {
                if (isMetadataLine(nextLine)) {
                    readMetadataLines(requestInfo, reader, nextLine);
                }
                readContentLines(requestInfo, reader, nextLine);
            }
        }

        return requestInfo;
    }

    private static void readRequestLine(RequestInfo requestInfo, BufferedReader reader) throws IOException {
        String line = reader.readLine();
        validateLineIsNotNull(line);

        String[] firstLineParts = line.split("\\s+", 3);
        validateThreePartsOfFirstLine(firstLineParts);

        String httpMethod = firstLineParts[0].toUpperCase();
        String uri = firstLineParts[1];
        String path = getPath(uri);
        String[] pathParts = getPathParts(path);
        String query = getQuery(uri);
        Map<String, String> params = getParams(query);

        setRequestInfo(requestInfo, httpMethod, uri, path, pathParts, params);
    }

    private static boolean validateLineIsNotNull(String line) {
        if (Objects.isNull(line)) {
            throwInvalidRequestFormatException();
        }
        return true;
    }

    private static void throwInvalidRequestFormatException() {
        throw new RuntimeException("The request does not match the expected format, unable to parse.");
    }

    private static void validateThreePartsOfFirstLine(String[] firstLineParts) {
        if (firstLineParts.length < 3) {
            throwInvalidRequestFormatException();
        }
    }

    private static String getPath(String uri) {
        return uri.contains("?") ? uri.substring(0, uri.indexOf('?')) : uri;
    }

    private static String[] getPathParts(String path) {
        return Arrays.stream(path.split(File.separator))
                .filter(part -> !part.isEmpty())
                .toArray(String[]::new);
    }

    private static String getQuery(String uri) {
        return uri.contains("?") ? uri.substring(uri.indexOf('?') + 1) : "";
    }

    private static Map<String, String> getParams(String query) {
        return (query == null || query.isEmpty())
                ? new HashMap<>()
                : Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2))
                .collect(Collectors.toMap(
                        keyValuePair -> keyValuePair[0],
                        keyValuePair -> keyValuePair.length > 1 ? keyValuePair[1] : ""));
    }

    private static void setRequestInfo(RequestInfo requestInfo, String httpMethod, String uri, String path,
                                       String[] pathParts,
                                       Map<String, String> params) {
        requestInfo.setHttpMethod(httpMethod);
        requestInfo.setUri(uri);
        requestInfo.setPath(path);
        requestInfo.setPathParts(pathParts);
        requestInfo.setParams(params);
    }

    private static void readHeaderLines(RequestInfo requestInfo, BufferedReader reader) throws IOException {
        Map<String, String> headers = new LinkedHashMap<>();
        String line;

        while (reader.ready() && (line = reader.readLine()) != null && !line.isEmpty()) {
            String[] headerParts = line.split(":", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0].trim(), headerParts[1].trim());
            }
        }
        requestInfo.setHeaders(headers);
    }

    private static boolean isMetadataLine(String line) {
        String[] parts = line.split("=", 2);
        return parts.length == 2 && !parts[0].trim().isEmpty() && !parts[1].trim().isEmpty();
    }

    private static void readMetadataLines(RequestInfo requestInfo, BufferedReader reader, String line)
            throws IOException {
        Map<String, String> metadata = new HashMap<>();
        parseMetadataLine(metadata, line);
        String nextLine;

        while (reader.ready() && (nextLine = reader.readLine()) != null && !nextLine.isEmpty()) {
            parseMetadataLine(metadata, nextLine);
        }
        requestInfo.setMetadata(metadata);
    }

    private static void parseMetadataLine(Map<String, String> metadata, String line) {
        String[] parts = line.split("=", 2);

        if (parts.length == 2) {
            String key = parts[0].trim();
            String value = parts[1].trim().replace("\"", "");
            metadata.put(key, value);
        }
    }

    private static void readContentLines(RequestInfo requestInfo, BufferedReader reader, String firstContentLine)
            throws IOException {
        Map<String, String> headers = requestInfo.getHeaders();
        String contentLengthStr = headers.get("Content-Length");

        System.out.println("[RequestParser] Content-Length header: " + contentLengthStr);
        System.out.println("[RequestParser] First content line: " + firstContentLine);

        byte[] contentBytes;
        if (contentLengthStr != null) {
            int contentLength = Integer.parseInt(contentLengthStr);
            System.out.println("[RequestParser] Reading with content length: " + contentLength);
            contentBytes = readContentLength(reader, contentLength, firstContentLine);
        } else {
            System.out.println("[RequestParser] No Content-Length found, reading until end");
            contentBytes = readFullContent(reader, firstContentLine);
        }

        if (contentBytes != null) {
            System.out.println("[RequestParser] Content bytes length: " + contentBytes.length);
        } else {
            System.out.println("[RequestParser] Content bytes is null");
        }

        requestInfo.setContent(contentBytes);
    }

    private static byte[] readContentLength(BufferedReader reader, int contentLength, String line)
            throws IOException {
        if (contentLength < 1000) {
            System.out.println("[RequestParser] Using simplified content reading for small content length");
            StringBuilder sb = new StringBuilder();

            if (line != null && !line.isEmpty()) {
                sb.append(line);
            }

            char[] buffer = new char[1024];
            int read;
            while (reader.ready() && (read = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, read);
                if (sb.length() >= contentLength) {
                    break;
                }
            }

            String content = sb.toString();
            if (content.length() > contentLength) {
                content = content.substring(0, contentLength);
            }

            System.out.println("[RequestParser] Read content: " + content);
            return content.getBytes();
        }

        char[] buffer = new char[contentLength];
        int bufferedChars = Math.min(line.length(), contentLength);
        line.getChars(0, bufferedChars, buffer, 0);
        int read;

        while (reader.ready() && bufferedChars < contentLength && (read = reader.read(buffer, bufferedChars, contentLength - bufferedChars)) != -1)  {
            bufferedChars += read;
        }

        return new String(buffer, 0, bufferedChars).getBytes();
    }

    private static byte[] readFullContent(BufferedReader reader, String line) throws IOException {
        StringBuilder content = new StringBuilder();
        content.append(line);

        String nextLine;
        while (reader.ready() && (nextLine = reader.readLine()) != null && !nextLine.isEmpty()) {
            content.append(nextLine);
        }

        return content.toString().getBytes();
    }

    // Inner class RequestInfo
    public static class RequestInfo {
        private String httpMethod; // e.g. GET, POST, DELETE
        private String uri; // e.g. /api/resource?id=123&name=test
        private String path; // /api/resource
        private String[] pathParts; // e.g. "api, resource"
        private Map<String, String> params; // e.g. {id=123, name=test}
        private Map<String, String> headers;
        private Map<String, String> metadata;
        private byte[] content;

        public RequestInfo() {
        }

        public String getHttpMethod() {
            return this.httpMethod;
        }

        public String getUri() {
            return this.uri;
        }

        public String getPath() {
            return this.path;
        }

        public String[] getPathParts() {
            return this.pathParts;
        }

        public Map<String, String> getParams() {
            return this.params;
        }

        public Map<String, String> getHeaders() {
            return this.headers;
        }

        public byte[] getContent() {
            return this.content;
        }

        public void setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setPathParts(String[] pathParts) {
            this.pathParts = pathParts;
        }

        public void setParams(Map<String, String> params) {
            this.params = params;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }
    }
}