package com.atlassian.jira.plugin.userformat.configuration;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.portal.MockPropertySet;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.jira.plugin.userformat.configuration.PropertySetBackedUserFormatTypeConfiguration.USER_FORMAT_CONFIGURATION_PROPERTY_SET_KEY;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.junit.Assert.assertEquals;

/**
 * @since v3.13
 */
public class TestPropertySetBackedUserFormatTypeConfiguration extends ListeningTestCase
{
    private JiraPropertySetFactory mockJiraPropertySetFactory;
    private MockPropertySet mockPropertySet = new MockPropertySet();

    @Before
    public void setUpMocks() throws Exception
    {
        mockJiraPropertySetFactory = getMockJiraPropertSetFactory();
    }

    @Test
    public void setUserFormatForTypeShouldSetAnEntryToTheUnderlyingPropertySet()
    {
        replay(mockJiraPropertySetFactory);

        final PropertySetBackedUserFormatTypeConfiguration userFormatTypeConfiguration =
                new PropertySetBackedUserFormatTypeConfiguration(mockJiraPropertySetFactory);

        userFormatTypeConfiguration.setUserFormatKeyForType("myType", "some.plugin:profileLink.module");
        assertEquals("some.plugin:profileLink.module", mockPropertySet.getMap().get("myType"));

        verify(mockJiraPropertySetFactory);
    }

    private JiraPropertySetFactory getMockJiraPropertSetFactory()
    {
        final JiraPropertySetFactory mockJiraPropertySetFactory = createMock(JiraPropertySetFactory.class);
        expect(mockJiraPropertySetFactory.buildCachingDefaultPropertySet(USER_FORMAT_CONFIGURATION_PROPERTY_SET_KEY, true)).
                andStubReturn(mockPropertySet);
        return mockJiraPropertySetFactory;
    }
}
