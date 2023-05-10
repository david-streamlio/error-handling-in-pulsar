package io.streamnative.util.reader;

import io.streamnative.util.consumer.handlers.success.MessageHandler;
import org.apache.pulsar.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class AbstractTopicReader<T> {

    private static final String NEWLINE = "\n";

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractTopicReader.class);
    protected final PulsarClient pulsarClient;

    protected final String topicName;

    protected Reader<T> reader;

    public AbstractTopicReader(PulsarClient pulsarClient, String topicName) {
        this.pulsarClient = pulsarClient;
        this.topicName = topicName;
    }

    public abstract Reader<T> getReader(long rollback) throws PulsarClientException;

    public void displayContents(long startTime) {
        LOGGER.info(getTopicContents(startTime));
    }

    public String getTopicContents(long startTime) {
        StringBuilder stringBuilder = new StringBuilder(1024);

        stringBuilder.append(String.format("Output Topic [%s] contents:", topicName)).append(NEWLINE);

        try {
            // Go back to the start time plus an additional 20 seconds
            getReader((System.currentTimeMillis() - startTime) + 20000);
            while (reader.hasMessageAvailable()) {
                Message msg = reader.readNext();

                if (msg != null) {
                   stringBuilder.append(String.format("[value : %s, Original Publish Time: %s, Processing Time: %s]",
                            msg.getValue() != null ? msg.getValue().toString() : "null",
                            msg.hasProperty(MessageHandler.ORIGINAL_PUBLISH_TIME) ?
                                    msg.getProperty(MessageHandler.ORIGINAL_PUBLISH_TIME) : "Unknown",
                            msg.hasProperty(MessageHandler.PROCESSING_TIME) ?
                                    msg.getProperty(MessageHandler.PROCESSING_TIME) + " ms" : "Unknown"));
                   stringBuilder.append(NEWLINE);
                }
            }
            close();
        } catch (final Exception ex) {
            LOGGER.error("Unable to read topic", ex);
        }

        return stringBuilder.toString();
    }

    protected void close() throws IOException {
        LOGGER.debug(String.format("Closing reader on topic %s", topicName));
        reader.close();
        reader = null;
    }
}
