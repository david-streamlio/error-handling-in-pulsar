package io.streamnative.util.consumer.handlers.error;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
public class ReconsumeLaterStrategy implements ErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReconsumeLaterStrategy.class);
    private long delayTime;

    private TimeUnit delayTimeUnit;

    public ReconsumeLaterStrategy(long delay, TimeUnit unit) {
        this.delayTime = delay;
        this.delayTimeUnit = unit;
    }

    @Override
    public void handle(Message msg, Consumer consumer) {
        try {
            LOGGER.info(String.format("Marking message [value: %s] for re-consumption in %d %s",
                    msg.getValue().toString(), delayTime, delayTimeUnit.name()));

            consumer.reconsumeLater(msg, delayTime, delayTimeUnit);
        } catch (PulsarClientException e) {
            LOGGER.error("Unable to reconsume later", e);
        }
    }
}
