package io.streamnative.errorhandlinginpulsar.configs;

import io.streamnative.errorhandlinginpulsar.patterns.ErrorPattern;
import io.streamnative.util.consumer.AbstractConsumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PatternConfig {

    @Autowired
    private ApplicationContext ctx;


    @Bean
    public ErrorPattern ackTimeoutPattern() {
        return new ErrorPattern(ctx.getBean("ackTimeoutConsumer", AbstractConsumer.class),
                ctx.getEnvironment().getProperty("consumer.timeout.output.topicName"));
    }

    @Bean
    public ErrorPattern ignoreErrorPattern() {
        return new ErrorPattern(ctx.getBean("ignoreErrorConsumer", AbstractConsumer.class),
                ctx.getEnvironment().getProperty("consumer.ignore.output.topicName"));
    }

    @Bean
    public ErrorPattern stopOnErrorPattern() {
        return new ErrorPattern(ctx.getBean("stopOnErrorConsumer", AbstractConsumer.class),
                ctx.getEnvironment().getProperty("consumer.stop.output.topicName"));
    }

    @Bean
    public ErrorPattern retryViaNackPattern() {
        return new ErrorPattern(ctx.getBean("retryViaNackConsumer", AbstractConsumer.class),
                ctx.getEnvironment().getProperty("consumer.retryNack.output.topicName"));
    }

    @Bean
    public ErrorPattern retryViaNackDLQPattern() {
        return new ErrorPattern(ctx.getBean("retryViaNackDLQConsumer", AbstractConsumer.class),
                ctx.getEnvironment().getProperty("consumer.retryNackDLQ.output.topicName"),
                ctx.getEnvironment().getProperty("consumer.retryNackDLQ.DLQ.topicName"));
    }

    @Bean
    public ErrorPattern retryTopicPattern() {
        return new ErrorPattern(ctx.getBean("retryTopicConsumer", AbstractConsumer.class),
                ctx.getEnvironment().getProperty("consumer.retryTopic.output.topicName"),
                ctx.getEnvironment().getProperty("consumer.retryTopic.DLQ.topicName"));
    }

}
