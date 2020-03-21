package com.wehotel.fizz.route;

import java.util.List;

/**
 * @author Lancer Hong
 * @see serviceConfig.json
 */

public class ServiceConfig {

    private String id;

    private List<String> blacklist;

    private List<String> whitelist;

    private List<Object> beforeForward;

    private Object LoadBalance;

    private Object trafficControl;

    private Object timeout;

    private Object failover;

    private List<Object> afterForward;

    private List<ApiConfig> apis;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getBlacklist() {
        return blacklist;
    }

    public void setBlacklist(List<String> blacklist) {
        this.blacklist = blacklist;
    }

    public List<String> getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist;
    }

    public List<Object> getBeforeForward() {
        return beforeForward;
    }

    public void setBeforeForward(List<Object> beforeForward) {
        this.beforeForward = beforeForward;
    }

    public Object getLoadBalance() {
        return LoadBalance;
    }

    public void setLoadBalance(Object loadBalance) {
        LoadBalance = loadBalance;
    }

    public Object getTrafficControl() {
        return trafficControl;
    }

    public void setTrafficControl(Object trafficControl) {
        this.trafficControl = trafficControl;
    }

    public Object getTimeout() {
        return timeout;
    }

    public void setTimeout(Object timeout) {
        this.timeout = timeout;
    }

    public Object getFailover() {
        return failover;
    }

    public void setFailover(Object failover) {
        this.failover = failover;
    }

    public List<Object> getAfterForward() {
        return afterForward;
    }

    public void setAfterForward(List<Object> afterForward) {
        this.afterForward = afterForward;
    }

    public List<ApiConfig> getApis() {
        return apis;
    }

    public void setApis(List<ApiConfig> apis) {
        this.apis = apis;
    }
}
