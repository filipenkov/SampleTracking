package com.atlassian.jira.projectconfig.util;

import java.util.Map;

/**
 * Can be used to create a standard JIRA velocity context.
 *
 * @since v4.4
 */
public interface VelocityContextFactory
{
    public Map<String, Object> createVelocityContext(Map<String, Object> initContext);
    public Map<String, Object> createDefaultVelocityContext();
}
