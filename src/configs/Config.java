package configs;

/**
 * Interface for configuration handlers in the pub/sub system.
 * <p>
 * Classes implementing this interface are responsible for managing the configuration
 * of the pub/sub system, including loading configuration files, instantiating agents,
 * and managing the lifecycle of those agents.
 * <p>
 * The configuration system allows for dynamic creation and configuration of agent
 * networks without requiring code changes.
 */
public interface Config {
    /**
     * Creates and initializes agents based on the configuration.
     * <p>
     * This method reads the configuration, validates it, and instantiates
     * the agents according to the specified configuration parameters.
     */
    void create();

    /**
     * Gets the name of this configuration.
     * 
     * @return The name of the configuration, typically the implementing class name
     */
    String getName();

    /**
     * Gets the version of this configuration.
     * 
     * @return The version number of the configuration
     */
    int getVersion();

    /**
     * Cleans up resources used by this configuration.
     * <p>
     * This method should be called when the configuration is no longer needed.
     * It typically closes all agents and releases any resources they hold.
     */
    void close();
}