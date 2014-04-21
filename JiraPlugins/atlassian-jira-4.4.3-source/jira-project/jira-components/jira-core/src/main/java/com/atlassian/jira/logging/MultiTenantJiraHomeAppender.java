package com.atlassian.jira.logging;

import com.atlassian.jira.startup.JiraHomeStartupCheck;
import com.atlassian.multitenant.MultiTenantComponentMap;
import com.atlassian.multitenant.MultiTenantContext;
import com.atlassian.multitenant.MultiTenantCreator;
import com.atlassian.multitenant.Tenant;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Priority;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.OptionHandler;

import java.io.File;

/**
 * All this class really does is delegate to the appropriate JiraHomeAppender for the current tenant.
 *
 * @since v4.3
 */
public class MultiTenantJiraHomeAppender implements Appender, OptionHandler, RollOverLogAppender
{
    private volatile MultiTenantComponentMap<Appender> multiTenantAppender;
    private final JiraHomeAppender systemTenantAppender = new JiraHomeAppender(JiraHomeStartupCheck.getInstance());
    private volatile String fileName;

    private Appender getAppender()
    {
        if (!MultiTenantContext.isEnabled() || !MultiTenantContext.getTenantReference().isSet())
        {
            return systemTenantAppender;
        }
        else
        {
            return getMultiTenantAppenders().get();
        }
    }

    private MultiTenantComponentMap<Appender> getMultiTenantAppenders()
    {
        if (multiTenantAppender == null)
        {
            synchronized (this)
            {
                if (multiTenantAppender == null)
                {
                    multiTenantAppender = MultiTenantContext.getFactory().createComponentMap(new AppenderCreator());
                }
            }
        }
        return multiTenantAppender;
    }

    private class AppenderCreator implements MultiTenantCreator<Appender>
    {
        @Override
        public Appender create(Tenant tenant)
        {
            // For the System Tenant we just want to reuse the already-created default appender.
            if (tenant == MultiTenantContext.getSystemTenant())
            {
                return systemTenantAppender;
            }
            else
            {
                RollingFileAppender appender = new RollingFileAppender();
                // Copy properties from the system tenant appender
                appender.setAppend(systemTenantAppender.getAppend());
                appender.setBufferedIO(systemTenantAppender.getBufferedIO());
                appender.setBufferSize(systemTenantAppender.getBufferSize());
                appender.setEncoding(systemTenantAppender.getEncoding());
                appender.setErrorHandler(systemTenantAppender.getErrorHandler());
                appender.setImmediateFlush(systemTenantAppender.getImmediateFlush());
                appender.setLayout(systemTenantAppender.getLayout());
                appender.setMaxBackupIndex(systemTenantAppender.getMaxBackupIndex());
                appender.setMaximumFileSize(systemTenantAppender.getMaximumFileSize());
                appender.setName(systemTenantAppender.getName() + "-" + tenant.getName());
                appender.setThreshold(systemTenantAppender.getThreshold());
                final File homeLogFile = JiraHomeAppender.getHomeLogFile(new File(tenant.getHomeDir()), fileName);
                if (homeLogFile != null)
                {
                    appender.setFile(homeLogFile.getAbsolutePath());
                }
                else
                {
                    appender.setFile(fileName);
                }
                appender.activateOptions();
                return appender;
            }
        }
    }

    @Override
    public void addFilter(Filter newFilter)
    {
        getAppender().addFilter(newFilter);
    }

    @Override
    public Filter getFilter()
    {
        return getAppender().getFilter();
    }

    public Filter getFirstFilter()
    {
        return systemTenantAppender.getFirstFilter();
    }

    @Override
    public void clearFilters()
    {
        getAppender().clearFilters();
    }

    @Override
    public void close()
    {
        getAppender().close();
    }

    public String getEncoding()
    {
        return systemTenantAppender.getEncoding();
    }

    public void setEncoding(String value)
    {
        systemTenantAppender.setEncoding(value);
    }

    @Override
    public void doAppend(LoggingEvent event)
    {
        getAppender().doAppend(event);
    }

    @Override
    public String getName()
    {
        return getAppender().getName();
    }

    public Priority getThreshold()
    {
        return systemTenantAppender.getThreshold();
    }

    @Override
    public void setErrorHandler(ErrorHandler errorHandler)
    {
        getAppender().setErrorHandler(errorHandler);
    }

    @Override
    public ErrorHandler getErrorHandler()
    {
        return getAppender().getErrorHandler();
    }

    @Override
    public void setLayout(Layout layout)
    {
        getAppender().setLayout(layout);
    }

    @Override
    public Layout getLayout()
    {
        return getAppender().getLayout();
    }

    @Override
    public void setName(String name)
    {
        getAppender().setName(name);
    }

    public void setThreshold(Priority threshold)
    {
        systemTenantAppender.setThreshold(threshold);
    }

    @Override
    public boolean requiresLayout()
    {
        return getAppender().requiresLayout();
    }

    public int getMaxBackupIndex()
    {
        return systemTenantAppender.getMaxBackupIndex();
    }

    public long getMaximumFileSize()
    {
        return systemTenantAppender.getMaximumFileSize();
    }

    public void setMaxBackupIndex(int maxBackups)
    {
        systemTenantAppender.setMaxBackupIndex(maxBackups);
    }

    public void setMaximumFileSize(long maxFileSize)
    {
        systemTenantAppender.setMaximumFileSize(maxFileSize);
    }

    public void setMaxFileSize(String value)
    {
        systemTenantAppender.setMaxFileSize(value);
    }

    public void setFile(String file)
    {
        this.fileName = file;
        systemTenantAppender.setFile(file);
    }

    public boolean getAppend()
    {
        return systemTenantAppender.getAppend();
    }

    public String getFile()
    {
        return systemTenantAppender.getFile();
    }

    @Override
    public void activateOptions()
    {
        systemTenantAppender.activateOptions();
    }

    public boolean getBufferedIO()
    {
        return systemTenantAppender.getBufferedIO();
    }

    public int getBufferSize()
    {
        return systemTenantAppender.getBufferSize();
    }

    public void setAppend(boolean flag)
    {
        systemTenantAppender.setAppend(flag);
    }

    public void setBufferedIO(boolean bufferedIO)
    {
        systemTenantAppender.setBufferedIO(bufferedIO);
    }

    public void setBufferSize(int bufferSize)
    {
        systemTenantAppender.setBufferSize(bufferSize);
    }

    public void setImmediateFlush(boolean value)
    {
        systemTenantAppender.setImmediateFlush(value);
    }

    public boolean getImmediateFlush()
    {
        return systemTenantAppender.getImmediateFlush();
    }

    public void rollOver()
    {
        Appender appender = getAppender();
        if (appender instanceof RollOverLogAppender)
        {
            ((RollOverLogAppender) appender).rollOver();
        }
    }
}
