package test;

import java.util.*;
import java.nio.charset.StandardCharsets;

public final class Message {

    public final byte[] data;
    public final String asText;
    public final double asDouble;
    public final Date date;

    public Message(byte[] data) {
        this(new String(data, StandardCharsets.UTF_8));
    }

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

    public Message(double asDouble) {
        this(Double.toString(asDouble));
    }
}