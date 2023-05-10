package io.streamnative.errorhandlinginpulsar.rest;

import io.streamnative.errorhandlinginpulsar.patterns.ErrorPattern;
import io.streamnative.errorhandlinginpulsar.patterns.PatternDemonstrationService;
import io.streamnative.errorhandlinginpulsar.rest.data.PatternResults;
import io.streamnative.util.producer.StringProducer;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
public class PatternController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PatternDemonstrationService.class);

    private static final String[] PATTERNS = {"ackTimeoutPattern", "ignoreErrorPattern", "stopOnErrorPattern",
            "retryViaNackPattern", "retryViaNackDLQPattern", "retryTopicPattern"};

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    PatternDemonstrationService demoService;

    @GetMapping("/patterns/list")
    public String listPatternNames() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : PATTERNS) {
            stringBuilder.append(s).append("\n");
        }

        return stringBuilder.toString();
    }

    @GetMapping("/pattern/{name}")
    public PatternResults runPattern(@PathVariable String name,
                             @RequestParam(name = "numMessages", defaultValue = "100") String numMessages,
                             @RequestParam(name = "errorOnCounts", defaultValue = "") String[] errorOnCount,
                                     @RequestParam(name="errorOnValues", defaultValue = "") String[] errorOnValues,
                             @RequestParam(name = "errorDurationMillis", defaultValue = "10") String errorDurationMillis) {

        LOGGER.debug(String.format("Processing request [pattern: %s, numMessages: %s, errorFrequency: %s, " +
                        "errorValues %s, errorDurationMillis %s]",
                name, numMessages, Arrays.toString(errorOnCount), Arrays.toString(errorOnValues), errorDurationMillis));

        return executePattern(name, numMessages,
                Arrays.stream(errorOnCount).mapToInt(Integer::parseInt).toArray(),
                errorOnValues, Long.parseLong(errorDurationMillis));
    }

    @GetMapping("/ackTimeout")
    public PatternResults ackTimeout(Model model) {
        PatternResults results = demoService.demonstrate(ctx.getBean("ackTimeoutPattern", ErrorPattern.class));
        return results;
    }

    private PatternResults executePattern(String patternName, String numMessages, int[] errorOnCount,
                                          String[] errorOnValues, Long errorDurationMillis) {
        ErrorPattern pattern = ctx.getBean(patternName, ErrorPattern.class);

        pattern.setProducer(ctx.getBean(StringProducer.class, ctx.getBean(PulsarClient.class),
                pattern.getConsumer().getTopicName()));

        pattern.setNumMessages(numMessages);
        pattern.modifyErrorBehavior(errorOnCount, errorOnValues, errorDurationMillis);
        return demoService.demonstrate(pattern);
    }

}
