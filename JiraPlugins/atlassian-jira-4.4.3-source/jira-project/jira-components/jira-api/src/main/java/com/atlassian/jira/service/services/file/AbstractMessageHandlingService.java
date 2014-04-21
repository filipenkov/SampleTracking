package com.atlassian.jira.service.services.file;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.service.AbstractService;
import com.atlassian.jira.service.util.ServiceUtils;
import com.atlassian.jira.service.util.handler.HandlerFactory;
import com.atlassian.jira.service.util.handler.MessageHandler;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * An abstract service to be subclassed by any service which wants to use MessageHandlers.
 */
public abstract class AbstractMessageHandlingService extends AbstractService
{
    private static final Logger log = Logger.getLogger(AbstractMessageHandlingService.class);
    protected static final String KEY_HANDLER = "handler";
    protected static final String KEY_HANDLER_PARAMS = "handler.params";

    /**
     * This field is volatile to ensure that the handler is "safely published". Since the handlers are effectively
     * immutable, this is sufficient to ensure changes made by init() in one thread are visible in other threads.
     */
    protected volatile MessageHandler handler = null;

    public void init(PropertySet props) throws ObjectConfigurationException
    {
        super.init(props);
        try
        {
            if (TextUtils.stringSet(getProperty(KEY_HANDLER)))
            {
                MessageHandler messageHandler = HandlerFactory.getHandler(getProperty(KEY_HANDLER));
                try
                {
                    if (hasProperty(KEY_HANDLER_PARAMS))
                    {
                        Map handlerParams = ServiceUtils.getParameterMap(getProperty(KEY_HANDLER_PARAMS));
                        messageHandler.init(handlerParams);
                    }
                }
                finally
                {
                    handler = messageHandler; // JRA-22396: publish init'ed state of handler safely
                }
            }
            else
            {
                log.error("You must specify a valid handler class for the " + getClass().getName() + " Service.");
            }
        }
        catch (Exception e)
        {
            log.error("Could not create handler (" + getProperty("handler") + ") - " + e, e);
        }
    }

    protected MessageHandler getHandler()
    {
        return handler;
    }
}
