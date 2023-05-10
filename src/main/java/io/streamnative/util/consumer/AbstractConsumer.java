package io.streamnative.util.consumer;

import io.streamnative.simulator.condition.Condition;
import io.streamnative.util.PulsarClientThread;
import io.streamnative.util.consumer.handlers.error.ErrorHandler;
import io.streamnative.util.consumer.handlers.success.MessageHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.pulsar.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractConsumer<T> extends PulsarClientThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConsumer.class);

    protected Consumer<T> consumer;

    protected ConsumerBuilder<T> consumerBuilder;

    protected String topicName;

    protected String subscriptionName;

    protected Condition errorCondition;

    protected Condition processingDelayCondition;

    protected ErrorHandler errorHandler;

    protected MessageHandler<T> messageHandler;

    protected Map<String, Integer> processingCountMap = new HashMap<>();

    // 5 minutes
    protected final AtomicLong maxRuntime = new AtomicLong(60 * 5 * 1000);

    public AbstractConsumer() {
        super();
    }

    public AbstractConsumer(PulsarClient client, String topicName, String subscriptionName) {
        super(client, topicName);
        this.topicName = topicName;
        this.subscriptionName = subscriptionName;
    }

    protected abstract ConsumerBuilder<T> createConsumerBuilder();

    protected Consumer<T> getConsumer() {
        if (consumer == null) {
            try {
                ConsumerBuilder<T> builder = getConsumerBuilder();
                consumer = builder.subscribe();
                LOGGER.info(String.format("Starting consumer on [Topic: %s, Subscription : %s]",
                        this.topicName, consumer.getSubscription()));
            } catch (PulsarClientException e) {
                LOGGER.error(String.format("Unable to consume from", this.topicName), e);
                throw new RuntimeException(e);
            }
        }
        return consumer;
    }

    public ConsumerBuilder<T> getConsumerBuilder() {
        if (consumerBuilder == null) {
            consumerBuilder = this.createConsumerBuilder();
        }
        return consumerBuilder;
    }

    public void updateConsumerBuilder(ConsumerBuilder<T> builder) {
        this.consumerBuilder = builder;
    }

    public void setMaxRuntime(long max) {
        if (max > 0) {
            this.maxRuntime.set(max);
        }
    }

    public void run() {

        processingCountMap.clear();
        running = true;

        try {
            long startTime = System.currentTimeMillis();

            while (running) {
                // Wait longer than negativeAckRedeliveryDelay
                Message<T> msg = getConsumer().receive(1, TimeUnit.MINUTES);

                if (msg == null) {
                    LOGGER.info("No more messages");
                    halt();
                    break;
                }

                LOGGER.info(String.format("Received message [value: %s, Publish Time: %d]",
                        msg.getValue(), msg.getPublishTime()));

                if (processingCountMap.containsKey(msg.getValue()) ) {
                    processingCountMap.put(msg.getValue().toString(),
                            processingCountMap.get(msg.getValue())+1);
                } else {
                    processingCountMap.put(msg.getValue().toString(), 1);
                }

                if (errorCondition == null || !errorCondition.check(msg)) {
                    // Responsible for ack-ing the message
                    if (processingDelayCondition != null && processingDelayCondition.check(msg)) {
                        while (processingDelayCondition.exists()) {
                            LOGGER.info("Waiting for processing delay to clear");
                            Thread.sleep(1000);
                        }
                    }

                    messageHandler.handleMessage(msg, consumer);

                } else if (errorHandler != null) {
                    // Responsible for nack-ing, retrying, etc.
                    errorHandler.handle(msg, consumer);
                }

                if (System.currentTimeMillis() - startTime > maxRuntime.get()) {
                    LOGGER.debug(String.format("Stopping consumer after %d milliseconds",
                            maxRuntime.get()));
                    running = false;
                }
            }

            messageHandler.close();

        } catch (Exception e) {
            LOGGER.error("Error Consuming", e);
        } finally {
            this.close();
            latch.countDown();
        }
    }

    public void close() {
        try {
            LOGGER.debug("Closing the consumer");
            if (consumer != null && getConsumer().isConnected()) {
                getConsumer().unsubscribe();
                getConsumer().close();
            }
        } catch (PulsarClientException e) {
            LOGGER.error("Unable to close consumer", e);
        } finally {
            this.consumer = null;
        }
    }

}