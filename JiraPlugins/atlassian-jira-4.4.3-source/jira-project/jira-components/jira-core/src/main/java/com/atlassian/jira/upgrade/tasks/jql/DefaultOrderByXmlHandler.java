package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.atlassian.query.order.OrderByImpl;
import com.atlassian.query.order.SearchSort;
import electric.xml.Element;
import electric.xml.Elements;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of {@link com.atlassian.jira.upgrade.tasks.jql.OrderByXmlHandler}.
 * <p/>
 * <pre>
 *  &lt;sort class='com.atlassian.query.order.SearchSort'&gt;
 *      &lt;searchSort field='issuekey' order='DESC'/&gt;
 *  &lt;/sort&gt;
 *  &lt;sort class='com.atlassian.query.order.SearchSort'&gt;
 *      &lt;searchSort field='summary' order='ASC'/&gt;
 *  &lt;/sort&gt;
 * </pre>
 *
 * @since v4.0
 */
public class DefaultOrderByXmlHandler implements OrderByXmlHandler
{
    private static final String OLD_SORT_CLASS = "com.atlassian.jira.issue.search.SearchSort";

    private static final Logger log = Logger.getLogger(DefaultOrderByXmlHandler.class);

    private final SearchHandlerManager searchHandlerManager;

    public DefaultOrderByXmlHandler(final SearchHandlerManager searchHandlerManager)
    {
        this.searchHandlerManager = notNull("searchHandlerManager", searchHandlerManager);
    }

    public OrderByConversionResults getOrderByFromXml(final Elements elements)
    {
        final List<ConversionError> conversionErrors = new ArrayList<ConversionError>();
        final List<SearchSort> searchSorts = new ArrayList<SearchSort>();

        final Collection<String> sortFieldNames = new HashSet<String>();

        while (elements.hasMoreElements())
        {
            final Element element = (Element) elements.nextElement();
            final String sortClass = element.getAttributeValue("class");

            if (!OLD_SORT_CLASS.equals(sortClass))
            {
                log.warn("Unexpected sorting class '" + sortClass + "' encountered while converting search sorts.");
            }

            final Element sortEl = element.getElement("searchSort");

            if (sortEl == null)
            {
                log.warn("Ignoring invalid sort detected during upgrade. Sort was '" + element + "'.");
                conversionErrors.add(new ConversionError("jira.jql.upgrade.order.by.error.general", null));
                continue;
            }

            final String field = sortEl.getAttribute("field");
            if (StringUtils.isBlank(field))
            {
                log.warn("Ignoring invalid sort detected during upgrade. Sort was '" + element + "'.");
                conversionErrors.add(new ConversionError("jira.jql.upgrade.order.by.error.general", null));
                continue;
            }

            if (sortFieldNames.contains(field))
            {
                log.warn("Skipping sort since field name '" + field + "' has already been included in the sorts.");
                continue;
            }
            sortFieldNames.add(field);

            final Collection<ClauseNames> names = searchHandlerManager.getJqlClauseNames(field);
            if (names.isEmpty())
            {
                final Long cfId = getId(field);

                // Lets always convert custom fields, they are easy
                if (cfId == null)
                {
                    log.warn("Ignoring invalid sort, unable to find JQL clause name for field '" + field + "'.");
                    conversionErrors.add(new ConversionError("jira.jql.upgrade.order.by.error.can.not.convert", field));
                }
                else
                {
                    log.warn("Creating a sort for a custom field with id '" + field + "' which is not found in the system.");
                    final String order = sortEl.getAttribute("order");
                    final SearchSort sort = new SearchSort(order, JqlCustomFieldId.toString(cfId));
                    searchSorts.add(sort);
                }
            }
            else
            {
                //MOVE ALONG NOTHING TO SEE HERE: Sorting of results is actually done by the field, thus we
                // don't care which JQL clause name we sort with.
                final ClauseNames name = names.iterator().next();

                final String order = sortEl.getAttribute("order");
                final SearchSort sort = new SearchSort(order, name.getPrimaryName());
                searchSorts.add(sort);
            }
        }

        // The search sorts were stored in reverse order and then the LuceneQueryCreator ran through them backwards,
        // this makes no sense, so lets not do it.
        Collections.reverse(searchSorts);

        return new OrderByConversionResults(new OrderByImpl(searchSorts), conversionErrors);
    }

    private Long getId(final String elementName)
    {
        try
        {
            return Long.parseLong(elementName.substring(FieldManager.CUSTOM_FIELD_PREFIX.length()));
        }
        catch (Exception e)
        {
            return null;
        }
    }


}
