package com.atlassian.jira.bc;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.bean.MockI18nBean;
import mock.user.MockOSUser;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.junit.Assert;

import java.util.Locale;

/**
 * Mock JiraServiceContext
 *
 * @since v3.13
 */
public class MockJiraServiceContext implements JiraServiceContext
{
    private ErrorCollection errorCollection = new SimpleErrorCollection();
    private User user;
    private I18nHelper i18nBean;

    public MockJiraServiceContext()
    {
        this("TestUser");
    }

    public MockJiraServiceContext(User user)
    {
        this(user, Locale.getDefault());
    }

    public MockJiraServiceContext(final User user, final ErrorCollection errors)
    {
        this.user = user;
        this.errorCollection = errors;
        this.i18nBean = new MockI18nBean();
    }

    public MockJiraServiceContext(String username)
    {
        this(username, Locale.getDefault());
    }

    public MockJiraServiceContext(String username, Locale locale)
    {
        this(new MockOSUser(username, null, null), locale);
        i18nBean = new MockI18nBean();
    }

    public MockJiraServiceContext(String username, String fullName)
    {
        this(new MockOSUser(username, fullName, null), Locale.getDefault());
        i18nBean = new MockI18nBean();
    }

    public MockJiraServiceContext(User user, Locale locale)
    {
        this.i18nBean = new MockI18nBean();
        this.user = user;
    }

    public ErrorCollection getErrorCollection()
    {
        return errorCollection;
    }

    public com.opensymphony.user.User getUser()
    {
        return OSUserConverter.convertToOSUser(user);
    }

    public User getLoggedInUser()
    {
        return user;
    }

    public I18nHelper getI18nBean()
    {
        return i18nBean;
    }

    /**
     * This mehtod is used to assert that there are no errors of any kind in the underlying ErrorCollection.
     */
    public void assertNoErrors()
    {
        if (getErrorCollection().hasAnyErrors())
            Assert.fail("Errors were found in the ErrorCollection.");
    }

    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
    }
}
