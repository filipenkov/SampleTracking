package com.atlassian.jira.functest.framework.log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A logging implementation with the concept of "indentation" for hirearchical levels of logging.  Really aimed at
 * making the output easier to read, especiallly in the Bamboo cargo logs.
 *
 * @since v3.13
 */
public class FuncTestLoggerImpl implements FuncTestLogger
{
    private static final char LOG_PREFIX_CHAR = '.';

    private final int indentLevel;

    public FuncTestLoggerImpl()
    {
        this(0);
    }

    public FuncTestLoggerImpl(int indetLevel)
    {
        this.indentLevel = indetLevel;
    }

    public void log(Object logData)
    {
        logIndented(this.indentLevel, logData);
    }


    public void log(Throwable t)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        logIndented(this.indentLevel, sw);
    }

    /**
     * This will log to a StringBuffer first if need be and replace the new lines with "indent + newline"
     *
     * @param logData the data to log
     */
    static void logIndented(Object logData)
    {
        logIndented(0, logData);
    }

    /**
     * This will log to a StringBuffer first if need be and replace the new lines with "indent + newline"
     *
     * @param indentLevel the indented level required
     * @param logData the data to log
     */
    static void logIndented(int indentLevel, Object logData)
    {
        String logDataStr = String.valueOf(logData);
        String indentStr = buildIndentStr(indentLevel);

        if (logDataStr.indexOf('\n') != -1)
        {
            StringBuilder sb = new StringBuilder(indentStr);
            sb.append(logDataStr);

            indentStr = '\n' + indentStr;
            int index = sb.indexOf("\n");
            while (index != -1)
            {
                sb.replace(index, index + 1, indentStr);
                index = sb.indexOf("\n", index + indentStr.length());
            }
            logDataStr = sb.toString();
            FuncTestOut.out.println(logDataStr);
        }
        else
        {
            FuncTestOut.out.print(indentStr);
            FuncTestOut.out.println(logDataStr);
        }
    }

    private static String buildIndentStr(int indentLevel)
    {
        StringBuilder sb = new StringBuilder();
        if (indentLevel == 0)
        {
            sb.append(LOG_PREFIX_CHAR);
        }
        else if (indentLevel >= 1)
        {
            sb.append('\t');
            sb.append(LOG_PREFIX_CHAR);
        }
        for (int i = 1; i < indentLevel; i++)
        {
            sb.append('\t');
        }

        sb.append(' ');
        return sb.toString();
    }
}
