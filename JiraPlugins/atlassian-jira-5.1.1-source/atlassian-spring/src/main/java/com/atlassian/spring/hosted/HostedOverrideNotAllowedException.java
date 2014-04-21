package com.atlassian.spring.hosted;

import java.net.URL;

import org.springframework.beans.BeansException;

/**
 * Exception thrown when a hosted override tries to override a bean that isn't marked as being allowed to be overridden.
 */
public class HostedOverrideNotAllowedException extends BeansException
{
    public HostedOverrideNotAllowedException(String bean, URL url)
    {
        super("Hosted override in " + url + " trying to override bean " + bean + " but bean is not overridable");
    }
}
