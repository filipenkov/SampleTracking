package com.atlassian.jira.upgrade.tasks.jql;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.transformer.FieldFlagOperandRegistry;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.operator.Operator;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.0
 */
public class DefaultClauseXmlHandlerRegistry implements ClauseXmlHandlerRegistry
{
    private static final Logger log = Logger.getLogger(DefaultClauseXmlHandlerRegistry.class);

    private final Map<String, ClauseXmlHandler> handlers;
    private final CustomFieldManager customFieldManager;

    public DefaultClauseXmlHandlerRegistry(CustomFieldManager customFieldManager, final JqlSelectOptionsUtil jqlSelectOptionsUtil, final FieldFlagOperandRegistry fieldFlagOperandRegistry, TimeZoneManager timeZoneManager)
    {
        this(customFieldManager, MapBuilder.<String, ClauseXmlHandler>newBuilder()
                .add("com.atlassian.jira.issue.search.parameters.lucene.PriorityParameter", new ConstantsClauseXmlHandler(fieldFlagOperandRegistry))
                .add("com.atlassian.jira.issue.search.parameters.lucene.ResolutionParameter", new ConstantsClauseXmlHandler(fieldFlagOperandRegistry))
                .add("com.atlassian.jira.issue.search.parameters.lucene.StatusParameter", new ConstantsClauseXmlHandler(fieldFlagOperandRegistry))
                .add("com.atlassian.jira.issue.search.parameters.lucene.IssueTypeParameter", new ConstantsClauseXmlHandler(fieldFlagOperandRegistry))
                .add("com.atlassian.jira.issue.search.parameters.lucene.IssueConstantsParameter", new ConstantsClauseXmlHandler(fieldFlagOperandRegistry))
                .add("com.atlassian.jira.issue.search.parameters.lucene.ProjectParameter", new ProjectClauseXmlHandler(fieldFlagOperandRegistry))
                .add("com.atlassian.jira.issue.search.parameters.lucene.VersionParameter", new AffectedVersionClauseXmlHandler(fieldFlagOperandRegistry))
                .add("com.atlassian.jira.issue.search.parameters.lucene.FixForParameter", new FixForVersionClauseXmlHandler(fieldFlagOperandRegistry))
                .add("com.atlassian.jira.issue.search.parameters.lucene.ComponentParameter", new ComponentClauseXmlHandler(fieldFlagOperandRegistry))
                .add("com.atlassian.jira.issue.search.parameters.lucene.MultipleFieldSingleValueLuceneParameter", new TextSystemFieldClauseXmlHandler(fieldFlagOperandRegistry))
                .add("com.atlassian.jira.issue.search.parameters.lucene.AbsoluteDateRangeParameter:created", new AbsoluteDateXmlHandler(Collections.singletonList(IssueFieldConstants.CREATED), timeZoneManager))
                .add("com.atlassian.jira.issue.search.parameters.lucene.AbsoluteDateRangeParameter:updated", new AbsoluteDateXmlHandler(Collections.singletonList(IssueFieldConstants.UPDATED), timeZoneManager))
                .add("com.atlassian.jira.issue.search.parameters.lucene.AbsoluteDateRangeParameter:duedate", new AbsoluteDateXmlHandler(Collections.singletonList(IssueFieldConstants.DUE_DATE), timeZoneManager))
                .add("com.atlassian.jira.issue.search.parameters.lucene.AbsoluteDateRangeParameter:resolutiondate", new AbsoluteDateXmlHandler(Collections.singletonList(IssueFieldConstants.RESOLUTION_DATE), timeZoneManager))
                .add("com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter:created", new RelativeDateXmlHandler(Collections.singletonList(IssueFieldConstants.CREATED)))
                .add("com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter:updated", new RelativeDateXmlHandler(Collections.singletonList(IssueFieldConstants.UPDATED)))
                .add("com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter:duedate", new RelativeDateXmlHandler(Collections.singletonList(IssueFieldConstants.DUE_DATE)))
                .add("com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter:resolutiondate", new RelativeDateXmlHandler(Collections.singletonList(IssueFieldConstants.RESOLUTION_DATE)))
                .add("com.atlassian.jira.issue.search.parameters.lucene.UserParameter:issue_author", new UserClauseXmlHandler(SystemSearchConstants.forReporter()))
                .add("com.atlassian.jira.issue.search.parameters.lucene.UsersGroupParameter:issue_author_group", new UserClauseXmlHandler(SystemSearchConstants.forReporter()))
                .add("com.atlassian.jira.issue.search.parameters.lucene.UserParameter:issue_assignee", new UserClauseXmlHandler(SystemSearchConstants.forAssignee()))
                .add("com.atlassian.jira.issue.search.parameters.lucene.UsersGroupParameter:issue_assignee_group", new UserClauseXmlHandler(SystemSearchConstants.forAssignee()))
                .add("com.atlassian.jira.issue.search.parameters.lucene.WorkRatioParameter", new WorkRatioClauseXmlHandler())

                // Custom fields parameters
                .add("com.atlassian.jira.issue.search.parameters.lucene.GenericMultiValueParameter", new MultiValueParameterClauseXmlHandler(fieldFlagOperandRegistry, customFieldManager))
                .add("com.atlassian.jira.issue.search.parameters.lucene.AbsoluteDateRangeParameter", new AbsoluteDateXmlHandler(Collections.<String>emptyList(), timeZoneManager))
                .add("com.atlassian.jira.issue.search.parameters.lucene.RelativeDateRangeParameter", new RelativeDateXmlHandler(Collections.<String>emptyList()))
                .add("com.atlassian.jira.issue.search.parameters.lucene.StringParameter", new SimpleCustomFieldClauseXmlHandler("value", Operator.EQUALS))
                .add("com.atlassian.jira.issue.search.parameters.lucene.FreeTextParameter", new SimpleCustomFieldClauseXmlHandler("value", Operator.LIKE))
                .add("com.atlassian.jira.issue.search.parameters.lucene.UserParameter", new UserParameterCustomFieldClauseXmlHandler())
                .add("com.atlassian.jira.issue.search.parameters.lucene.UsersGroupParameter", new UserGroupParameterCustomFieldClauseXmlHandler())
                .add("com.atlassian.jira.issue.search.parameters.lucene.StringRangeParameter", new StringRangeParameterClauseXmlHandler())
                .add("com.atlassian.jira.issue.search.parameters.lucene.StringParameter:com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect", new CascadeSelectParameterClauseXmlHandler(jqlSelectOptionsUtil))
                .toMap()
        );
    }

