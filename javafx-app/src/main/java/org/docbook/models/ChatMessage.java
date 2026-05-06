package org.docbook.models;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ChatMessage {

    public enum MessageType {
        USER,
        ASSISTANT
    }

    private String content;
    private MessageType type;
    private long timestamp;

    public ChatMessage(String content, MessageType type) {
        this.content = content;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public String getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSenderLabel() {
        return type == MessageType.USER ? "You" : "DocBook Assistant";
    }

    public String getFormattedTime() {
        LocalDateTime ldt = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.systemDefault()
        );
        return ldt.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    @Override
    public String toString() {
        return "[" + type + "] " + content;
    }
}