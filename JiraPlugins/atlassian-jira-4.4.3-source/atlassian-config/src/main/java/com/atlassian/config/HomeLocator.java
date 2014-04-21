package com.atlassian.config;

import javax.servlet.ServletContext;

/**
 * Used to find application home properties
 */
public interface HomeLocator
{
    String getHomePath();

    String getConfigFileName();

    void lookupServletHomeProperty(ServletContext context);
}
