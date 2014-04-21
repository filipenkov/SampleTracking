package com.atlassian.jira.issue.fields;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeFormatterFactoryStub;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.matcher.MapContainsEntryMatcher;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.collect.CompositeMap;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.collect.Maps;
import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ColumnViewDateTimeHelperTest extends MockControllerTestCase
{

    @Mock
    private NavigableFieldImpl field;

    @Mock
    private FieldLayoutItem fieldLayoutItem;

    @Mock
    private Issue issue;

    @Mock
    JiraAuthenticationContext authenticationContext;

    Date date;

    DateTimeFormatterFactory dateTimeFormatterFactory;

    Map<String, Object> displayParams;

    I18nHelper i18nHelper;

    Map<String, Object> legacyParams;

    Map<String, Object> expectedParams;

    @Test
    public void renderUsesDateColumnViewVelocityTemplateByDefault() throws Exception
    {
        expect(field.renderTemplate(eq("date-columnview.vm"), this.<Map>anyObject())).andStubReturn("default formatted date");

        String rendered = instantiate(ColumnViewDateTimeHelper.class).render(field, fieldLayoutItem, displayParams, issue, date);
        assertThat(rendered, equalTo("default formatted date"));
    }

    @Test
    public void renderUsesDateExcelViewVelocityTemplateForExcelView() throws Exception
    {
        expect(field.renderTemplate(eq("date-excelview.vm"), this.<Map>anyObject())).andStubReturn("excel formatted date");
        Map<String, Object> excelDisplayParams = CompositeMap.of(Collections.singletonMap("excel_view", (Object) Boolean.TRUE), displayParams);

        String rendered = instantiate(ColumnViewDateTimeHelper.class).render(field, fieldLayoutItem, excelDisplayParams, issue, date);
        assertThat(rendered, equalTo("excel formatted date"));
    }

    @Test
    public void velocityParamsShouldContainLegacyParams() throws Exception
    {
        expect(field.renderTemplate(anyString(), MapContainsEntryMatcher.containsEntry("field-specific-key", "field-specific-value"))).andReturn("excel formatted date");

        instantiate(ColumnViewDateTimeHelper.class).render(field, fieldLayoutItem, displayParams, issue, date);
        verify(field); // verify that the passed-in velocity params contain the field-specific-key
    }

    @Test
    public void velocityParamsShouldContainTitle() throws Exception
    {
        expect(field.renderTemplate(anyString(), MapContainsEntryMatcher.containsEntry("title", "07/Aug/66 11:58 PM"))).andReturn("ignored");

        instantiate(ColumnViewDateTimeHelper.class).render(field, fieldLayoutItem, displayParams, issue, date);
        verify(field);
    }

    @Test
    public void velocityParamsShouldContainIso8601() throws Exception
    {
        expect(field.renderTemplate(anyString(), MapContainsEntryMatcher.containsEntry("iso8601", "24666-08-07T23:58+1000"))).andReturn("ignored");

        instantiate(ColumnViewDateTimeHelper.class).render(field, fieldLayoutItem, displayParams, issue, date);
        verify(field);
    }

    @Test
    public void velocityParamsShouldContainValue() throws Exception
    {
        expect(field.renderTemplate(anyString(), MapContainsEntryMatcher.containsEntry("value", "07/Aug/66"))).andReturn("ignored");

        instantiate(ColumnViewDateTimeHelper.class).render(field, fieldLayoutItem, displayParams, issue, date);
        verify(field);
    }

    @Before
    public void setUp() throws Exception
    {
        date = new Date(716235487126345L);

        dateTimeFormatterFactory = new DateTimeFormatterFactoryStub();
        addObjectInstance(dateTimeFormatterFactory);
        i18nHelper = new NoopI18nHelper();
        displayParams = MapBuilder.build("key1", new Object());
        legacyParams = MapBuilder.build("key2", new Object());
        expectedParams = CompositeMap.of(displayParams, legacyParams);

        expect(authenticationContext.getI18nHelper()).andStubReturn(i18nHelper);
        expect(field.getVelocityParams(eq(fieldLayoutItem), eq(i18nHelper), this.<Map>anyObject(), eq(issue))).andStubAnswer(new GetVelocityParamsAnswer());
    }

    /**
     * Adds the entry ("field-specific-key", "field-specific-value") to the given map.
     */
    static class GetVelocityParamsAnswer implements IAnswer<Map<String, Object>>
    {
        public Map<String, Object> answer() throws Throwable
        {
            // add a field-specific key and value
            Map<String, Object> displayParams = Maps.newHashMap((Map<String, Object>) EasyMock.getCurrentArguments()[2]);
            displayParams.put("field-specific-key", "field-specific-value");

            return displayParams;
        }
    }
}
