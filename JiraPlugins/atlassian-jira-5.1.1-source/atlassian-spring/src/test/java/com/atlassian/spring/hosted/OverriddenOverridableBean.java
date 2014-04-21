package com.atlassian.spring.hosted;

/**
 */
public class OverriddenOverridableBean implements OverridableBean
{
    public String getValue()
    {
        return "overridden";
    }
}
