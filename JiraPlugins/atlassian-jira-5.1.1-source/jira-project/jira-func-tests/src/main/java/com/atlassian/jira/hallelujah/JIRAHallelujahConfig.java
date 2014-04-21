package com.atlassian.jira.hallelujah;

import com.atlassian.buildeng.hallelujah.jms.JMSConfiguration;
import org.apache.commons.lang.StringUtils;

public class JIRAHallelujahConfig
{
    private static final String HALLELUJAH_QUEUE_ID_PROPERTY = "jira.hallelujah.queueId";

    public static JMSConfiguration getConfiguration()
    {
        JMSConfiguration configuration = JMSConfiguration.fromDefaultFile();
        final String queueId = System.getProperty(HALLELUJAH_QUEUE_ID_PROPERTY);
        if (StringUtils.isNotBlank(queueId))
        {
            configuration = JMSConfiguration.fromParams(configuration.getBrokerUrl(), queueId, configuration.getId());
        }
        return configuration;
    }
}
