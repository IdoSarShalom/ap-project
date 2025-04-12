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

        if (reader.ready()) {
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

        String httpCommand = firstLineParts[0].toUpperCase();
        String uri = firstLineParts[1];
        String resourceUri = getResourceUri(uri);
        String[] resourceUriParts = getResourceUriParts(resourceUri);
        String query = getQuery(uri);
        Map<String, String> params = getParams(query);

        setRequestInfo(requestInfo, httpCommand, uri, resourceUri, resourceUriParts, params);
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

    private static String getResourceUri(String uri) {
        return uri.contains("?") ? uri.substring(0, uri.indexOf('?')) : uri;
    }

    private static String[] getResourceUriParts(String resourceUri) {
        return Arrays.stream(resourceUri.split(File.separator))
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

    private static void setRequestInfo(RequestInfo requestInfo, String httpCommand, String uri, String resourceUri,
                                       String[] resourceUriParts, Map<String, String> params) {
        requestInfo.setHttpCommand(httpCommand);
        requestInfo.setUri(uri);
        requestInfo.setResourceUri(resourceUri);
        requestInfo.setUriSegments(resourceUriParts);
        requestInfo.setParameters(params);
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

        byte[] contentBytes;
        if (contentLengthStr != null) {
            int contentLength = Integer.parseInt(contentLengthStr);
            contentBytes = readContentLength(reader, contentLength, firstContentLine);
        } else {
            contentBytes = readFullContent(reader, firstContentLine);
        }

        requestInfo.setContent(contentBytes);
    }

    private static byte[] readContentLength(BufferedReader reader, int contentLength, String line)
            throws IOException {
        if (contentLength < 1000) {
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

            return content.getBytes();
        }

        char[] buffer = new char[contentLength];
        int bufferedChars = Math.min(line.length(), contentLength);
        line.getChars(0, bufferedChars, buffer, 0);
        int read;

        while (reader.ready() && bufferedChars < contentLength && (read = reader.read(buffer, bufferedChars, contentLength - bufferedChars)) != -1) {
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

    public static class RequestInfo {
        private String httpCommand; // e.g. GET, POST, DELETE
        private String uri; // e.g. /api/resource?id=123&name=test
        private String resourceUri; // /api/resource
        private String[] uriSegments; // e.g. "api, resource"
        private Map<String, String> parameters; // e.g. {id=123, name=test}
        private Map<String, String> headers;
        private Map<String, String> metadata;
        private byte[] content;

        public RequestInfo() {
        }

        public String getHttpCommand() {
            return this.httpCommand;
        }

        public String getUri() {
            return this.uri;
        }

        public String getResourceUri() {
            return this.resourceUri;
        }

        public String[] getUriSegments() {
            return this.uriSegments;
        }

        public Map<String, String> getParameters() {
            return this.parameters;
        }

        public Map<String, String> getHeaders() {
            return this.headers;
        }

        public byte[] getContent() {
            return this.content;
        }

        public void setHttpCommand(String httpCommand) {
            this.httpCommand = httpCommand;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public void setResourceUri(String resourceUri) {
            this.resourceUri = resourceUri;
        }

        public void setUriSegments(String[] uriSegments) {
            this.uriSegments = uriSegments;
        }

        public void setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
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