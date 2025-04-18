package configs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A configuration implementation that loads agent configuration from a text file.
 * <p>
 * This class implements the Config interface and provides functionality to read
 * agent configurations from a text file, validate them, and instantiate the
 * appropriate agent objects.
 * <p>
 * The configuration file format consists of groups of three lines:
 * <ol>
 *   <li>The agent type (must be a valid class name in the graph package)</li>
 *   <li>A comma-separated list of topics to subscribe to</li>
 *   <li>A comma-separated list of topics to publish to</li>
 * </ol>
 * <p>
 * Each agent is wrapped in a ParallelAgent to allow concurrent message processing.
 */
public class GenericConfig implements Config {

    /** The package prefix for agent classes */
    private static final String PACKAGE_PREFIX = "graph";
    
    /** Pattern for valid topic names (single uppercase letter) */
    private static final Pattern TOPIC_PATTERN = Pattern.compile("^[A-Z]$");

    /** List of instantiated agents */
    private final List<Agent> instantiatedAgents = new ArrayList<>();
    
    /** List of agent type names from the configuration */
    private final List<String> agentTypes = new ArrayList<>();
    
    /** List of subscription lines from the configuration */
    private final List<String> subscriptionLines = new ArrayList<>();
    
    /** List of publication lines from the configuration */
    private final List<String> publicationLines = new ArrayList<>();
    
    /** Path to the configuration file */
    private String configFilePath;

    /**
     * Creates and configures agents based on the configuration file.
     * <p>
     * This method reads the configuration file, parses it, validates the
     * configuration, and instantiates the specified agents.
     * 
     * @throws RuntimeException if the configuration file is invalid or if there
     *         are errors instantiating the agents
     * @throws IllegalStateException if no configuration file has been set
     */
    public void create() {
        ensureConfigFileIsSet();
        List<String> lines = readLinesFromFile();
        ensureValidLineCount(lines);
        populateConfigLists(lines);
        validateConfigLists();
        instantiateAgentsFromLists();
    }

    /**
     * Ensures that a configuration file path has been set.
     * 
     * @throws IllegalStateException if no configuration file has been set
     */
    private void ensureConfigFileIsSet() {
        if (configFilePath == null) {
            throw new IllegalStateException("No config file set. Invoke setConfFile() first.");
        }
    }

    /**
     * Reads all lines from the configuration file.
     * 
     * @return List of lines from the configuration file
     * @throws RuntimeException if an error occurs while reading the file
     */
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

    /**
     * Ensures that the number of lines in the configuration file is valid.
     * <p>
     * The number of lines must be a multiple of 3, as each agent requires
     * three lines of configuration.
     * 
     * @param lines The lines from the configuration file
     * @throws RuntimeException if the number of lines is not a multiple of 3
     */
    private void ensureValidLineCount(List<String> lines) {
        if (lines.size() % 3 != 0) {
            throw new RuntimeException("Config file format error: total lines not multiple of 3");
        }
    }

    /**
     * Populates the configuration lists from the lines read from the file.
     * <p>
     * Every three lines from the file correspond to one agent's configuration:
     * agent type, subscription topics, and publication topics.
     * 
     * @param lines The lines from the configuration file
     */
    private void populateConfigLists(List<String> lines) {
        for (int i = 0; i < lines.size(); i += 3) {
            agentTypes.add(lines.get(i));
            subscriptionLines.add(lines.get(i + 1));
            publicationLines.add(lines.get(i + 2));
        }
    }

    /**
     * Validates the configuration lists to ensure they contain valid values.
     * <p>
     * This method checks that agent types are valid, topic names are valid,
     * and that the correct number of topics is specified for each agent type.
     * 
     * @throws RuntimeException if the configuration is invalid
     */
    public void validateConfigLists() {
        for (int i = 0; i < agentTypes.size(); i++) {
            String agentType = agentTypes.get(i);
            String subsLine = subscriptionLines.get(i);
            String pubsLine = publicationLines.get(i);

            validateAgentType(agentType, i);
            validateTopicLine(subsLine, "Subscription", i);
            validateTopicLine(pubsLine, "Publication", i);

            if (agentType.equals("IncAgent") || agentType.equals("DecAgent") ||
                    agentType.equals("NegAgent") || agentType.equals("AbsAgent") ||
                    agentType.equals("DoubleAgent")) {
                validateSingleInputAgentSubscription(subsLine, i, agentType);
                validateSingleInputAgentPublication(pubsLine, i, agentType);
            } else if (agentType.equals("PlusAgent") || agentType.equals("MultAgent") ||
                    agentType.equals("MinusAgent") || agentType.equals("MaxAgent") ||
                    agentType.equals("MinAgent") || agentType.equals("AvgAgent")) {
                validateDoubleInputAgentSubscription(subsLine, i, agentType);
                validateDoubleInputAgentPublication(pubsLine, i, agentType);
            }
        }
    }

