package com.atlassian.spring.hosted;

/**
 */
public class DefaultOverridableBean implements OverridableBean
{
    public String getValue()
    {
        return "default";
    }
}
