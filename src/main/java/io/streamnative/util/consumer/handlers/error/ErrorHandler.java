package io.streamnative.util.consumer.handlers.error;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;

public interface ErrorHandler {
    void handle(Message msg, Consumer consumer);
}
