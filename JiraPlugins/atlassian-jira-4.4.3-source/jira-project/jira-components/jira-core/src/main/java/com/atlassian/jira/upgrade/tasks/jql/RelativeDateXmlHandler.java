package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.jql.util.JqlDateSupportImpl;
import com.atlassian.jira.util.Function;
import electric.xml.Element;
import org.apache.log4j.Logger;

import java.util.Collection;

import static com.atlassian.util.concurrent.Assertions.notNull;

/**
 * Class that reads in old skool RelativeDateXmlHandler parameters. For example:
 * <pre>
 * &lt;parameter class='com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter'>
 *   &lt;created name='created:relative'&gt;
 *   &lt;previousOffset&gt;259200000&lt;/previousOffset&gt;
 *   &lt;nextOffset&gt;86400000&lt;/nextOffset&gt;
 *   &lt;/created&gt;
 * &lt;/parameter&gt;
 * </pre>
 *
 * @since v4.0
 */
public class RelativeDateXmlHandler extends AbstractDateXmlHandler
{
    private static final Logger log = Logger.getLogger(RelativeDateXmlHandler.class);

    private static final String ELEMENT_FROM_DURATION = "previousOffset";
    private static final String ELEMENT_TO_DURATION = "nextOffset";

    private final Function<Long, String> durationConverter;

    public RelativeDateXmlHandler(final Collection<String> supportedXmlFieldNames)
    {
        this(supportedXmlFieldNames, new DurationConverter());
    }

    RelativeDateXmlHandler(final Collection<String> supportedXmlFieldNames, final Function<Long, String> durationConverter)
    {
        super(supportedXmlFieldNames);
        this.durationConverter = notNull("durationConverter", durationConverter);
    }

    protected String getLowerBound(final String fieldName, final Element element)
    {
        return getBound(fieldName, element, ELEMENT_FROM_DURATION);
    }

    protected String getUpperBound(final String fieldName, final Element element)
    {
        return getBound(fieldName, element, ELEMENT_TO_DURATION);
    }

    private String getBound(final String fieldName, final Element element, final String subElement)
    {
        final String dateString = JqlXmlSupport.getTextFromSubElement(element, subElement);
        if (dateString != null)
        {
            try
            {
                long dateLong = Long.parseLong(dateString);
                return durationConverter.get(dateLong);
            }
            catch (NumberFormatException e)
            {
                log.warn(String.format("Date parameter '%s' in element '%s' is and invalid duration for field '%s'.", dateString, subElement, fieldName));
            }
        }
        return null;
    }

    /**
     * When dealing in relative dates, we need to render saved offsets from XML in duration format for the JQL query string.
     */
    static final class DurationConverter implements Function<Long, String>
    {
        public String get(final Long input)
        {
            //The SearchParameters store their offsets, such that the relative date is calculated by subtracting the offset
            //from the current date. E.g. if the user enters "-3d", the offset will be stored as "+(number of milliseconds in 3 days)"
            return JqlDateSupportImpl.getDurationString(-input);
        }
    }

}
