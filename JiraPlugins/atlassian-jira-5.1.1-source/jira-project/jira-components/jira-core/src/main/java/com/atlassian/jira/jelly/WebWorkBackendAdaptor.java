/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import com.atlassian.core.ofbiz.CoreFactory;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.log4j.Logger;
import webwork.util.ValueStack;

import java.beans.Introspector;
import java.util.Map;

/**
 * Used to kick off backend actions;
 *
 * @deprecated Because WebWorkBackend actions are deprecated - see {@link com.atlassian.core.ofbiz.CoreFactory#getActionDispatcher()}. Since v5.0.
 */
public class WebWorkBackendAdaptor extends WebWorkAdaptor
{
    private static final Logger log = Logger.getLogger(WebWorkAdaptor.class);

    public WebWorkBackendAdaptor()
    {
        // Clear caches
        // RO: If not, then it will contain garbage after a couple of redeployments
        Introspector.flushCaches();

        // Clear ValueStack method cache
        // RO: If not, then it will contain garbage after a couple of redeployments
        ValueStack.clearMethods();
    }

    /**
     * @deprecated Because WebWorkBackend actions are deprecated - see {@link com.atlassian.core.ofbiz.CoreFactory#getActionDispatcher()}. Since v5.0.
     */
    public boolean mapJellyTagToAction(ActionTagSupport tag, XMLOutput output) throws JellyTagException
    {
        log.debug("WebWorkAdaptor.mapJellyTagToAction");

        // Set up the dispatcher
        String actionName = tag.getActionName();

        Map fields = tag.getProperties();

        try
        {
            setResult(CoreFactory.getActionDispatcher().execute(actionName, fields));
        }
        catch (Exception e)
        {
            return processWebworkException(actionName, e, output, tag);
        }

        return processResult(tag, output);
    }
}
