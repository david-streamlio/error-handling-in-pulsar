package io.streamnative.errorhandlinginpulsar.rest.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageProperty {

    private String key;

    private String value;
}
