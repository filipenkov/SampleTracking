package com.atlassian.jira.web.bean;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.web.FieldVisibilityManagerImpl;

/**
 * This provides a Bean version (i.e. no-arg constructor) which is usable inside JSPs with the webwork:bean tag for
 * legacy support.
 *
 * @deprecated You should get a FieldVisibilityManager dependency injected via PICO rather than creating one of these
 * manually. This class will not be availables via dependency injection in the future. Since v4.0
 */
@Deprecated
public class FieldVisibilityBean extends FieldVisibilityManagerImpl
{
    public FieldVisibilityBean()
    {
        super(ComponentManager.getInstance().getFieldManager(), ComponentAccessor.getProjectManager());
    }
}
