package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.jql.util.JqlDateSupportImpl;
import com.atlassian.jira.timezone.TimeZoneManager;
import electric.xml.Element;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Date;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Class that reads in old skool AbsoluteDateRangeParameter parameters. For example:
 * <pre>
 * &lt;parameter class='com.atlassian.jira.issue.search.parameters.lucene.AbsoluteDateRangeParameter'>
 *   &lt;created name='created:absolute'&gt;
 *      &lt;fromDate&gt;1229392800000&lt;/fromDate&gt;
 *      &lt;toDate&gt;1260975600000&lt;/toDate&gt;
 *   &lt;/created&gt;
 * &lt;/parameter&gt;
 * </pre>
 * @since v4.0
 */
public class AbsoluteDateXmlHandler extends AbstractDateXmlHandler
{
    private static final Logger log = Logger.getLogger(AbsoluteDateXmlHandler.class);

    private static final String ELEMENT_FROM_DATE = "fromDate";
    private static final String ELEMENT_TO_DATE = "toDate";

    private final JqlDateSupport dateSupport;

    public AbsoluteDateXmlHandler(final Collection<String> supportedXmlFieldNames, TimeZoneManager timeZoneManager)
    {
        this(supportedXmlFieldNames, new JqlDateSupportImpl(timeZoneManager));
    }

    AbsoluteDateXmlHandler(final Collection<String> supportedXmlFieldNames, final JqlDateSupport dateSupport)
    {
        super(supportedXmlFieldNames);
        this.dateSupport = notNull("dateSupport", dateSupport);
    }

    protected String getLowerBound(final String fieldName, final Element element)
    {
        return getBound(fieldName, element, ELEMENT_FROM_DATE);
    }

    protected String getUpperBound(final String fieldName, final Element element)
    {
        return getBound(fieldName, element, ELEMENT_TO_DATE);
    }

    private String getBound(final String fieldName, final Element element, final String subElement)
    {
        final String dateString = JqlXmlSupport.getTextFromSubElement(element, subElement);
        if (dateString != null)
        {
            try
            {
                long dateLong = Long.parseLong(dateString);
                return dateSupport.getDateString(new Date(dateLong));
            }
            catch (NumberFormatException e)
            {
                log.warn(String.format("Date parameter '%s' in element '%s' is and invalid date for field '%s'.", dateString, subElement, fieldName));
            }
        }
        return null;
    }
}
