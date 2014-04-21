package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * If the property {@link com.atlassian.jira.config.properties.APKeys#JIRA_ATTACHMENT_SIZE} is not set
 * or not an {@link Integer} then set it to a default value.
 *
 * @since v3.13
 */
public class UpgradeTask_Build317 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build317.class);
    private static final String DEFAULT_ATTACHMENT_SIZE = "10485760";//10 Mb in bytes
    private static final String DEFAULT_WEBWORK_PROPERTIES = "webwork.properties";

    public String getBuildNumber()
    {
        return "317";
    }

    public String getShortDescription()
    {
        return "Set application property '" + APKeys.JIRA_ATTACHMENT_SIZE + "' to default value if not set or invalid value";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        final Properties properties = getWebworkProperties();
        if (properties == null)
        {
            setAttachmentSizeToDefaultValue();
            return;
        }

        final String attachmentSize = properties.getProperty(APKeys.JIRA_ATTACHMENT_SIZE);
        if (!TextUtils.stringSet(attachmentSize))
        {
            log.warn("'" + APKeys.JIRA_ATTACHMENT_SIZE + "' property value is not set. Using default value: '" + getDefaultValue() + "'");
            setAttachmentSizeToDefaultValue();
        }
        else
        {
            try
            {
                final int size = Integer.parseInt(attachmentSize);
                if (size < 0)
                {
                    log.warn("'" + APKeys.JIRA_ATTACHMENT_SIZE + "' property value '" + attachmentSize + "' is not a positive number. Using default value: '" + getDefaultValue() + "'");
                    setAttachmentSizeToDefaultValue();
                }
                else
                {
                    //set valid value
                    setAttachmentSizeToValue(attachmentSize);
                }
            }
            catch (final NumberFormatException e)
            {
                log.warn("'" + APKeys.JIRA_ATTACHMENT_SIZE + "' property value '" + attachmentSize + "' is not a valid number. Using default value: '" + getDefaultValue() + "'");
                setAttachmentSizeToDefaultValue();
            }
        }
    }

    protected Properties getWebworkProperties()
    {
        final Properties properties = new Properties();
        InputStream in = null;
        try
        {
            in = ClassLoaderUtils.getResourceAsStream(DEFAULT_WEBWORK_PROPERTIES, getClass());
            properties.load(in);
        }
        catch (final Exception e)
        {
            log.error(
                "Could not load webwork properties from '" + DEFAULT_WEBWORK_PROPERTIES + "'.  Using default value for '" + APKeys.JIRA_ATTACHMENT_SIZE + "' as '" + getDefaultValue() + "'",
                e);
            return null;
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (final IOException e)
            {
                log.error("Could not close '" + DEFAULT_WEBWORK_PROPERTIES + "' inputStream", e);
            }
        }
        return properties;
    }

    private void setAttachmentSizeToDefaultValue()
    {
        setAttachmentSizeToValue(getDefaultValue());
    }

    private void setAttachmentSizeToValue(final String value)
    {
        getApplicationProperties().setString(APKeys.JIRA_ATTACHMENT_SIZE, value);
    }

    private String getDefaultValue()
    {
        return DEFAULT_ATTACHMENT_SIZE;
    }
}
