package com.atlassian.config.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

import javax.servlet.ServletContext;

public class BootstrappedContextLoader extends ContextLoader
{
    protected ApplicationContext loadParentContext(ServletContext servletContext) throws BeansException
    {
        return com.atlassian.config.util.BootstrapUtils.getBootstrapContext();
    }
}
