package com.atlassian.jira.webtest.webdriver.tests.admin.applicationproperties;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.admin.applicationproperties.AdvancedApplicationProperty;
import com.atlassian.jira.pageobjects.pages.admin.applicationproperties.AdvancedPropertiesPage;
import com.atlassian.jira.pageobjects.pages.admin.applicationproperties.EditAdvancedApplicationPropertyForm;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.DelayedBinder;
import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Test for the "Advanced Configuration" page
 *
 * @since v4.4
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS })
@Restore("xml/TestAdvancedApplicationProperties.xml")
public class TestAdvancedApplicationProperties extends BaseJiraWebTest
{
    @Test
    public void testOnlyAdminUsersCanAccessPropertiesPage()
    {
        // Login as sys admin, can be bound
        // Logout, and try and hit the page again
        AdvancedPropertiesPage propertiesPage = jira.gotoLoginPage().loginAsSysAdmin(AdvancedPropertiesPage.class);

        jira.logout();

        DelayedBinder<AdvancedPropertiesPage> advancedPropertiesPageDelayedBinder = jira.visitDelayed(AdvancedPropertiesPage.class);

        assertFalse(advancedPropertiesPageDelayedBinder.canBind());

        // Login as non-sysadmin,

        jira.gotoLoginPage().login("normal_admin", "normal_admin", DashboardPage.class);
        advancedPropertiesPageDelayedBinder = jira.visitDelayed(AdvancedPropertiesPage.class);

        assertFalse(advancedPropertiesPageDelayedBinder.canBind());

    }

    @Test
    public void testCanViewApplicationProperties()
    {
        // can view expected list of application properties
        final AdvancedPropertiesPage propertiesPage = jira.gotoLoginPage().loginAsSysAdmin(AdvancedPropertiesPage.class);
        final List<AdvancedApplicationProperty> applicationProperties = propertiesPage.getApplicationProperties();
        final List<AdvancedApplicationProperty> expectedApplicationProperties = getExpectedApplicationProperties();

        assertEquals(expectedApplicationProperties.size(), applicationProperties.size());
        for(int i = 0; i < applicationProperties.size(); ++i)
        {
            assertEquals(expectedApplicationProperties.get(i), applicationProperties.get(i));
        }
//        assertEquals(expectedApplicationProperties, applicationProperties);
    }

    private List<AdvancedApplicationProperty> getExpectedApplicationProperties()
    {
        return Lists.<AdvancedApplicationProperty>newArrayList(
                new MockAdvancedApplicationProperty("jira.attachment.number.of.zip.entries", "30", "The contents of zip attachments can be displayed, but often there are too many to display."),
                new MockAdvancedApplicationProperty("jira.clone.prefix", "CLONE -", null),
                new MockAdvancedApplicationProperty("jira.date.picker.java.format", "d/MMM/yy", "This part is only for the Java (server side) generated dates. Note that this should correspond to the javascript date picker format (jira.date.picker.javascript.format) setting."),
                new MockAdvancedApplicationProperty("jira.date.picker.javascript.format", "%e/%b/%y", "This part is only for the JavaScript (client side) generated dates. Note that this should correspond to the java date picker format (jira.date.picker.java.format) setting."),
                new MockAdvancedApplicationProperty("jira.date.time.picker.java.format", "dd/MMM/yy h:mm a", "This part is only for the Java (server side) generated datetimes. Note that this should correspond to the javascript datetime picker format (jira.date.time.picker.javascript.format) setting."),
                new MockAdvancedApplicationProperty("jira.date.time.picker.javascript.format", "%e/%b/%y %I:%M %p", "This part is only for the JavaScript (client side) generated date times. Note that this should correspond to the java datetime picker format (jira.date.time.picker.java.format) setting."),
                new MockAdvancedApplicationProperty("jira.issue.actions.order", "asc", "The default order of actions (tab items like 'Comments', 'Change History' etc) on the 'View Issue' screen, by date, from top to bottom."),
                new MockAdvancedApplicationProperty("jira.projectkey.pattern", "([A-Z][A-Z]+)", "A regular expression that defines a valid project key."),
                new MockAdvancedApplicationProperty("jira.table.cols.subtasks", "issuetype, status, assignee, progress", "The columns to show when viewing sub-task issues in a table"),
                new MockAdvancedApplicationProperty("jira.view.issue.links.sort.order", "type, status, priority", "Specifies the sort order of the issue links on the 'View Issue' screen.")

        );
    }

