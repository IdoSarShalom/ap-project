package configs;

import test.Agent;
import test.ParallelAgent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class GenericConfig implements Config {

    private String configFilePath;
    private final List<Agent> instantiatedAgents = new ArrayList<>();
    private final List<String> agentTypes = new ArrayList<>();
    private final List<String> subscriptionLines = new ArrayList<>();
    private final List<String> publicationLines = new ArrayList<>();

    public void create() {
        ensureConfigFileIsSet();
        List<String> lines = readLinesFromFile();
        ensureValidLineCount(lines);
        populateConfigLists(lines);
        validateConfigLists();
        instantiateAgentsFromLists();
    }

    private void ensureConfigFileIsSet() {

        if (configFilePath == null) {
            throw new IllegalStateException("No config file set. Invoke setConfFile() first.");
        }
    }

    private List<String> readLinesFromFile() {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(configFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading config file: " + configFilePath, e);
        }
        return lines;
    }

    private void ensureValidLineCount(List<String> lines) {

        if (lines.size() % 3 != 0) {
            throw new RuntimeException("Config file format error: total lines not multiple of 3");
        }
    }

    private void populateConfigLists(List<String> lines) {
        for (int i = 0; i < lines.size(); i += 3) {
            String agentType = lines.get(i);
            String subsLine = lines.get(i + 1);
            String pubsLine = lines.get(i + 2);
            agentTypes.add(agentType);
            subscriptionLines.add(subsLine);
            publicationLines.add(pubsLine);
        }
    }

    private void validateConfigLists() {

        if (agentTypes.size() != subscriptionLines.size() || agentTypes.size() != publicationLines.size()) {
            throw new RuntimeException("Configuration lists have inconsistent sizes.");
        }
        for (int i = 0; i < agentTypes.size(); i++) {
            String agentType = agentTypes.get(i);
            String subsLine = subscriptionLines.get(i);
            String pubsLine = publicationLines.get(i);
            validateAgentType(agentType, i);
            validateTopicLine(subsLine, "Subscription", i);
            validateTopicLine(pubsLine, "Publication", i);
        }
    }

    private void validateAgentType(String agentType, int index) {

        if (agentType.chars().filter(ch -> ch == '.').count() < 2) {
            throw new RuntimeException("Agent type line " + (index * 3 + 1) + " does not specify a valid package structure (requires at least two dots): '" + agentType + "'");
        }
        String className = extractClassName(agentType);

        if (!className.equals(className.trim())) {
            throw new RuntimeException("Agent type line " + (index * 3 + 1) + " has leading or trailing spaces in class name: '" + className + "'");
        }

        try {
            Class<?> agentClass = Class.forName(className);
            if (!Agent.class.isAssignableFrom(agentClass)) {
                throw new RuntimeException("Class " + className + " at line " + (index * 3 + 1) + " does not implement Agent interface.");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Agent class not found at line " + (index * 3 + 1) + ": " + className);
        }
    }

    private void validateTopicLine(String line, String lineType, int index) {

        if (!line.equals(line.trim())) {
            throw new RuntimeException(lineType + " line " + (index * 3 + (lineType.equals("Subscription") ? 2 : 3)) + " has leading or trailing spaces: '" + line + "'");
        }

        if (!line.isEmpty()) {
            String[] topics = line.split(",");
            for (String topic : topics) {

                if (topic.contains(" ")) {
                    throw new RuntimeException(lineType + " line " + (index * 3 + (lineType.equals("Subscription") ? 2 : 3)) + " contains spaces in topic: '" + topic + "'");
                }

                if (topic.isEmpty()) {
                    throw new RuntimeException(lineType + " line " + (index * 3 + (lineType.equals("Subscription") ? 2 : 3)) + " contains empty topic in: '" + line + "'");
                }
            }
        }
    }

    private void instantiateAgentsFromLists() {
        for (int i = 0; i < agentTypes.size(); i++) {
            String agentType = agentTypes.get(i);
            String subsLine = subscriptionLines.get(i);
            String pubsLine = publicationLines.get(i);
            String className = extractClassName(agentType);
            String[] subs = parseTopics(subsLine);
            String[] pubs = parseTopics(pubsLine);
            Agent agent = createAgentInstance(className, subs, pubs);
            ParallelAgent parallelAgent = new ParallelAgent(agent, 10);
            instantiatedAgents.add(parallelAgent);
        }
    }

    private String extractClassName(String agentType) {
        return agentType.substring(agentType.indexOf(".") + 1);
    }

    private String[] parseTopics(String line) {
        return (line.isEmpty()) ? new String[0] : line.split(",");
    }

    private Agent createAgentInstance(String className, String[] subs, String[] pubs) {

        try {
            Class<?> agentClass = Class.forName(className);
            Constructor<?> constructor = agentClass.getConstructor(String[].class, String[].class);
            return (Agent) constructor.newInstance(subs, pubs);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to instantiate agent: " + className, e);
        }
    }

    public void close() {
        for (Agent agent : instantiatedAgents) {
            agent.close();
        }
        instantiatedAgents.clear();
    }

    public void setConfFile(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public int getVersion() {
        return 1;
    }
}