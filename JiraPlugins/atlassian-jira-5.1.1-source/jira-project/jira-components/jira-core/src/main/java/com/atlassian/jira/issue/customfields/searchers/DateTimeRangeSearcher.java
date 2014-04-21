package com.atlassian.jira.issue.customfields.searchers;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.customfields.converters.DateTimePickerConverter;
import com.atlassian.jira.issue.customfields.searchers.renderer.DateCustomFieldSearchRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.constants.SimpleFieldSearchConstants;
import com.atlassian.jira.issue.search.searchers.transformer.DateSearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.util.DateSearcherConfig;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.action.util.CalendarLanguageUtil;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DateTimeRangeSearcher extends AbstractDateRangeSearcher
{
    private final JiraAuthenticationContext context;
    private final CustomFieldInputHelper customFieldInputHelper;
    private final DateTimeFormatterFactory dateTimeFormatterFactory;

    public DateTimeRangeSearcher(final JiraAuthenticationContext context, final JqlOperandResolver jqlOperandResolver,
            final VelocityRequestContextFactory velocityRenderContext,
            final ApplicationProperties applicationProperties, final VelocityTemplatingEngine templatingEngine, final CalendarLanguageUtil calendarUtils,
            final JqlDateSupport dateSupport, final CustomFieldInputHelper customFieldInputHelper, TimeZoneManager timeZoneManager,
            DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        super(jqlOperandResolver, velocityRenderContext, applicationProperties, templatingEngine, calendarUtils, new DateTimePickerConverter(dateTimeFormatterFactory), dateSupport, timeZoneManager);
        this.context = context;
        this.customFieldInputHelper = notNull("customFieldInputHelper", customFieldInputHelper);
        this.dateTimeFormatterFactory = notNull("dateTimeFormatterFactory", dateTimeFormatterFactory);
    }

    /**
     * @deprecated use the second constructor which takes a TimeZoneManager.
     */
    public DateTimeRangeSearcher(final JiraAuthenticationContext context, final JqlOperandResolver jqlOperandResolver,
            final VelocityRequestContextFactory velocityRenderContext,
            final ApplicationProperties applicationProperties, final VelocityTemplatingEngine templatingEngine, final CalendarLanguageUtil calendarUtils,
            final JqlDateSupport dateSupport, final CustomFieldInputHelper customFieldInputHelper, DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        this(context, jqlOperandResolver, velocityRenderContext, applicationProperties, templatingEngine, calendarUtils, dateSupport, customFieldInputHelper, ComponentManager.getComponentInstanceOfType(TimeZoneManager.class), dateTimeFormatterFactory);
    }

    @Override
    DateSearchInputTransformer createSearchInputTransformer(final DateSearcherConfig config, TimeZoneManager timeZoneManager)
    {
        final DateTimePickerConverter dateTimePickerConverter = new DateTimePickerConverter(dateTimeFormatterFactory);
        return new DateSearchInputTransformer(true, config, dateTimePickerConverter, dateTimePickerConverter, jqlOperandResolver, dateSupport, customFieldInputHelper, timeZoneManager, dateTimeFormatterFactory);
    }

    @Override
    DateCustomFieldSearchRenderer createSearchRenderer(final CustomField field, final SimpleFieldSearchConstants constants, final String nameKey, final DateSearcherConfig config)
    {
        return new DateCustomFieldSearchRenderer(true, field, constants, config, velocityRenderContext,
                applicationProperties, templatingEngine, calendarUtils, fieldVisibilityManager);
    }
}
