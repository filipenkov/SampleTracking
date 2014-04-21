package com.atlassian.config.spring;

import com.atlassian.spring.container.SpringContainerContext;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class BootstrappedContainerContext extends SpringContainerContext
{
    public synchronized void refresh()
    {
        ContextLoader loader = new BootstrappedContextLoader();

        // if we have an existing spring context, ensure we close it properly
        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());

        if (ctx != null)
        {
            loader.closeWebApplicationContext(getServletContext());
        }

        loader.initWebApplicationContext(getServletContext());

        if (getApplicationContext() == null)
        {
            setApplicationContext(WebApplicationContextUtils.getWebApplicationContext(getServletContext()));
        }

        contextReloaded();
    }
}
