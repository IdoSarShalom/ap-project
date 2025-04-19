package graph;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Represents a message in the pub/sub system.
 * <p>
 * A message is an immutable data container that can be published to topics.
 * It stores data in multiple formats (byte array, string, and double) to facilitate
 * easy access depending on the consumer's needs. Each message also captures
 * the creation timestamp.
 */
public final class Message {

    /** The raw byte data of the message */
    public final byte[] data;
    
    /** The message content as a text string */
    public final String asText;
    
    /** The message content as a double value (or NaN if not parseable as a number) */
    public final double asDouble;
    
    /** The timestamp when this message was created */
    public final Date date;

    /**
     * Creates a new message from a byte array.
     * <p>
     * The byte array is converted to a string using UTF-8 encoding.
     * 
     * @param data The byte data for the message
     */
    public Message(byte[] data) {
        this(new String(data, StandardCharsets.UTF_8));
    }

    /**
     * Creates a new message from a string.
     * <p>
     * The string is also parsed as a double if possible.
     * 
     * @param text The text content for the message
     */
    public Message(String text) {
        this.data = text.getBytes(StandardCharsets.UTF_8);
        this.asText = text;
        double asDouble;

        try {
            asDouble = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            asDouble = Double.NaN;
        }

        this.asDouble = asDouble;
        this.date = new Date();
    }

    /**
     * Creates a new message from a double value.
     * <p>
     * The double is converted to a string representation.
     * 
     * @param asDouble The double value for the message
     */
    public Message(double asDouble) {
        this(Double.toString(asDouble));
    }
}