package io.streamnative.simulator.condition.observer;

public interface Observable {

    void register(ConditionObserver obj);

    void unregister(ConditionObserver obj);

    void notifyObservers();
}
