package com.wehotel.fizz.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lancer Hong
 */

@Service
public class ServiceConfigHolder /*implements MessageListener*/ {

    private static final Logger log = LoggerFactory.getLogger(ServiceConfigHolder.class);

    private Map<String, ServiceConfig> serviceConfigMap = new HashMap<>(128);

    @PostConstruct
    public void init() {
        // make a http request which get all service config from ServiceConfigManager service, then to serviceConfigMap
    }

    // lsn ServiceConfig change msg from ServiceConfigManager service, then update serviceConfigMap
    // @Override
    // public void onMessage(Message message, byte[] pattern) {
    // }

    public ServiceConfig getServiceConfig(String id) {
        return serviceConfigMap.get(id);
    }
}
