package graph;

import configs.GenericConfig;
import server.RequestParser;
import servlets.Servlet;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ConfLoader implements Servlet {

    private static final String TEMP_UPLOAD_PATH = "uploaded.conf";

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        String content = extractConfigContent(ri);
        saveConfigToFile(content);
        clearTopicManager();
        GenericConfig config = createConfig();
    }

    private String extractConfigContent(RequestParser.RequestInfo ri) {
        return new String(ri.getContent(), StandardCharsets.UTF_8);
    }

    private void saveConfigToFile(String content) {
        try (FileWriter writer = new FileWriter(TEMP_UPLOAD_PATH)) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void clearTopicManager() {
        TopicManagerSingleton.get().clear();
    }

    private GenericConfig createConfig() {
        GenericConfig config = new GenericConfig();
        config.setConfFile(TEMP_UPLOAD_PATH);
        config.create();
        return config;
    }

    @Override
    public void close() throws IOException {
    }
}