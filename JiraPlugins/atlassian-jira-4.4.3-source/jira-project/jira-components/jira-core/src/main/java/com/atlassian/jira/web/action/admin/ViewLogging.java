package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.util.log.LogMarker;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.atlassian.util.profiling.UtilTimerStack;
import com.google.common.collect.ImmutableList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The view action for the JIRA Profiling and Logging Admin section
 */
@WebSudoRequired
public class ViewLogging extends JiraWebActionSupport
{
    private List<Logger> loggers;
    private Logger rootLogger;

    private String markMessage;
    private boolean rollOver;

    private static final Collection<Level> availableLevels = ImmutableList.of(Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL, Level.OFF);
    private static final String VIEW_LOGGING_JSPA = "ViewLogging.jspa";
    private static final String HASH_HTTP = "#http";
    private static final String HASH_SOAP = "#soap";
    private static final String HASH_SQL = "#sql";
    private static final String HASH_PROFILING = "#profiling";

    private static final String SOAP_ACCESS_LOG = "com.atlassian.jira.soap.axis.JiraAxisSoapLog";
    private static final String SOAP_DUMP_LOG = "com.atlassian.jira.soap.axis.JiraAxisSoapLogDump";
    private static final String HTTP_ACCESS_LOG = "com.atlassian.jira.web.filters.accesslog.AccessLogFilter";
    private static final String HTTP_DUMP_LOG = "com.atlassian.jira.web.filters.accesslog.AccessLogFilterDump";
    private static final String HTTP_ACCESS_LOG_INCLUDE_IMAGES = "com.atlassian.jira.web.filters.accesslog.AccessLogFilterIncludeImages";

    private static final String SQL_LOG = "com.atlassian.jira.ofbiz.LoggingSQLInterceptor";

    private static final Set<String> LOGGER_NAME_EXEMPTION_SET;

    static
    {
        Set<String> set = new HashSet<String>();
        set.add(SQL_LOG);
        set.add(SOAP_ACCESS_LOG);
        set.add(SOAP_DUMP_LOG);
        set.add(HTTP_ACCESS_LOG);
        set.add(HTTP_DUMP_LOG);
        set.add(HTTP_ACCESS_LOG_INCLUDE_IMAGES);
        LOGGER_NAME_EXEMPTION_SET = Collections.unmodifiableSet(set);
    }

    public Collection getLoggers()
    {
        if (loggers == null)
        {
            loggers = new ArrayList<Logger>();
            final Enumeration currentLoggers = LogManager.getCurrentLoggers();
            while (currentLoggers.hasMoreElements())
            {
                Logger logger = (Logger) currentLoggers.nextElement();

                //only display categories that have an explicit level
                if (logger.getLevel() != null)
                {
                    if (!LOGGER_NAME_EXEMPTION_SET.contains(logger.getName()))
                    {
                        loggers.add(logger);
                    }
                }
            }

            Collections.sort(loggers, new LoggerComparator());
        }
        return loggers;
    }


    public Logger getRootLogger()
    {
        if (rootLogger == null)
        {
            rootLogger = Logger.getRootLogger();
        }
        return rootLogger;
    }

    public Collection getAvailableLevels()
    {
        return availableLevels;
    }

   //---------Log mark related methods ------------------------//

     public String doMarkLogs() throws Exception
    {
        String msg = getMarkMessage();
        if (rollOver)
        {
            LogMarker.rolloverAndMark(msg);
        }
        else
        {
            LogMarker.markLogs(msg);
        }
        return getRedirect(VIEW_LOGGING_JSPA);
    }


    //---------Profiling related methods ------------------------//

