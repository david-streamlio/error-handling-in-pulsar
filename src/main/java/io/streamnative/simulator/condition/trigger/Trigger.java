package io.streamnative.simulator.condition.trigger;

import org.apache.pulsar.client.api.Message;

public interface Trigger {

    boolean isTriggered(Message msg);
}
