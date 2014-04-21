package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.order.OrderBy;
import electric.xml.Elements;

import java.util.List;

/**
 * Converts a pre 4.0 search sort from a SearchRequest XML into a OrderBy clause for JQL.
 *
 * @since v4.0
 */
public interface OrderByXmlHandler
{
    /**
     * Produce an OrderBy clase from pre JIRA 4.0 XML request. The old XML looks like:
     *
     * <pre>
     *  &lt;sort class='com.atlassian.query.order.SearchSort'&gt;
     *      &lt;searchSort field='issuekey' order='DESC'/&gt;
     *  &lt;/sort&gt;
     *  &lt;sort class='com.atlassian.query.order.SearchSort'&gt;
     *      &lt;searchSort field='summary' order='ASC'/&gt;
     *  &lt;/sort&gt;
     * </pre>
     *
     * @param elements the {@code Sort} elements from the XML.
     * @return the conversion results containing the converted OrderBy clause and any conversion errors that occurred, not null.
     */
    OrderByConversionResults getOrderByFromXml(Elements elements);

    static class OrderByConversionResults
    {
        private final OrderBy convertedOrderBy;
        private final List<ConversionError> conversionErrors;

        public OrderByConversionResults(final OrderBy convertedOrderBy, final List<ConversionError> conversionErrors)
        {
            this.convertedOrderBy = convertedOrderBy;
            this.conversionErrors = conversionErrors;
        }

        public OrderBy getConvertedOrderBy()
        {
            return convertedOrderBy;
        }

        public List<ConversionError> getConversionErrors()
        {
            return conversionErrors;
        }
    }

    public class ConversionError
    {
        private final String messageKey;
        private final String messageValue;

        public ConversionError(final String messageKey, final String messageValue)
        {
            this.messageKey = messageKey;
            this.messageValue = messageValue;
        }

        /**
         * @param i18nHelper used to i18n the message
         * @param savedFilterName the name of the filter this result was generated for.
         * @return a message explaining what happened as a result of the conversion.
         */
        public String getMessage(I18nHelper i18nHelper, String savedFilterName)
        {
            return i18nHelper.getText(messageKey, savedFilterName, messageValue);
        }
    }

}
