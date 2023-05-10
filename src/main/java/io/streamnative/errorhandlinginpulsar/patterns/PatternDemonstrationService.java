package io.streamnative.errorhandlinginpulsar.patterns;

import io.streamnative.errorhandlinginpulsar.configs.CountdownLatchFactory;
import io.streamnative.errorhandlinginpulsar.rest.data.MessageContents;
import io.streamnative.errorhandlinginpulsar.rest.data.MessageProperty;
import io.streamnative.errorhandlinginpulsar.rest.data.PatternResults;
import io.streamnative.errorhandlinginpulsar.rest.data.ProcessingCount;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class PatternDemonstrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PatternDemonstrationService.class);

    @Autowired
    protected ExecutorService executor;

    @Autowired
    private CountdownLatchFactory countdownLatchFactory;

    public PatternResults demonstrate(ErrorPattern pattern) {

        try {
            CountDownLatch latch = countdownLatchFactory.getObject();
            pattern.getProducer().setLatch(latch);
            pattern.getConsumer().setLatch(latch);

            long startTime = System.currentTimeMillis();

            executor.submit(pattern.getConsumer());
            Thread.sleep(5000); // Wait for the consumer to connect to the broker, etc.
            executor.submit(pattern.getProducer());

            latch.await();
            return getResults(pattern, startTime);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            pattern.getConsumer().close();
            pattern.getProducer().close();
        }
    }

    private PatternResults getResults(ErrorPattern pattern, long startTime) throws PulsarClientException {
        PatternResults results = new PatternResults();
        if (pattern.getProcessedMessagesReader() != null) {
            Reader reader = pattern.getProcessedMessagesReader().getReader(startTime);
            results.getProcessedMessages().addAll(
                    getMessages(reader));
        }

        if (pattern.getDLQMessageReader() != null) {
            Reader reader = pattern.getDLQMessageReader().getReader(startTime);
            results.getDlqMessages().addAll(
                    getMessages(reader));
        }

        Map<String, Integer> map = pattern.getConsumer().getProcessingCountMap();

        for (String key : map.keySet()) {
            results.getProcessingCounts().add(
                    ProcessingCount.builder()
                            .msgContent(key)
                            .processCount(map.get(key))
                            .build());
        }

        // Publish this to a Pulsar topic, so I can query using Pulsar SQL?
        return results;
    }

    private List<MessageContents> getMessages(Reader reader) throws PulsarClientException {
        List<MessageContents> msgs = new ArrayList<>();

        while (reader.hasMessageAvailable()) {
            Message msg = reader.readNext();

            List<MessageProperty> properties = null;
            if (!msg.getProperties().isEmpty()) {
                properties = new ArrayList<>();

                Map<String, String> map = msg.getProperties();

                for (String key : map.keySet()) {
                    properties.add(MessageProperty.builder()
                                .key(key)
                                .value(msg.getProperty(key))
                                .build());
                }
            }

            msgs.add(MessageContents.builder()
                        .key(msg.getKey())
                        .contents(msg.getValue().toString())
                        .properties(properties)
                        .build());
        }

        return msgs;
    }
}
