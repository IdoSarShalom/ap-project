package configs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A generic config that reads agent definitions from a text file.
 * Each agent is defined by 3 lines:
 * 1) Fully-qualified class name (e.g., "graph.PlusAgent")
 * 2) Comma-separated list of subs (topics to subscribe)
 * 3) Comma-separated list of pubs (topics to publish)
 * <p>
 * We'll instantiate each agent reflectively, then wrap it in ParallelAgent.
 */
public class GenericConfig implements Config {

    private static final String PACKAGE_PREFIX = "main.java.graph.";
    private final List<Agent> createdAgents = new ArrayList<>();
    private String confFile;

    @Override
    public void create() {
        checkFileProvided(confFile);
        List<String> lines = readNonEmptyLines(confFile);
        validateLineCount(lines);
        parseAndInstantiateAgents(lines);
    }

    /**
     * If no file was provided, we cannot proceed.
     */
    private void checkFileProvided(String path) {
        if (path == null) {
            throw new IllegalStateException("No config file set. Invoke setConfFile() first.");
        }
    }

    private List<String> readNonEmptyLines(String filePath) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading config file: " + filePath, e);
        }

        return lines;
    }

    private void validateLineCount(List<String> lines) {
        if (lines.size() % 3 != 0) {
            throw new RuntimeException("Config file format error: total lines not multiple of 3");
        }
    }

    /**
     * Walks through the list in steps of 3 lines at a time, instantiating agents.
     * - line0 = class name
     * - line1 = comma-separated subs
     * - line2 = comma-separated pubs
     */
    private void parseAndInstantiateAgents(List<String> lines) {
        for (int i = 0; i < lines.size(); i += 3) {
            String agentType = lines.get(i).trim();
            String agentName = agentType.substring(agentType.lastIndexOf(".") + 1);
            String subsLine = lines.get(i + 1).trim();
            String pubsLine = lines.get(i + 2).trim();

            String[] subs = parseTopics(subsLine);
            String[] pubs = parseTopics(pubsLine);

            System.out.println("[GenericConfig] Creating agent: " + agentType +
                    " subscribing to " + subsLine + " and publishing to " + pubsLine);

            // Create the agent reflectively.
            Agent agent = instantiateAgent(agentName, subs, pubs);

            // Wrap it in a ParallelAgent (capacity=10 for example).
            ParallelAgent pAgent = new ParallelAgent(agent, 10);
            createdAgents.add(pAgent);
        }
    }

    /**
     * Splits a comma-separated string into an array of topics.
     */
    private String[] parseTopics(String line) {
        return (line == null || line.isEmpty()) ? new String[0] : line.split(",");
    }

    /**
     * Uses reflection to construct an Agent with a two-arg constructor:
     * Agent(String[] subs, String[] pubs)
     */
    private Agent instantiateAgent(String className, String[] subs, String[] pubs) {
        try {
            System.out.println("[GenericConfig] Looking for class: " + className);
            Class<?> clz = Class.forName(PACKAGE_PREFIX + className);
            System.out.println("[GenericConfig] Found class: " + clz.getName());
            return (Agent) clz
                    .getConstructor(String[].class, String[].class)
                    .newInstance(subs, pubs);
        } catch (ClassNotFoundException e) {
            System.err.println("[GenericConfig] Class not found: " + className);
            e.printStackTrace();
            throw new RuntimeException("Class not found: " + className, e);
        } catch (Exception e) {
            System.err.println("[GenericConfig] Failed to create agent: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create agent of type " + className, e);
        }
    }

    @Override
    public void close() {
        for (Agent a : createdAgents) {
            a.close();
        }
        createdAgents.clear();
    }

    public void setConfFile(String confFile) {
        this.confFile = confFile;
    }

    @Override
    public String getName() {
        return "Generic Config";
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
