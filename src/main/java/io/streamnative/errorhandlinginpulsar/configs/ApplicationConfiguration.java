package io.streamnative.errorhandlinginpulsar.configs;

import io.streamnative.util.reader.StringTopicReader;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.admin.PulsarAdminBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@PropertySources({
        @PropertySource("classpath:consumer.properties")
})
@ComponentScan(basePackages = {
        "io.streamnative.errorhandlinginpulsar",
        "io.streamnative.util"})
public class ApplicationConfiguration {

    @Autowired
    private ApplicationContext ctx;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public ExecutorService executor() { return Executors.newFixedThreadPool(10);}

    @Bean
    public StringTopicReader retryTopicReader() {
        return new StringTopicReader(ctx.getBean(PulsarClient.class),
                ctx.getEnvironment().getProperty("consumer.retryTopic.output.topicName"));
    }

    @Bean(destroyMethod = "close")
    public PulsarAdmin pulsarAdmin() throws PulsarClientException {
        PulsarAdminBuilder adminBuilder = PulsarAdmin.builder()
                .serviceHttpUrl("http://localhost:8080");

        return adminBuilder.build();
    }
}
