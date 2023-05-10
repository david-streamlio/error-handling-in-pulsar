package io.streamnative.simulator.condition.trigger;

import org.apache.pulsar.client.api.Message;

public class MessageContentTrigger<T> implements Trigger {

    private final T errorContent;

    public MessageContentTrigger(T errorContent) {
        this.errorContent = errorContent;
    }

    @Override
    public boolean isTriggered(Message msg) {
        return (msg == null) ? false : (msg.getValue().equals(errorContent));
    }
}
