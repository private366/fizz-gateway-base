package com.wehotel.fizz.route;

import java.util.List;

/**
 * @author Lancer Hong
 * @see apis of serviceConfig.json
 */

public class ApiConfig {

    private String path;

    private Object blacklist;

    private Object whitelist;

    private List<Object> beforeForward;

    private Object LoadBalance;

    private Object trafficControl;

    private Object timeout;

    private Object failover;

    private List<Object> afterForward;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Object getBlacklist() {
        return blacklist;
    }

    public void setBlacklist(Object blacklist) {
        this.blacklist = blacklist;
    }

    public Object getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(Object whitelist) {
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
}
