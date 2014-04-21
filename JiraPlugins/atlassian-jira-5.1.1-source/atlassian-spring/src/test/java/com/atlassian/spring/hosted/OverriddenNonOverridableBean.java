package com.atlassian.spring.hosted;

/**
 */
public class OverriddenNonOverridableBean implements NonOverridableBean
{
    public String getValue()
    {
        return "overridden";
    }
}
