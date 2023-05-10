package io.streamnative.util;

import org.apache.pulsar.client.api.PulsarClient;

import java.util.concurrent.CountDownLatch;

public abstract class PulsarClientThread implements Runnable {

    protected CountDownLatch latch;

    protected PulsarClient pulsarClient;

    protected String topicName;

    protected boolean running = false;

    protected PulsarClientThread() {

    }

    protected PulsarClientThread(PulsarClient pulsarClient, String topicName) {
        this.pulsarClient = pulsarClient;
        this.topicName = topicName;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public void halt() {
        this.running = false;
    }

    public abstract void close();
}
