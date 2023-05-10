package io.streamnative.simulator.condition.trigger;

import org.apache.pulsar.client.api.Message;

import java.util.Random;

public class RandomTrigger implements Trigger {

    private final Random rnd = new Random();
    private final float errorProbability;

    public RandomTrigger(float errorProbability) {
        this.errorProbability = errorProbability;
    }

    @Override
    public boolean isTriggered(Message msg) {
        return rnd.nextFloat() < this.errorProbability;
    }
}
