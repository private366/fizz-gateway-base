package com.wehotel.fizz.route;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Lancer Hong
 * 对应serviceConfig.json.服务id.beforeForward[i]
 */

public class HandlerConfig {

    public String id;

    @JsonProperty("config")
    public String json;

    public HandlerConfig(String id, String json) {
        this.id = id;
        this.json = json;
    }
}
