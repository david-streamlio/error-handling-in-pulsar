package io.streamnative.simulator.condition;

import io.streamnative.simulator.condition.observer.Observable;
import io.streamnative.simulator.condition.observer.ConditionObserver;
import io.streamnative.simulator.condition.trigger.Trigger;
import lombok.Builder;
import org.apache.pulsar.client.api.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Builder
public class Condition implements Observable {

    @Builder.Default
    private List<ConditionObserver> conditionObservers = new ArrayList<ConditionObserver>();

    @Builder.Default
    private ConditionState initialReference = ConditionState.CLEAR;

    private AtomicReference<ConditionState> conditionState;
    private List<Trigger> triggers;

    public boolean exists() {
        return getConditionState().get().equals(ConditionState.TRIGGERED);
    }

    public boolean check(Message msg) {
        boolean triggered = false;
        try {
            if (conditionTriggered(msg)) {
                this.changeState(ConditionState.TRIGGERED);
                triggered = true;
            }
            return exists();
        } finally {
            if (triggered) {
                notifyObservers(); // Trigger the error clearing logic
            }
        }
    }

    private AtomicReference<ConditionState> getConditionState() {
        if (conditionState == null) {
            conditionState = new AtomicReference<ConditionState>(initialReference);
        }
        return conditionState;
    }

    private boolean conditionTriggered(Message msg) {
        boolean triggered = false;
        for (Trigger trigger: triggers) {
            if (trigger.isTriggered(msg)) {
                /* Don't exit early in case we have multiple counter triggers
                   because we need each trigger to update their count in order
                   to work as expected.
                 */
                triggered = true;
            }
        }
        return triggered;
    }

    public synchronized void changeState(ConditionState state) {
        getConditionState().set(state);
    }

    @Override
    public void register(ConditionObserver errorConditionObserver) {
        if (errorConditionObserver != null && !conditionObservers.contains(errorConditionObserver)) {
            conditionObservers.add(errorConditionObserver);
            errorConditionObserver.setErrorCondition(this);
        }
    }

    @Override
    public void unregister(ConditionObserver errorConditionObserver) {
        if (errorConditionObserver != null && conditionObservers.contains(errorConditionObserver)) {
            conditionObservers.remove(errorConditionObserver);
        }
    }

    @Override
    public void notifyObservers() {
        for (ConditionObserver errorConditionObserver : conditionObservers) {
          errorConditionObserver.update();
        }
    }
}
