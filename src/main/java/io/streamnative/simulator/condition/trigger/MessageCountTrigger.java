package io.streamnative.simulator.condition.trigger;

import org.apache.pulsar.client.api.Message;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Used for triggering an error on every Nth message.
 */
public class MessageCountTrigger implements Trigger {

    private AtomicInteger msgCounter = new AtomicInteger(0);

    private final int moduloTriggerValue;

    public MessageCountTrigger(int moduloTriggerValue) {
        this.moduloTriggerValue = moduloTriggerValue;
    }

    @Override
    public boolean isTriggered(Message msg) {
        return (msgCounter.incrementAndGet() % moduloTriggerValue == 0);
    }
}
