package io.streamnative.errorhandlinginpulsar.configs;

import io.streamnative.simulator.condition.Condition;
import io.streamnative.simulator.condition.observer.TransientConditionObserver;
import io.streamnative.simulator.condition.trigger.MessageContentTrigger;
import io.streamnative.util.consumer.AbstractConsumer;
import io.streamnative.util.consumer.StringConsumer;
import io.streamnative.util.consumer.handlers.error.IgnoreStrategy;
import io.streamnative.util.consumer.handlers.error.NackStrategy;
import io.streamnative.util.consumer.handlers.error.ReconsumeLaterStrategy;
import io.streamnative.util.consumer.handlers.error.StopStrategy;
import io.streamnative.util.consumer.handlers.success.WriteToTopicStrategy;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;
import org.apache.pulsar.client.api.SubscriptionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Configuration
@PropertySources({
        @PropertySource("classpath:consumer.properties")
})
public class ConsumerConfig {

    @Autowired
    private ApplicationContext ctx;

    public static PropertySourcesPlaceholderConfigurer consumerPropertyConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    IgnoreStrategy ignore() {
        return new IgnoreStrategy();
    }

    @Bean
    NackStrategy nack() {
        return new NackStrategy();
    }

    @Bean
    StopStrategy stop() {
        return new StopStrategy();
    }

    /*
     * These are all single errors for a specific message value.
     * Example would be a malformed message, whose contents can never be read
     * or a CDC database operation on a table that doesn't exist.
     */
    @Bean
    @Scope("prototype")
    public AbstractConsumer ackTimeoutConsumer() {
        StringConsumer consumer = new StringConsumer(ctx.getBean(PulsarClient.class),
                ctx.getEnvironment().getProperty("consumer.timeout.input.topicName"),
                ctx.getEnvironment().getProperty("consumer.timeout.input.subscriptionName"));

        consumer.updateConsumerBuilder(
                consumer.getConsumerBuilder()
                        .ackTimeout(5, TimeUnit.SECONDS)
                        .ackTimeoutTickTime(1, TimeUnit.SECONDS)
                        .receiverQueueSize(1)
                        .subscriptionInitialPosition(SubscriptionInitialPosition.Latest)
                        .subscriptionType(SubscriptionType.Exclusive)
        );

        consumer.setMessageHandler(ctx.getBean(WriteToTopicStrategy.class,
                ctx.getEnvironment().getProperty("consumer.timeout.output.topicName")));

        // Messages with a value of "5" will take longer to process
        consumer.setProcessingDelayCondition(Condition.builder()
                .triggers(Collections.singletonList(new MessageContentTrigger("5"))).build());

        // Processing delay of 5 seconds
        consumer.getProcessingDelayCondition().register(TransientConditionObserver.of(10000L));

        return consumer;
    }

    @Bean
    @Scope("prototype")
    public AbstractConsumer ignoreErrorConsumer() {
        StringConsumer consumer = new StringConsumer(ctx.getBean(PulsarClient.class),
                ctx.getEnvironment().getProperty("consumer.ignore.input.topicName"),
                ctx.getEnvironment().getProperty("consumer.ignore.input.subscriptionName"));

        consumer.updateConsumerBuilder(
                consumer.getConsumerBuilder()
                        .subscriptionInitialPosition(SubscriptionInitialPosition.Latest)
                        .subscriptionType(SubscriptionType.Exclusive)
        );

        consumer.setMessageHandler(ctx.getBean(WriteToTopicStrategy.class,
                ctx.getEnvironment().getProperty("consumer.ignore.output.topicName")));

        // Ignore any messages that fail to process. Ack them and continue
        consumer.setErrorHandler(ctx.getBean(IgnoreStrategy.class));
        return consumer;
    }

    @Bean
    @Scope("prototype")
    public AbstractConsumer stopOnErrorConsumer() {
        StringConsumer consumer = new StringConsumer(ctx.getBean(PulsarClient.class),
                ctx.getEnvironment().getProperty("consumer.stop.input.topicName"),
                ctx.getEnvironment().getProperty("consumer.stop.input.subscriptionName"));

        consumer.updateConsumerBuilder(
            consumer.getConsumerBuilder().receiverQueueSize(1)
                .subscriptionInitialPosition(SubscriptionInitialPosition.Latest)
                .subscriptionType(SubscriptionType.Exclusive));

        // Write the messages to an output topic
        consumer.setMessageHandler(ctx.getBean(WriteToTopicStrategy.class,
                ctx.getEnvironment().getProperty("consumer.stop.output.topicName")));

        // Immediately stop processing all messages on the first error
        consumer.setErrorHandler(ctx.getBean(StopStrategy.class));
        return consumer;
    }

