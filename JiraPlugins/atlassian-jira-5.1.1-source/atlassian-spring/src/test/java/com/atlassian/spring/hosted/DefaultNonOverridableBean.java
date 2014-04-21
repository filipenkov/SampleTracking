package com.atlassian.spring.hosted;

/**
 */
public class DefaultNonOverridableBean implements NonOverridableBean
{
    public String getValue()
    {
        return "default";
    }
}