    DefaultClauseXmlHandlerRegistry(final CustomFieldManager customFieldManager, final Map<String, ClauseXmlHandler> handlers)
    {
        this.handlers = handlers;
        this.customFieldManager = notNull("customFieldManager", customFieldManager);
    }

    public ClauseXmlHandler getClauseXmlHandler(String searchParameterClassName, String elementName)
    {
        notBlank("elementName", elementName);
        if (elementName.startsWith(FieldManager.CUSTOM_FIELD_PREFIX))
        {
            Long id = getId(elementName);
            final CustomField customField = (id != null) ? customFieldManager.getCustomFieldObject(id) : null;

            if (customField == null)
            {
                log.warn("Unable to find custom field in system with id '" + id + "', converting anyway.");
            }

            final String customFieldKey = (customField != null) ? customField.getCustomFieldType().getKey() : null;
            ClauseXmlHandler clauseXmlHandler = null;
            if (customFieldKey != null)
            {
                clauseXmlHandler = handlers.get(searchParameterClassName+":"+customFieldKey);
            }

            if (clauseXmlHandler == null)
            {
                clauseXmlHandler = handlers.get(searchParameterClassName);
            }
            return clauseXmlHandler;
        }
        else
        {
            ClauseXmlHandler handler = this.handlers.get(searchParameterClassName + ":" + elementName);
            if (handler == null)
            {
                handler = this.handlers.get(searchParameterClassName);
            }
            return handler;
        }
    }

    private Long getId(String elementName)
    {
        try
        {
            // There is a class of value that has "_group" appended to the id, we should handle this
            if (elementName.endsWith(UserClauseXmlHandler.GROUP_SUFFIX))
            {
                elementName = elementName.substring(0, elementName.indexOf(UserClauseXmlHandler.GROUP_SUFFIX));
            }

            return Long.parseLong(elementName.substring(FieldManager.CUSTOM_FIELD_PREFIX.length()));
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
