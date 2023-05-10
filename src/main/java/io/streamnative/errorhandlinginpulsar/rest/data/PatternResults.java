package io.streamnative.errorhandlinginpulsar.rest.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PatternResults {

    private List<MessageContents> processedMessages = new ArrayList<>();

    private List<MessageContents> dlqMessages = new ArrayList<>();

    private List<ProcessingCount> processingCounts = new ArrayList<>();
}
