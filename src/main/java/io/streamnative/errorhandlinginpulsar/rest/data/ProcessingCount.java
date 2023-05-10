package io.streamnative.errorhandlinginpulsar.rest.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessingCount {
    private String msgContent;

    private Integer processCount;
}
