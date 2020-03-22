package com.wehotel.fizz.route;

import org.springframework.web.server.ServerWebExchange;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lancer Hong
 * inspired by vert.x
 */

public class RoutingContext {

    private ServerWebExchange exchange;

    private Map<String/* it's serviceConfig.json.服务id.beforeForward[i].id  */, HandleResult> dataMap;

    public RoutingContext(ServerWebExchange exchange) {
        this.exchange = exchange;
    }

    public ServerWebExchange getServerWebExchange() {
        return exchange;
    }

    public HandleResult get(String handlerId) {
        if (dataMap == null) {
            return null;
        } else {
            return dataMap.get(handlerId);
        }
    }

    public void put(String handlerId, HandleResult hr) {
        if (dataMap == null) {
            dataMap = new HashMap<>();
        }
        dataMap.put(handlerId, hr);
    }
}