    /**
     * Validates that a single-input agent has exactly one subscription topic.
     * 
     * @param subsLine The subscription line to validate
     * @param index The index of the agent in the configuration
     * @param agentType The type of the agent
     * @throws RuntimeException if the subscription line is invalid
     */
    private void validateSingleInputAgentSubscription(String subsLine, int index, String agentType) {
        String[] listenTopics = parseTopics(subsLine);
        if (listenTopics.length != 1) {
            throw new RuntimeException(agentType + " at line " + (index * 3 + 2) + " must have exactly one listen topic. Found: " + listenTopics.length);
        }
    }

    /**
     * Validates that a single-input agent has exactly one publication topic.
     * 
     * @param pubsLine The publication line to validate
     * @param index The index of the agent in the configuration
     * @param agentType The type of the agent
     * @throws RuntimeException if the publication line is invalid
     */
    private void validateSingleInputAgentPublication(String pubsLine, int index, String agentType) {
        String[] publishTopics = parseTopics(pubsLine);
        if (publishTopics.length != 1) {
            throw new RuntimeException(agentType + " at line " + (index * 3 + 3) + " must have exactly one publish topic. Found: " + publishTopics.length);
        }
    }

    /**
     * Validates that a double-input agent has exactly two subscription topics.
     * 
     * @param subsLine The subscription line to validate
     * @param index The index of the agent in the configuration
     * @param agentType The type of the agent
     * @throws RuntimeException if the subscription line is invalid
     */
    private void validateDoubleInputAgentSubscription(String subsLine, int index, String agentType) {
        String[] listenTopics = parseTopics(subsLine);
        if (listenTopics.length != 2) {
            throw new RuntimeException(agentType + " at line " + (index * 3 + 2) + " must have exactly two listen topics. Found: " + listenTopics.length);
        }
    }

    /**
     * Validates that a double-input agent has exactly one publication topic.
     * 
     * @param pubsLine The publication line to validate
     * @param index The index of the agent in the configuration
     * @param agentType The type of the agent
     * @throws RuntimeException if the publication line is invalid
     */
    private void validateDoubleInputAgentPublication(String pubsLine, int index, String agentType) {
        String[] publishTopics = parseTopics(pubsLine);
        if (publishTopics.length != 1) {
            throw new RuntimeException(agentType + " at line " + (index * 3 + 3) + " must have exactly one publish topic. Found: " + publishTopics.length);
        }
    }

    /**
     * Validates an agent type from the configuration.
     * <p>
     * Checks that the agent type does not have leading or trailing spaces,
     * that the corresponding class exists, and that it implements the Agent interface.
     * 
     * @param agentType The agent type to validate
     * @param index The index of the agent in the configuration
     * @throws RuntimeException if the agent type is invalid
     */
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

    /**
     * Validates a topic line from the configuration.
     * <p>
     * Checks that the line is not empty, does not have leading or trailing spaces,
     * does not end with a comma, and that all topics have valid format.
     * 
     * @param line The topic line to validate
     * @param lineType The type of the line ("Subscription" or "Publication")
     * @param index The index of the agent in the configuration
     * @throws RuntimeException if the topic line is invalid
     */
    private void validateTopicLine(String line, String lineType, int index) {
        if (line.isEmpty()) {
            throw new RuntimeException(lineType + " line " + (index * 3 + (lineType.equals("Subscription") ? 2 : 3)) + " is empty.");
        }

        if (!line.equals(line.trim())) {
            throw new RuntimeException(lineType + " line " + (index * 3 + (lineType.equals("Subscription") ? 2 : 3)) + " has leading or trailing spaces: '" + line + "'");
        }

        if (line.endsWith(",")) {
            throw new RuntimeException(lineType + " line " + (index * 3 + (lineType.equals("Subscription") ? 2 : 3)) + " ends with a comma: '" + line + "'");
        }

        String[] topics = parseTopics(line);

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

    /**
     * Instantiates agents from the configuration lists.
     * <p>
     * Creates an instance of each agent type with the specified subscription
     * and publication topics, then wraps it in a ParallelAgent.
     */
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

    /**
     * Parses a comma-separated list of topics.
     * 
     * @param line The line containing comma-separated topics
     * @return An array of topic names
     */
    private String[] parseTopics(String line) {
        return line.split(",");
    }

    /**
     * Creates an instance of an agent with the specified configuration.
     * 
     * @param className The name of the agent class to instantiate
     * @param subs The topics to subscribe to
     * @param pubs The topics to publish to
     * @return A new agent instance
     * @throws RuntimeException if the agent cannot be instantiated
     */
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

    /**
     * Cleans up resources used by this configuration.
     * <p>
     * Closes all instantiated agents and clears the list.
     */
    public void close() {
        for (Agent agent : instantiatedAgents) {
            agent.close();
        }
        instantiatedAgents.clear();
    }

    /**
     * Sets the path to the configuration file.
     * <p>
     * This method must be called before invoking create().
     * 
     * @param configFilePath The path to the configuration file
     */
    public void setConfFile(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    /**
     * Gets the name of this configuration.
     * 
     * @return The simple class name of this configuration
     */
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Gets the version of this configuration.
     * 
     * @return The configuration version (always 1)
     */
    public int getVersion() {
        return 1;
    }
}
