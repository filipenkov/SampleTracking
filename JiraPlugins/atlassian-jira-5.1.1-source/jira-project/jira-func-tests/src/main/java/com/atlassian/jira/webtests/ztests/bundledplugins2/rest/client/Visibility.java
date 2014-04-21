package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

public class Visibility
{
    public String type;
    public String value;

    public Visibility() {}

    public Visibility(final String type, final String value)
    {
        this.type = type;
        this.value = value;
    }
}
