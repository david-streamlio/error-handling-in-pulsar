package io.streamnative.errorhandlinginpulsar.pulsar;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pulsar")
public class PulsarProperties {

    private String serviceUrl;
    private String serviceHttpUrl;
    private String authPluginClassName;
    private String authParams;

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getServiceHttpUrl() {
        return serviceHttpUrl;
    }

    public void setServiceHttpUrl(String serviceHttpUrl) {
        this.serviceHttpUrl = serviceHttpUrl;
    }

    public String getAuthPluginClassName() {
        return authPluginClassName;
    }

    public void setAuthPluginClassName(String authPluginClassName) {
        this.authPluginClassName = authPluginClassName;
    }

    public String getAuthParams() {
        return authParams;
    }

    public void setAuthParams(String authParams) {
        this.authParams = authParams;
    }
}
