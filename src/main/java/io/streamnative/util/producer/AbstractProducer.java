package io.streamnative.util.producer;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import io.streamnative.util.PulsarClientThread;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.pulsar.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractProducer<T> extends PulsarClientThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProducer.class);

    protected static String[] RANDOM_KEYS = {RandomStringUtils.randomNumeric(5),
            RandomStringUtils.randomNumeric(5),
            RandomStringUtils.randomNumeric(5),
            RandomStringUtils.randomNumeric(5)};

    protected static final Random rnd = new Random();

    protected static String getRandomKey() {
        return RANDOM_KEYS[rnd.nextInt(RANDOM_KEYS.length)];
    }

    protected Producer<T> producer;

    protected final AtomicInteger msgCount = new AtomicInteger(0);

    public AbstractProducer(PulsarClient client, String topicName) throws PulsarClientException {
        super(client, topicName);
    }

    public void setMsgCount(int count) {
        if (count > 0) {
            this.msgCount.set(count);
        }
    }

    protected abstract Producer<T> createProducer();

    protected abstract T generateMessageContent();

    public void run() {
        running = true;
        int counter = 0;

        while (running && counter++ < this.msgCount.get()) {
            try {
                TypedMessageBuilder<T> builder = getProducer()
                        .newMessage()
                        .key(getRandomKey())
                        .value(generateMessageContent());
                builder.send();

                Thread.sleep(1000);
            } catch (final Exception ex) {
                LOGGER.error("Error producing ", ex);
            }
        }

        this.close();
        latch.countDown();
    }

    public void close() {
        try {
            LOGGER.debug("Closing the producer");
            getProducer().close();
        } catch (PulsarClientException e) {
            LOGGER.error("Unable to close producer", e);
        } finally {
            this.producer = null;
        }
    }

    private Producer<T> getProducer() {
        if (this.producer == null) {
            LOGGER.info(String.format("Creating producer on %s", this.topicName));
            this.producer = createProducer();
        }
        return this.producer;
    }
}