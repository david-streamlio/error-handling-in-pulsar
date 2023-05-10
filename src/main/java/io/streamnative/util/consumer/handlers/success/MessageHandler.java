package io.streamnative.util.consumer.handlers.success;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClientException;

import java.io.Closeable;

public interface MessageHandler<T> extends Closeable {

    String PROCESSING_TIME = "Total Processing Time";
    String ORIGINAL_PUBLISH_TIME = "Original Publish Time";
    void handleMessage(Message<T> msg, Consumer<T> consumer) throws PulsarClientException;

}
