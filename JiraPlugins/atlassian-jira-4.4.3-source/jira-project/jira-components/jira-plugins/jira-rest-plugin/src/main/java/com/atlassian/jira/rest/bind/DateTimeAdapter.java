package com.atlassian.jira.rest.bind;

import com.atlassian.jira.rest.api.Dates;

import java.sql.Timestamp;
import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapter for marshalling and unmarshalling date/time objects, or instants in time. Note that this class should
 * <b>not</b> be used to marshall calendar dates that do not represent a specific point in time (e.g. ). In those cases
 * {@link DateAdapter} should be used.
 * <p/>
 * The wire format used is <code>'{@value Dates#TIME_FORMAT}'</code>.
 *
 * @see com.atlassian.jira.rest.api.Dates
 * @since v4.1
 */
public class DateTimeAdapter extends XmlAdapter<String, Date>
{
    @Override
    public Date unmarshal(final String s) throws Exception
    {
        return Dates.fromTimeString(s);
    }

    @Override
    public String marshal(final Date date) throws Exception
    {
        return Dates.asTimeString(date);
    }
}
