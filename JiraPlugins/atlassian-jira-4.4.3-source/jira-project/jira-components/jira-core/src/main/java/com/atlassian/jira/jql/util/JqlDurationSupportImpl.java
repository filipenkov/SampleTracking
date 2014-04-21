package com.atlassian.jira.jql.util;

import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.InjectableComponent;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.NumberTools;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * The default implementation of {@link com.atlassian.jira.jql.util.JqlDurationSupport}
 *
 * @since v4.0
 */
@InjectableComponent
public class JqlDurationSupportImpl implements JqlDurationSupport
{
    private static final Logger log = Logger.getLogger(JqlDurationSupportImpl.class);

    public boolean validate(final String durationString, final boolean allowNegatives)
    {
        try
        {
            final String trimDuration = StringUtils.trimToNull(durationString);
            if (trimDuration != null)
            {
                final Long value = parseDurationWithNegative(trimDuration);
                return allowNegatives || value >= 0;
            }
        }
        catch (InvalidDurationException e)
        {
            // return false
        }
        catch (NumberFormatException e)
        {
            // return false
        }
        return false;
    }

    public String convertToIndexValue(final QueryLiteral rawValue)
    {
        notNull("rawValue", rawValue);
        if (rawValue.getLongValue() != null)
        {
            return convertToIndexValue(rawValue.getLongValue());
        }
        else if (rawValue.getStringValue() != null)
        {
            return convertToIndexValue(rawValue.getStringValue());
        }
        return null;
    }

    /**
     * Convenience method. See {@link #convertToDuration(String)}.
     *
     * Converts the duration in minutes into its duration in seconds.
     *
     * @param durationInMinutes the duration
     * @return the duration in seconds; null if there was a problem
     */
    public Long convertToDuration(final Long durationInMinutes)
    {
        notNull("durationInMinutes", durationInMinutes);
        return convertToDuration(durationInMinutes.toString());
    }

    /**
     * Converts the formatted duration string into its duration in seconds.
     *
     * @param durationString the formatted duration string
     * @return the duration in seconds; null if there was a problem
     */
    public Long convertToDuration(final String durationString)
    {
        notNull("durationString", durationString);
        final String trimDuration = StringUtils.trimToNull(durationString);
        if (trimDuration != null)
        {
            try
            {
                return parseDurationWithNegative(trimDuration);
            }
            catch (InvalidDurationException e)
            {
                log.debug("Specified duration was not valid: '" + durationString + "'.", e);
            }
            catch (NumberFormatException e)
            {
                log.debug("Specified duration was not valid: '" + durationString + "'.", e);
            }
        }
        return null;
    }

    /**
     * Convenience method. See {@link #convertToIndexValue(String)}.
     *
     * Converts the specified duration into the format used by the index.
     *
     * @param durationInMinutes the duration
     * @return the index representation of the duration value in seconds
     */
    protected String convertToIndexValue(final Long durationInMinutes)
    {
        final Long l = convertToDuration(durationInMinutes);
        if (l != null)
        {
            return NumberTools.longToString(l);
        }
        return null;
    }

    /**
     * Converts the specified duration into the format used by the index.
     *
     * @param durationString the formatted duration string
     * @return the index representation of the duration value in seconds
     */
    protected String convertToIndexValue(final String durationString)
    {
        final String trimDuration = StringUtils.trimToNull(durationString);
        if (trimDuration != null)
        {
            final Long l = convertToDuration(trimDuration);
            if (l != null)
            {
                return NumberTools.longToString(l);
            }
        }
        return null;
    }

    Long parseDurationWithNegative(final String str) throws InvalidDurationException
    {
        return DateUtils.getDurationWithNegative(str);
    }
}
