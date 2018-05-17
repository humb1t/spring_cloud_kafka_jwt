package org.nipu.poc.springcloudkafkajwtservice.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.EnumMap;

public class MessageContainer<T> implements Serializable {
    private final EnumMap<HeaderTypes, String> headers = new EnumMap<>(HeaderTypes.class);
    private final T message;

    public MessageContainer(@JsonProperty("message") T message) {
        this.message = message;
    }

    public T getMessage() {
        return message;
    }

    public MessageContainer<T> addHeader(HeaderTypes header, String value) {
        headers.put(header, value);
        return this;
    }

    public String getHeader(HeaderTypes headerTypes){
        return headers.get(headerTypes);
    }

    @Override
    public String toString() {
        return "MessageContainer{" +
                "headers=" + headers +
                ", message=" + message +
                '}';
    }
}