    public String doEnableProfiling() throws Exception
    {
        System.setProperty(UtilTimerStack.MIN_TIME, "1");
        UtilTimerStack.setActive(true);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_PROFILING);
    }

    public String doDisableProfiling() throws Exception
    {
        UtilTimerStack.setActive(false);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_PROFILING);
    }

    public boolean isProfilingEnabled()
    {
        return UtilTimerStack.isActive();
    }

    //---------SOAP related methods ------------------------//

    public String doEnableSoapAccessLog()
    {
        getSoapAccessLogger().setLevel(Level.INFO);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_SOAP);
    }

    public String doDisableSoapAccessLog()
    {
        // always disable these as a pair.  The first is a prerequisite
        // to the second
        getSoapAccessLogger().setLevel(Level.OFF);
        getSoapDumpLogger().setLevel(Level.OFF);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_SOAP);
    }

    public String doEnableSoapDumpLog()
    {
        getSoapDumpLogger().setLevel(Level.INFO);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_SOAP);
    }

    public String doDisableSoapDumpLog()
    {
        getSoapDumpLogger().setLevel(Level.OFF);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_SOAP);
    }

    private Logger getSoapAccessLogger()
    {
        return Logger.getLogger(SOAP_ACCESS_LOG);
    }

    private Logger getSoapDumpLogger()
    {
        return Logger.getLogger(SOAP_DUMP_LOG);
    }

    public boolean isSoapAccessLogEnabled()
    {
        return getSoapAccessLogger().getLevel() != Level.OFF;
    }

    public boolean isSoapDumpLogEnabled()
    {
        return getSoapDumpLogger().getLevel() != Level.OFF;
    }

    //---------HTTP related methods ------------------------//

    public String doEnableHttpAccessLog()
    {
        getHttpAccessLogger().setLevel(Level.INFO);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_HTTP);
    }

    public String doDisableHttpAccessLog()
    {
        // always do these in pairs
        getHttpAccessLogger().setLevel(Level.OFF);
        getHttpAccessIncludeImagesLogger().setLevel(Level.OFF);
        getHttpDumpLogger().setLevel(Level.OFF);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_HTTP);
    }

    private Logger getHttpAccessLogger()
    {
        return Logger.getLogger(HTTP_ACCESS_LOG);
    }

    public boolean isHttpAccessLogEnabled()
    {
        return getHttpAccessLogger().getLevel() != Level.OFF;
    }

    public String doEnableHttpDumpLog()
    {
        getHttpDumpLogger().setLevel(Level.INFO);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_HTTP);
    }

    public String doDisableHttpDumpLog()
    {
        getHttpDumpLogger().setLevel(Level.OFF);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_HTTP);
    }

    private Logger getHttpDumpLogger()
    {
        return Logger.getLogger(HTTP_DUMP_LOG);
    }

    public boolean isHttpDumpLogEnabled()
    {
        return getHttpDumpLogger().getLevel() != Level.OFF;
    }


    public String doEnableHttpAccessLogIncludeImages()
    {
        getHttpAccessIncludeImagesLogger().setLevel(Level.INFO);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_HTTP);
    }

    public String doDisableHttpAccessLogIncludeImages()
    {
        getHttpAccessIncludeImagesLogger().setLevel(Level.OFF);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_HTTP);
    }

    private Logger getHttpAccessIncludeImagesLogger()
    {
        return Logger.getLogger(HTTP_ACCESS_LOG_INCLUDE_IMAGES);
    }

    public boolean isHttpAccessLogIncludeImagesEnabled()
    {
        return getHttpAccessIncludeImagesLogger().getLevel() != Level.OFF;
    }

    //---------SQL related methods ------------------------//

    public String doEnableSqlLog()
    {
        getSqlLogger().setLevel(Level.INFO);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_SQL);
    }

    public String doDisableSqlLog()
    {
        getSqlLogger().setLevel(Level.OFF);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_SQL);
    }

    public String doEnableSqlDumpLog()
    {
        getSqlLogger().setLevel(Level.DEBUG);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_SQL);
    }

    public String doDisableSqlDumpLog()
    {
        getSqlLogger().setLevel(Level.INFO);
        return getRedirect(VIEW_LOGGING_JSPA + HASH_SQL);
    }

    private Logger getSqlLogger()
    {
        return Logger.getLogger(SQL_LOG);
    }

    public boolean isSqlLogEnabled()
    {
        return getSqlLogger().getLevel() != Level.OFF;
    }

    public boolean isSqlDumpLogEnabled()
    {
        return getSqlLogger().getLevel() == Level.DEBUG;
    }


    public boolean isAtLevel(final Logger logger, final String targetLevel)
    {
        final String loggerLevelName = logger.getEffectiveLevel().toString();
        return targetLevel.equals(loggerLevelName);
    }

    public String getMarkMessage()
    {
        return markMessage;
    }

    public void setMarkMessage(String markMessage)
    {
        this.markMessage = markMessage;
    }

    public boolean isRollOver()
    {
        return rollOver;
    }

    public void setRollOver(boolean rollOver)
    {
        this.rollOver = rollOver;
    }

    private static class LoggerComparator implements Comparator<Logger>
    {
        public int compare(final Logger o1, final Logger o2)
        {
            if (o1 == null || o2 == null)
            {
                return 0; //lazy
            }

            String name1 = o1.getName();
            String name2 = o2.getName();

            if (name1 == null || name2 == null)
            {
                return 0; //lazy
            }
            else
            {
                return name1.compareTo(name2);
            }
        }
    }

}
