package com.atlassian.jira.issue.fields;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import webwork.action.Action;

import java.util.Collection;
import java.util.Map;

/**
 * A very simple {@link OrderableField} field.
 *
 * @since v4.1
 */
public class MockOrderableField implements OrderableField
{
    private String id;
    private String name;
    private boolean shown = true;

    public MockOrderableField(final String id)
    {
        this.id = id;
    }

    public MockOrderableField(final String id, final String name)
    {
        this.id = id;
        this.name = name;
    }

    public MockOrderableField setId(String id)
    {
        this.id = id;
        return this;
    }

    public MockOrderableField setName(String name)
    {
        this.name = name;
        return this;
    }

    public String getCreateHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public String getCreateHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue, final Map displayParameters)
    {
        throw new UnsupportedOperationException();
    }

    public String getEditHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public String getEditHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue, final Map displayParameters)
    {
        throw new UnsupportedOperationException();
    }

    public String getBulkEditHtml(final OperationContext operationContext, final Action action, final BulkEditBean bulkEditBean, final Map displayParameters)
    {
        throw new UnsupportedOperationException();
    }

    public String getViewHtml(final FieldLayoutItem fieldLayoutItem, final Action action, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public String getViewHtml(final FieldLayoutItem fieldLayoutItem, final Action action, final Issue issue, final Map displayParameters)
    {
        throw new UnsupportedOperationException();
    }

    public String getViewHtml(final FieldLayoutItem fieldLayoutItem, final Action action, final Issue issue, final Object value, final Map displayParameters)
    {
        throw new UnsupportedOperationException();
    }

    public boolean isShown(final Issue issue)
    {
        return shown;
    }

    public MockOrderableField setShown(boolean shown)
    {
        this.shown = shown;
        return this;
    }

    public void populateDefaults(final Map fieldValuesHolder, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public void populateFromParams(final Map fieldValuesHolder, final Map parameters)
    {
        throw new UnsupportedOperationException();
    }

    public void populateFromIssue(final Map fieldValuesHolder, final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public void validateParams(final OperationContext operationContext, final ErrorCollection errorCollectionToAddTo, final I18nHelper i18n, final Issue issue, final FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        throw new UnsupportedOperationException();
    }

    public Object getDefaultValue(final Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    public void createValue(final Issue issue, final Object value)
    {
        throw new UnsupportedOperationException();
    }

    public void updateValue(final FieldLayoutItem fieldLayoutItem, final Issue issue, final ModifiedValue modifiedValue, final IssueChangeHolder issueChangeHolder)
    {
        throw new UnsupportedOperationException();
    }

    public void updateIssue(final FieldLayoutItem fieldLayoutItem, final MutableIssue issue, final Map fieldValueHolder)
    {
        throw new UnsupportedOperationException();
    }

    public void removeValueFromIssueObject(final MutableIssue issue)
    {
        throw new UnsupportedOperationException();
    }

    public boolean canRemoveValueFromIssueObject(final Issue issue)
    {
        return false;
    }

    public MessagedResult needsMove(final Collection originalIssues, final Issue targetIssue, final FieldLayoutItem targetFieldLayoutItem)
    {
        throw new UnsupportedOperationException();
    }

    public void populateForMove(final Map fieldValuesHolder, final Issue originalIssue, final Issue targetIssue)
    {
        throw new UnsupportedOperationException();
    }

    public boolean hasValue(final Issue issue)
    {
        return false;
    }

    public String availableForBulkEdit(final BulkEditBean bulkEditBean)
    {
        throw new UnsupportedOperationException();
    }

    public Object getValueFromParams(final Map params) throws FieldValidationException
    {
        throw new UnsupportedOperationException();
    }

    public void populateParamsFromString(final Map fieldValuesHolder, final String stringValue, final Issue issue)
            throws FieldValidationException
    {
        throw new UnsupportedOperationException();
    }

    public SearchHandler createAssociatedSearchHandler()
    {
        throw new UnsupportedOperationException();
    }

    public String getId()
    {
        return id;
    }

    public String getNameKey()
    {
        throw new UnsupportedOperationException();
    }

    public String getName()
    {
        return name;
    }

    public int compareTo(final Object o)
    {
        OrderableField field = (OrderableField) o;
        return id.compareTo(field.getId());
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
