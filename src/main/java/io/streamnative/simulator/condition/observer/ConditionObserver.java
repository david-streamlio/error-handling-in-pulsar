package io.streamnative.simulator.condition.observer;

import io.streamnative.simulator.condition.Condition;

public interface ConditionObserver {

    void update();

    void setErrorCondition(Condition condition);

}
