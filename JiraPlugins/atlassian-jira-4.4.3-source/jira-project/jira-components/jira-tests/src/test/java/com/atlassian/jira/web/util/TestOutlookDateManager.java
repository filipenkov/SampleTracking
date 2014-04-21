package com.atlassian.jira.web.util;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Locale;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

public class TestOutlookDateManager extends MockControllerTestCase
{
    @SuppressWarnings ({ "UnusedDeclaration", "FieldCanBeLocal" })
    private EventPublisher mockEventPublisher;
    private ApplicationProperties mockApplicationProperties;
    private DateTimeFormatterFactory dateTimeFormatterFactory;

    private OutlookDateManager outlookDateManager;
    private DateTimeFormatter defaultFormatter;
    private DateTimeFormatter userFormatter;

    public TestOutlookDateManager()
    {
        super();
    }

    @Before
    public void setUp() throws Exception
    {
        mockEventPublisher = getMock(EventPublisher.class);
        mockApplicationProperties = getMock(ApplicationProperties.class);
        dateTimeFormatterFactory = getMock(DateTimeFormatterFactory.class);

        defaultFormatter = createMock(DateTimeFormatter.class);
        userFormatter = createMock(DateTimeFormatter.class);
        expect(dateTimeFormatterFactory.formatter()).andStubReturn(defaultFormatter);
        expect(defaultFormatter.forLoggedInUser()).andStubReturn(userFormatter);
    }

    @Test
    public void testRefresh()
    {
        outlookDateManager = instantiate(OutlookDateManagerImpl.class);

        OutlookDate englishDate = outlookDateManager.getOutlookDate(Locale.ENGLISH);
        outlookDateManager.refresh();

        OutlookDate newEnglishDate = outlookDateManager.getOutlookDate(Locale.ENGLISH);
        assertNotSame(englishDate, newEnglishDate);
    }

    @Test
    public void testGetOutlookDate()
    {
        Calendar calendarInstance = Calendar.getInstance();
        calendarInstance.set(2001, 2, 1);

        DateTimeFormatter datePickerFormatter = createMock(DateTimeFormatter.class);
        expect(userFormatter.withStyle(DateTimeStyle.DATE_PICKER)).andStubReturn(datePickerFormatter);
        expect(datePickerFormatter.format(calendarInstance.getTime())).andReturn("1/Mar/01");

        outlookDateManager = instantiate(OutlookDateManagerImpl.class);
        
        OutlookDate englishDate = outlookDateManager.getOutlookDate(Locale.ENGLISH);

        final String englishFormattedDate = englishDate.formatDatePicker(calendarInstance.getTime());
        assertEquals("1/Mar/01", englishFormattedDate);
    }
}
