package io.streamnative.simulator.condition.observer;

import io.streamnative.simulator.condition.Condition;
import io.streamnative.simulator.condition.ConditionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class TransientConditionObserver implements ConditionObserver {

    public static TransientConditionObserver of(long delay) {
        return new TransientConditionObserver(delay);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TransientConditionObserver.class);

    private final long delay;

    private Timer timer = new Timer("Timer");

    private Condition errorCondition;

    private AtomicBoolean scheduledToBeCleared = new AtomicBoolean(false);

    private TransientConditionObserver(long delay) {
        this.delay = delay;
    }

    @Override
    public void update() {
        LOGGER.debug(String.format("Scheduling error to clear in %d milliseconds", delay));
        if (!scheduledToBeCleared.get()) {
            scheduledToBeCleared.set(true);
            TimerTask task = new TimerTask() {
                public void run() {
                    errorCondition.changeState(ConditionState.CLEAR);
                    scheduledToBeCleared.set(false);
                    LOGGER.debug("Error cleared");
                }
            };

            timer.schedule(task, delay);
        }
    }

    @Override
    public void setErrorCondition(Condition condition) {
        this.errorCondition = condition;
    }
}
