package io.streamnative.errorhandlinginpulsar.configs;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CountdownLatchFactory implements FactoryBean<CountDownLatch> {

    private int count;

    public CountdownLatchFactory(@Value("${latch.count}") int count) {
        this.count = count;
    }

    @Override
    public CountDownLatch getObject() throws Exception {
        return new CountDownLatch(count);
    }

    @Override
    public Class<?> getObjectType() {
        return CountDownLatch.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}

