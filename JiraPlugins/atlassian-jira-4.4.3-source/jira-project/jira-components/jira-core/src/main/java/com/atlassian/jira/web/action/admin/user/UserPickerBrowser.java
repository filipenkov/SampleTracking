package com.atlassian.jira.web.action.admin.user;

import com.atlassian.core.user.GroupUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.AbstractBrowser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.jira.web.bean.UserPickerFilter;
import webwork.action.ActionContext;
import webwork.util.BeanUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserPickerBrowser extends AbstractBrowser
{
    private List users;
    private String formName;
    private String element;
    public static final int ALL_USERS = 1;
    public static final int ASSIGNABLE = 2;

    // Multi-select
    private boolean multiSelect = false;
    private final EmailFormatter emailFormatter;
    private List<String>  previouslySelectedUsers = new ArrayList<String>();

    public UserPickerBrowser(EmailFormatter emailFormatter)
    {
        this.emailFormatter = emailFormatter;
    }

    protected String doExecute() throws Exception
    {
        resetPager();

        BeanUtil.setProperties(params, getFilter());

        // If the user changes either the email filter, name filter or the group,
        // reset the cursor to the start of the filter result set.
        if (params.containsKey("emailFilter") || params.containsKey("group") || params.containsKey("nameFilter"))
        {
            if (params.containsKey("start"))
            {
                setStart(params.get("start").toString());
            }
            else
            {
                setStart("0");
            }
        }

        // JRA-12989 - Reset the start to 0 if number of items returned is less than the pager start
        if (getBrowsableItems().size() <= getPager().getStart())
        {
            setStart("0");
        }

        return super.doExecute();
    }

    public PagerFilter getPager()
    {
        return getFilter();
    }

    public void resetPager()
    {
        ActionContext.getSession().put(SessionKeys.USER_PICKER_FILTER, null);
    }

    public UserPickerFilter getFilter()
    {
        UserPickerFilter filter = (UserPickerFilter) ActionContext.getSession().get(SessionKeys.USER_PICKER_FILTER);

        if (filter == null)
        {
            filter = new UserPickerFilter(getLocale());
            ActionContext.getSession().put(SessionKeys.USER_PICKER_FILTER, filter);
        }

        return filter;
    }

    /**
     * Return the current 'page' of issues (given max and start) for the current filter
     */
    public List getCurrentPage()
    {
        return getFilter().getCurrentPage(getBrowsableItems());
    }

    public List/*<User>*/ getBrowsableItems()
    {
        if (users == null)
        {
            try
            {
                users = getFilter().getFilteredUsers();
            }
            catch (Exception e)
            {
                log.error("Exception getting users: " + e, e);
                throw new RuntimeException(e);
            }
        }

        return users;
    }

    public Collection getGroups()
    {
        return GroupUtils.getGroups();
    }

    /**
     * Convenience method to use from JSP's to access total number of users
     *
     * @return a collection of browsable users
     */
    public Collection/*<User>*/ getUsers()
    {
        return getBrowsableItems();
    }

    /**
     * Get the name of the calling form
     *
     * @return form name
     */
    public String getFormName()
    {
        return formName;
    }

    /**
     * Set the name of the calling form
     *
     * @param formName form name
     */
    public void setFormName(String formName)
    {
        this.formName = formName;
    }

    /**
     * Get the name of the element for the value to be returned to
     *
     * @return the name of the element for the value to be returned to
     */
    public String getElement()
    {
        return element;
    }

    /**
     * Set the name of the element for the value to be returned to
     *
     * @param element the name of the element for the value to be returned to
     */
    public void setElement(String element)
    {
        this.element = element;
    }

    public boolean getPermission()
    {
        return isHasPermission(Permissions.USER_PICKER);
    }

    // -----------------    Multi-select    ----------------------
    public boolean isMultiSelect()
    {
        return multiSelect;
    }

    public void setMultiSelect(boolean isMultiSelect)
    {
        this.multiSelect = isMultiSelect;
    }

    public boolean getEmailColumnVisible()
    {
        return emailFormatter.emailVisible(getRemoteUser());
    }

    public String getDisplayEmail(String email)
    {
        return emailFormatter.formatEmail(email, getRemoteUser());
    }

    public String getPreviouslySelected()
    {
        StringBuilder stringBuilder = new StringBuilder();
        if (!previouslySelectedUsers.isEmpty())
        {
            stringBuilder.append(";");
        }
        for (String userName : previouslySelectedUsers)
        {
            stringBuilder.append(encode(userName));
            stringBuilder.append(';');
        }
        return stringBuilder.toString();
    }

    public void setPreviouslySelected(String previouslySelected)
    {
        if (previouslySelected.length() != 0)
        {
            previouslySelected = previouslySelected.substring(1, previouslySelected.length()-1);
            final String[] users = previouslySelected.split(";");
            for (String user : users)
            {
                previouslySelectedUsers.add(decode(user));
            }
        }
    }

    public boolean wasPreviouslySelected(User user)
    {
        return previouslySelectedUsers.contains(user.getName());
    }

    private String decode(final String user)
    {
        return user.replace("%59", ";");
    }

    private String encode(final String user)
    {
        return user.replace(";", "%59");
    }
}
