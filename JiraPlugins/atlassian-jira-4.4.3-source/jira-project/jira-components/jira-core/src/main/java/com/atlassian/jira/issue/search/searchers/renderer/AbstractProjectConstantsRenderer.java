package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.option.Option;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstantsWithEmpty;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.query.Query;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.user.User;

import java.util.Collection;
import java.util.List;

/**
 * An abstract renderer for the project constants (versions and components).
 *
 * @since v4.0
 */
public abstract class AbstractProjectConstantsRenderer extends AbstractSearchRenderer implements SearchRenderer
{
    private final SimpleFieldSearchConstantsWithEmpty searchConstants;
    private final FieldVisibilityManager fieldVisibilityManager;

    public AbstractProjectConstantsRenderer(VelocityRequestContextFactory velocityRequestContextFactory,
            ApplicationProperties applicationProperties, VelocityManager velocityManager, FieldVisibilityManager fieldVisibilityManager,
            SimpleFieldSearchConstantsWithEmpty searchConstants, String searcherNameKey)
    {
        super(velocityRequestContextFactory, applicationProperties, velocityManager, searchConstants.getSearcherId(), searcherNameKey);
        this.searchConstants = searchConstants;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    public boolean isRelevantForQuery(final User searcher, Query query)
    {
        return isRelevantForQuery(searchConstants.getJqlClauseNames(), query);
    }

    public boolean isShown(final User searcher, SearchContext searchContext)
    {
        return searchContext.isSingleProjectContext() &&
                !getSelectListOptions(searcher, searchContext).isEmpty() &&
                !fieldVisibilityManager.isFieldHiddenInAllSchemes(searchConstants.getFieldId(), searchContext, searcher);
    }

    abstract List<Option> getSelectListOptions(final User searcher, SearchContext seacrchContext);

    Collection<GenericProjectConstantsLabel> getSelectedObjects(FieldValuesHolder fieldValuesHolder, Function<String, GenericProjectConstantsLabel> function)
    {
        Collection<String> selectedValues = (Collection<String>)fieldValuesHolder.get(searchConstants.getUrlParameter());
        if (selectedValues != null && !selectedValues.isEmpty())
        {
            return CollectionUtil.transform(selectedValues, function);
        }
        else
        {
            return null;
        }
    }

    /**
     * A label with an optional url to the browse page
     *
     * @since v4.0
     */
    public static class GenericProjectConstantsLabel
    {
        private final String browseUrl;
        private final String label;

        /**
         *
         * @param label the label
         */
        public GenericProjectConstantsLabel(String label)
        {
            this(label, null);
        }

        /**
         *
         * @param label the label
         * @param browseUrl the url linking the label to the browse page
         */
        public GenericProjectConstantsLabel(String label, String browseUrl)
        {
            this.browseUrl = browseUrl;
            this.label = label;
        }

        public String getLabel()
        {
            return label;
        }

        public String getBrowseUrl()
        {
            return browseUrl;
        }
    }
}