    @Bean
    @Scope("prototype")
    public AbstractConsumer retryViaNackConsumer() {

        StringConsumer consumer = new StringConsumer(ctx.getBean(PulsarClient.class),
                ctx.getEnvironment().getProperty("consumer.retryNack.input.topicName"),
                ctx.getEnvironment().getProperty("consumer.retryNack.input.subscriptionName"));

        consumer.updateConsumerBuilder(
                consumer.getConsumerBuilder()
                        .negativeAckRedeliveryDelay(10, TimeUnit.SECONDS)
                        .subscriptionInitialPosition(SubscriptionInitialPosition.Latest)
                        .subscriptionType(SubscriptionType.Shared)
        );

        // Write the messages to an output topic
        consumer.setMessageHandler(ctx.getBean(WriteToTopicStrategy.class,
                ctx.getEnvironment().getProperty("consumer.retryNack.output.topicName")));

        // Nack all processing failures
        consumer.setErrorHandler(ctx.getBean(NackStrategy.class));

        return consumer;
    }

    @Bean
    @Scope("prototype")
    public AbstractConsumer retryViaNackDLQConsumer() {

        StringConsumer consumer = new StringConsumer(ctx.getBean(PulsarClient.class),
                ctx.getEnvironment().getProperty("consumer.retryNackDLQ.input.topicName"),
                ctx.getEnvironment().getProperty("consumer.retryNackDLQ.input.subscriptionName"));

        consumer.updateConsumerBuilder(
                consumer.getConsumerBuilder()
                        .negativeAckRedeliveryDelay(10, TimeUnit.SECONDS)
                        .subscriptionInitialPosition(SubscriptionInitialPosition.Latest)
                        .subscriptionType(SubscriptionType.Shared)
                        .deadLetterPolicy(DeadLetterPolicy.builder()
                                .deadLetterTopic(ctx.getEnvironment()
                                        .getProperty("consumer.retryNackDLQ.DLQ.topicName"))
                                .maxRedeliverCount(ctx.getEnvironment()
                                        .getProperty("consumer.retryNackDLQ.DLQ.maxRedeliveryCount", Integer.class))
                                .build())
        );

        // Write the messages to an output topic
        consumer.setMessageHandler(ctx.getBean(WriteToTopicStrategy.class,
                ctx.getEnvironment().getProperty("consumer.retryNackDLQ.output.topicName")));

        // Nack all processing failures
        consumer.setErrorHandler(ctx.getBean(NackStrategy.class));

        return consumer;
    }

    @Bean
    @Scope("prototype")
    public AbstractConsumer retryTopicConsumer() {

        StringConsumer consumer = new StringConsumer(ctx.getBean(PulsarClient.class),
                ctx.getEnvironment().getProperty("consumer.retryTopic.input.topicName"),
                ctx.getEnvironment().getProperty("consumer.retryTopic.input.subscriptionName"));

        consumer.updateConsumerBuilder(
                consumer.getConsumerBuilder()
                        .negativeAckRedeliveryDelay(30, TimeUnit.SECONDS)
                        .subscriptionInitialPosition(SubscriptionInitialPosition.Latest)
                        .subscriptionType(SubscriptionType.Shared)
                        .receiverQueueSize(1)
                        .enableRetry(true)
                        .deadLetterPolicy(DeadLetterPolicy.builder()
                                .deadLetterTopic(ctx.getEnvironment()
                                        .getProperty("consumer.retryTopic.DLQ.topicName"))
                                .retryLetterTopic(ctx.getEnvironment()
                                        .getProperty("consumer.retryTopic.DLQ.retry.topicName"))
                                .maxRedeliverCount(ctx.getEnvironment()
                                        .getProperty("consumer.retryTopic.DLQ.maxRedeliveryCount", Integer.class))
                                .build())
        );

        // Write the messages to an output topic
        consumer.setMessageHandler(ctx.getBean(WriteToTopicStrategy.class,
                ctx.getEnvironment().getProperty("consumer.retryTopic.output.topicName")));

        // Schedule all failed messages for re-processing in 500 ms.
        consumer.setErrorHandler(new ReconsumeLaterStrategy(500, TimeUnit.MILLISECONDS));

        return consumer;
    }
}
