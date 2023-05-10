package io.streamnative.errorhandlinginpulsar.pulsar;

import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

public class PulsarConfiguration {
    @Autowired
    private PulsarProperties pulsarProperties;

    @Bean(destroyMethod = "close")
    public PulsarClient pulsarClient() throws PulsarClientException {
        ClientBuilder clientBuilder = PulsarClient.builder()
                .serviceUrl(pulsarProperties.getServiceUrl());

        if (StringUtils.isNotBlank(pulsarProperties.getAuthPluginClassName())) {
            clientBuilder.authentication(
                    AuthenticationFactory.create(
                            pulsarProperties.getAuthPluginClassName(),
                            pulsarProperties.getAuthParams()
                    )
            );
        }

        return clientBuilder.build();
    }

}
