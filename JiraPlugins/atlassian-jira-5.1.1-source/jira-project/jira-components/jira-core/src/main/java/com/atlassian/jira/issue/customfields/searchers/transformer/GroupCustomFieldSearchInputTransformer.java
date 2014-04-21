package com.atlassian.jira.issue.customfields.searchers.transformer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.converters.GroupConverter;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.query.Query;

/**
 * The {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer} for group custom fields.
 *
 * @since v4.0
 */
public class GroupCustomFieldSearchInputTransformer extends AbstractSingleValueCustomFieldSearchInputTransformer implements SearchInputTransformer
{
    private final CustomField customField;
    private final GroupConverter groupConverter;

    public GroupCustomFieldSearchInputTransformer(CustomField customField, ClauseNames clauseNames, String urlParameterName,
            GroupConverter groupConverter, final CustomFieldInputHelper customFieldInputHelper)
    {
        super(customField, clauseNames, urlParameterName, customFieldInputHelper);
        this.customField = customField;
        this.groupConverter = groupConverter;
    }

    @Override
    public void validateParams(final User searcher, final SearchContext searchContext, final FieldValuesHolder fieldValuesHolder, final I18nHelper i18nHelper, final ErrorCollection errors)
    {
        final CustomFieldParams custParams = (CustomFieldParams) fieldValuesHolder.get(customField.getId());
        if (custParams == null)
        {
            return;
        }
        final String groupName = (String) custParams.getFirstValueForNullKey();
        try
        {
            groupConverter.getGroup(groupName);
        }
        catch (final FieldValidationException e)
        {
            errors.addError(customField.getId(), i18nHelper.getText("admin.errors.could.not.find.groupname", groupName));
        }
    }

    ///CLOVER:OFF
    public boolean doRelevantClausesFitFilterForm(final User searcher, final Query query, final SearchContext searchContext)
    {
        return convertForNavigator(query).fitsNavigator();
    }
    ///CLOVER:ON
}