    @Test
    public void testCanEditApplicationProperty()
    {
        // Can click to edit
        final AdvancedPropertiesPage propertiesPage = jira.gotoLoginPage().loginAsSysAdmin(AdvancedPropertiesPage.class);
        AdvancedApplicationProperty applicationProperty = propertiesPage.getProperty("jira.projectkey.pattern");

        EditAdvancedApplicationPropertyForm editApplicationPropertyForm = applicationProperty.edit();

        // Can cancel and no change made
        editApplicationPropertyForm.setText("[A-Z]+[0-9]*").cancel();

        applicationProperty = propertiesPage.getProperty("jira.projectkey.pattern");
        assertEquals("([A-Z][A-Z]+)", applicationProperty.getValue());

        // Can change, save, and take effect
        editApplicationPropertyForm = applicationProperty.edit();
        editApplicationPropertyForm.setText("[A-Z]+[0-9]*").submit();

        applicationProperty = propertiesPage.getProperty("jira.projectkey.pattern");
        assertEquals("[A-Z]+[0-9]*", applicationProperty.getValue());

        // Can revert
        applicationProperty.revert();

        applicationProperty = propertiesPage.getProperty("jira.projectkey.pattern");
        assertEquals("([A-Z][A-Z]+)", applicationProperty.getValue());


    }

    @Test
    public void testValidationWorks()
    {
        // Click to edit
        final AdvancedPropertiesPage propertiesPage = jira.gotoLoginPage().loginAsSysAdmin(AdvancedPropertiesPage.class);
        AdvancedApplicationProperty applicationProperty = propertiesPage.getProperty("jira.projectkey.pattern");

        EditAdvancedApplicationPropertyForm editApplicationPropertyForm = applicationProperty.edit();

        // Enter invalid value
        editApplicationPropertyForm.setText("ABC").submit();

        // Expect error
        assertEquals("An existing project contains a key that cannot be matched with the pattern.", editApplicationPropertyForm.getError());

        editApplicationPropertyForm.cancel();

        // Enter invalid value with a banned, to be escaped char
        editApplicationPropertyForm = applicationProperty.edit();
        editApplicationPropertyForm.setText("[A-Z][A-Z]+>*").submit();

        // Expect error, but should be escaped only once.
        assertEquals("Matches a banned character: >", editApplicationPropertyForm.getError());

        editApplicationPropertyForm.cancel();

    }

    public static class MockAdvancedApplicationProperty implements AdvancedApplicationProperty
    {

        private String key;
        private String value;
        private String description;

        public MockAdvancedApplicationProperty(final String key, final String value, final String description)
        {
            this.key = key;
            this.value = value;
            this.description = description;
        }

        @Override
        public EditAdvancedApplicationPropertyForm edit()
        {
            throw new UnsupportedOperationException("Can't be edited");
        }

        @Override
        public AdvancedApplicationProperty revert()
        {
            throw new UnsupportedOperationException("Can't be reverted");
        }

        @Override
        public String getKey()
        {
            return key;
        }

        @Override
        public String getDescription()
        {
            return description;
        }

        @Override
        public String getValue()
        {
            return value;
        }

        @Override
        public String toString()
        {
            return new ToStringBuilder(this).
                    append("key", key).
                    append("value", value).
                    append("description", description).
                    toString();
        }

        @Override
        public boolean equals(Object o)
        {
            if(o == null || !(o instanceof AdvancedApplicationProperty))
            {
                return false;
            }

            final AdvancedApplicationProperty rhs = (AdvancedApplicationProperty)o;

            return new EqualsBuilder().append(key, rhs.getKey())
                    .append(value, rhs.getValue())
                    .append(description, rhs.getDescription())
                    .isEquals();

        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder().append(key)
                    .append(value)
                    .append(description)
                    .toHashCode();
        }
    }

}
