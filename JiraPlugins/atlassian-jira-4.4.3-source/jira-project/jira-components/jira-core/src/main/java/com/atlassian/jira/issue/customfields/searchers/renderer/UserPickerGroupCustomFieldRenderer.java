package com.atlassian.jira.issue.customfields.searchers.renderer;

import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.UserFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.renderer.AbstractUserSearchRenderer;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @since v4.0
 */
public class UserPickerGroupCustomFieldRenderer extends AbstractUserSearchRenderer implements SearchRenderer
{
    private static final String KEY_VALUE = "value";
    private static final String KEY_KEY = "key";
    private static final String KEY_RELATED = "related";

    private final CustomField field;
    private final FieldVisibilityManager fieldVisibilityManager;

    public UserPickerGroupCustomFieldRenderer(CustomField field, final UserFieldSearchConstants searchConstants, final String nameKey,
            final VelocityRequestContextFactory velocityRequestContextFactory, final ApplicationProperties applicationProperties,
            final VelocityManager velocityManager, final UserPickerSearchService searchService, final FieldVisibilityManager fieldVisibilityManager)
    {
        super(searchConstants, nameKey, velocityRequestContextFactory, applicationProperties, velocityManager, searchService);
        this.field = field;
        this.fieldVisibilityManager = fieldVisibilityManager;
    }

    public boolean isShown(final User searcher, final SearchContext searchContext)
    {
        return CustomFieldUtils.isShownAndVisible(getCustomField(), searcher, searchContext, fieldVisibilityManager);
    }

    /**
     * Returns a list of select box options
     * @param searcher  performing this action.
     * @return a list of select box options
     */
    @Override
    protected List<Map<String, String>> getSelectedListOptions(final User searcher)
    {
        final List<Map<String, String>> types = new ArrayList<Map<String, String>>();

        final I18nHelper i18n = new I18nBean(searcher);

        types.add(MapBuilder.<String, String>newBuilder().add(KEY_VALUE, i18n.getText("assignee.types.anyuser")).add(KEY_KEY, null).add(KEY_RELATED, AbstractUserSearchRenderer.SELECT_LIST_NONE).toHashMap());

        // Note that its not possible to search custom fields for no value, and hence the option for 'no value' is missing.

        // If the current user is null (not logged in) do not include the "Current User" as one of the options
        // Fixes: JRA-3341
        if (searcher != null)
        {
            types.add(MapBuilder.<String, String>newBuilder().add(KEY_VALUE, i18n.getText("assignee.types.currentuser"))
                    .add(KEY_KEY, DocumentConstants.ISSUE_CURRENT_USER).add(KEY_RELATED, AbstractUserSearchRenderer.SELECT_LIST_NONE).toHashMap());
        }

        types.add(MapBuilder.<String, String>newBuilder().add(KEY_VALUE, i18n.getText("assignee.types.specifyuser")).add(KEY_KEY, DocumentConstants.SPECIFIC_USER).add(KEY_RELATED,
            AbstractUserSearchRenderer.SELECT_LIST_USER).toHashMap());
        types.add(MapBuilder.<String, String>newBuilder().add(KEY_VALUE, i18n.getText("assignee.types.specifygroup")).add(KEY_KEY, DocumentConstants.SPECIFIC_GROUP).add(KEY_RELATED,
            AbstractUserSearchRenderer.SELECT_LIST_GROUP).toHashMap());

        return types;
    }

    @Override
    protected String getEmptyValueKey()
    {
        return "should_never_be_selected";
    }

    private CustomField getCustomField()
    {
        return field;
    }
}
