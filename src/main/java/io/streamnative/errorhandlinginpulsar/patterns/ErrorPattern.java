package io.streamnative.errorhandlinginpulsar.patterns;

import io.micrometer.common.util.StringUtils;
import io.streamnative.simulator.condition.Condition;
import io.streamnative.simulator.condition.observer.TransientConditionObserver;
import io.streamnative.simulator.condition.trigger.MessageContentTrigger;
import io.streamnative.simulator.condition.trigger.MessageCountTrigger;
import io.streamnative.simulator.condition.trigger.Trigger;
import io.streamnative.util.consumer.AbstractConsumer;
import io.streamnative.util.producer.AbstractProducer;

import io.streamnative.util.reader.AbstractTopicReader;
import io.streamnative.util.reader.StringTopicReader;
import lombok.Data;
import org.apache.pulsar.client.api.PulsarClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class ErrorPattern {

    @Autowired
    private ApplicationContext ctx;

    private AbstractConsumer consumer;

    private AbstractProducer producer;

    private String processedMessagesTopic;

    private String deadLetterQueueTopic;

    public ErrorPattern(AbstractConsumer con, String outputTopic) {
        this(con, outputTopic, null);
    }

    public ErrorPattern(AbstractConsumer con, String outputTopic, String dlq) {
        this.consumer = con;
        this.processedMessagesTopic = outputTopic;
        this.deadLetterQueueTopic = dlq;
    }

    public AbstractTopicReader getProcessedMessagesReader() {
        if (StringUtils.isNotBlank(processedMessagesTopic)) {
            return new StringTopicReader(ctx.getBean(PulsarClient.class), processedMessagesTopic);
        }
        return null;
    }

    public AbstractTopicReader getDLQMessageReader() {
        if (StringUtils.isNotBlank(deadLetterQueueTopic)) {
            return new StringTopicReader(ctx.getBean(PulsarClient.class), deadLetterQueueTopic);
        }
        return null;
    }

    public void setNumMessages(String numMessages) {
        if (StringUtils.isNotBlank(numMessages) && Integer.parseInt(numMessages) > 0) {
            this.getProducer().setMsgCount(Integer.parseInt(numMessages));
        }
    }

    // TODO Add support for multiple errors, eg. getConsumer.addErrorCondition() vs. setErrorCondition()
    public void modifyErrorBehavior(int[] errorOnCount, String[] errorOnValues, Long errorDurationMillis) {
        this.getConsumer().setErrorCondition(null);

        List<Trigger> triggers = new ArrayList<>();

        if (errorOnValues != null) {
            for (String s : errorOnValues) {
                triggers.add(new MessageContentTrigger(s));
            }
        }

        if (errorOnCount != null) {
            for (int i : errorOnCount) {
                triggers.add(new MessageCountTrigger(i));
            }
        }

        if (!CollectionUtils.isEmpty(triggers)) {
            this.getConsumer().setErrorCondition(Condition.builder()
                    .triggers(triggers).build());

            this.getConsumer().getErrorCondition().register(
                    TransientConditionObserver.of(errorDurationMillis));
        }
    }
}
