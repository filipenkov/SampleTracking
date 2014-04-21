package org.jcvi.jira.plugins.customfield.shared.config;
/**
 * Created with IntelliJ IDEA. User: pedworth Date: 12/17/13 Time: 3:24 PM To
 * change this template use File | Settings | File Templates.
 */
public enum ConfigInputType {

    TEXTAREA("textarea"),
    SELECT("select"),
    MESSAGE("message");
    private String name;
    ConfigInputType(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
}
