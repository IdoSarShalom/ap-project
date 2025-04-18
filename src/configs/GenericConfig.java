package configs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GenericConfig implements Config {

    private static final String PACKAGE_PREFIX = "graph";
    private static final Pattern TOPIC_PATTERN = Pattern.compile("^[A-Z]$");

    private final List<Agent> instantiatedAgents = new ArrayList<>();
    private final List<String> agentTypes = new ArrayList<>();
    private final List<String> subscriptionLines = new ArrayList<>();
    private final List<String> publicationLines = new ArrayList<>();
    private String configFilePath;

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
            agentTypes.add(lines.get(i));
            subscriptionLines.add(lines.get(i + 1));
            publicationLines.add(lines.get(i + 2));
        }
    }

    private void validateConfigLists() {
        for (int i = 0; i < agentTypes.size(); i++) {
            String agentType = agentTypes.get(i);
            String subsLine = subscriptionLines.get(i);
            String pubsLine = publicationLines.get(i);
            validateAgentType(agentType, i);
            validateTopicLine(subsLine, "Subscription", i);
            validateTopicLine(pubsLine, "Publication", i);

            if (agentType.equals("IncAgent")) {
                validateIncAgentConfig(subsLine, i);
            } else if (agentType.equals("PlusAgent")) {
                validatePlusAgentConfig(subsLine, i);
            }
        }
    }

    private void validateIncAgentConfig(String subsLine, int index) {
        String[] listenTopics = parseTopics(subsLine);
        if (listenTopics.length != 1) {
            throw new RuntimeException("IncAgent at line " + (index * 3 + 1) + " must have exactly one listen topic. Found: " + listenTopics.length);
        }
    }

    private void validatePlusAgentConfig(String subsLine, int index) {
        String[] listenTopics = parseTopics(subsLine);
        if (listenTopics.length != 2) {
            throw new RuntimeException("PlusAgent at line " + (index * 3 + 1) + " must have exactly two listen topics. Found: " + listenTopics.length);
        }
    }

    private void validateAgentType(String agentType, int index) {
        if (!agentType.equals(agentType.trim())) {
            throw new RuntimeException("Agent type line " + (index * 3 + 1) + " has leading or trailing spaces: '" + agentType + "'");
        }

        try {
            Class<?> agentClass = Class.forName(String.format("%s.%s", PACKAGE_PREFIX, agentType));
            if (!Agent.class.isAssignableFrom(agentClass)) {
                throw new RuntimeException("Class " + agentType + " at line " + (index * 3 + 1) + " does not implement Agent interface.");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Agent class not found at line " + (index * 3 + 1) + ": " + agentType);
        }
    }

    private void validateTopicLine(String line, String lineType, int index) {
        if (line.isEmpty()) {
            throw new RuntimeException(lineType + " line " + (index * 3 + (lineType.equals("Subscription") ? 2 : 3)) + " is empty.");
        }

        if (!line.equals(line.trim())) {
            throw new RuntimeException(lineType + " line " + (index * 3 + (lineType.equals("Subscription") ? 2 : 3)) + " has leading or trailing spaces: '" + line + "'");
        }

        String[] topics = line.split(",");

        for (String topic : topics) {
            if (!TOPIC_PATTERN.matcher(topic).matches()) {
                throw new RuntimeException(lineType + " line " + (index * 3 + (lineType.equals("Subscription") ? 2 : 3)) + " contains invalid topic format: '" + topic + "'. Topic must be exactly one uppercase letter (A-Z).");
            }
            if (topic.contains(" ")) {
                throw new RuntimeException(lineType + " line " + (index * 3 + (lineType.equals("Subscription") ? 2 : 3)) + " contains spaces in topic: '" + topic + "'");
            }
            if (topic.isEmpty()) {
                throw new RuntimeException(lineType + " line " + (index * 3 + (lineType.equals("Subscription") ? 2 : 3)) + " contains empty topic in: '" + line + "'");
            }
        }

    }

    private void instantiateAgentsFromLists() {
        for (int i = 0; i < agentTypes.size(); i++) {
            String agentType = agentTypes.get(i);
            String subsLine = subscriptionLines.get(i);
            String pubsLine = publicationLines.get(i);
            String[] subs = parseTopics(subsLine);
            String[] pubs = parseTopics(pubsLine);
            Agent agent = createAgentInstance(agentType, subs, pubs);
            ParallelAgent parallelAgent = new ParallelAgent(agent, 10);
            instantiatedAgents.add(parallelAgent);
        }
    }

    private String[] parseTopics(String line) {
        return (line.isEmpty()) ? new String[0] : line.split(",");
    }

    private Agent createAgentInstance(String className, String[] subs, String[] pubs) {
        try {
            Class<?> agentClass = Class.forName(String.format("%s.%s", PACKAGE_PREFIX, className));
            Constructor<?> constructor = agentClass.getConstructor(String[].class, String[].class);

            return (Agent) constructor.newInstance(subs, pubs);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
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
