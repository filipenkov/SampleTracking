package com.atlassian.jira.bc;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of the JiraServiceContext.
 *
 * For now, this guy has to be considered as API because devs must feed a JiraServiceContext instance to Service methods,
 * and this is the only way to create one that will be compatible with mulitple versions of JIRA.
 * If we don't like this, we need to provide a Factory or something.
 */
public class JiraServiceContextImpl implements JiraServiceContext
{
    private final ErrorCollection errorCollection;
    private final User user;
    private I18nHelper i18nHelper;
    private com.opensymphony.user.User osUser;

    /**
     * Instantiates this class with given user and new empty error collection.
     * The I18nHelper will be created from the User supplied.
     *
     * @param user user
     */
    public JiraServiceContextImpl(User user)
    {
        this(user, new SimpleErrorCollection());
    }

    /**
     * Instantiates this class with given user and new empty error collection.
     * The I18nHelper will be created from the User supplied.
     *
     * @param user user
     * @deprecated since v4.3. Use {@link #JiraServiceContextImpl(com.atlassian.crowd.embedded.api.User)} instead.
     */
    public JiraServiceContextImpl(com.opensymphony.user.User user)
    {
        this(user, new SimpleErrorCollection());
    }

    /**
     * Instantiates this class with user and error collection.
     * The I18nHelper will be created from the User supplied.
     *
     * @param user            user
     * @param errorCollection error collection; must not be null
     * @throws IllegalArgumentException if error collection is null
     */
    public JiraServiceContextImpl(User user, ErrorCollection errorCollection)
    {
        this(user, errorCollection, null);
    }

    /**
     * Instantiates this class with user and error collection.
     * The I18nHelper will be created from the User supplied.
     *
     * @param user            user
     * @param errorCollection error collection; must not be null
     * @throws IllegalArgumentException if error collection is null
     * 
     * @deprecated since v4.3. Use {@link #JiraServiceContextImpl(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.util.ErrorCollection)} instead.
     */
    public JiraServiceContextImpl(com.opensymphony.user.User user, ErrorCollection errorCollection)
    {
        this(user, errorCollection, null);
    }

    /**
     * Instantiates this class with user and error collection and I18nHelper.
     *
     * @param user            user
     * @param errorCollection error collection; must not be null
     * @param i18nHelper      optional I18nHelper to use
     * @throws IllegalArgumentException if error collection is null
     */
    public JiraServiceContextImpl(User user, ErrorCollection errorCollection, I18nHelper i18nHelper)
    {
        this.errorCollection = notNull("errorCollection", errorCollection);
        this.user = user;
        this.i18nHelper = i18nHelper;
    }

    /**
     * Instantiates this class with user and error collection and I18nHelper.
     *
     * @param user            user
     * @param errorCollection error collection; must not be null
     * @param i18nHelper      optional I18nHelper to use
     * @throws IllegalArgumentException if error collection is null
     * 
     * @deprecated since v4.3. Use {@link #JiraServiceContextImpl(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.util.ErrorCollection, com.atlassian.jira.util.I18nHelper)} instead.
     */
    public JiraServiceContextImpl(com.opensymphony.user.User user, ErrorCollection errorCollection, I18nHelper i18nHelper)
    {
        this.errorCollection = notNull("errorCollection", errorCollection);
        this.user = user;
        this.osUser = user;
        this.i18nHelper = i18nHelper;
    }

    /**
     * Returns error collection, never null
     *
     * @return error collection
     */
    public ErrorCollection getErrorCollection()
    {
        return errorCollection;
    }

    /**
     * Returns user, can be null
     *
     * @return user
     */
    public com.opensymphony.user.User getUser()
    {
        if (osUser ==  null)
        {
            osUser = OSUserConverter.convertToOSUser(user);
        }
        return osUser;
    }

    public User getLoggedInUser()
    {
        return user;
    }

    public I18nHelper getI18nBean()
    {
        if (i18nHelper == null)
        {
            i18nHelper = ComponentAccessor.getI18nHelperFactory().getInstance(user);
        }
        return i18nHelper;
    }


    @SuppressWarnings ({ "RedundantIfStatement" })
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final JiraServiceContextImpl that = (JiraServiceContextImpl) o;

        if (errorCollection != null ? !errorCollection.equals(that.errorCollection) : that.errorCollection != null)
        {
            return false;
        }
        if (user != null ? !user.equals(that.user) : that.user != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (errorCollection != null ? errorCollection.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }

    /**
     * Prints username and error collection
     *
     * @return string representing username and error collection of this context
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("User:");
        if (user == null)
        {
            sb.append("[anonymous]");
        }
        else
        {
            sb.append(user.getName());
        }
        sb.append(" ");
        sb.append(errorCollection.toString());
        return sb.toString();
    }

}
