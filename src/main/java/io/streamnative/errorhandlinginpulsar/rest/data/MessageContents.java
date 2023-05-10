package io.streamnative.errorhandlinginpulsar.rest.data;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MessageContents {

    private String key;

    private String contents;

    private List<MessageProperty> properties;
}
