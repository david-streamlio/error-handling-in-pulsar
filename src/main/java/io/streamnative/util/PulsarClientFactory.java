package io.streamnative.util;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public class PulsarClientFactory {
    private final String brokerUrl;
    private PulsarClient pulsarClient;

    public PulsarClientFactory(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public PulsarClient createClient() throws PulsarClientException {
        if (pulsarClient == null) {
           pulsarClient = PulsarClient.builder().serviceUrl(brokerUrl).build();
        }
        return pulsarClient;
    }
}
