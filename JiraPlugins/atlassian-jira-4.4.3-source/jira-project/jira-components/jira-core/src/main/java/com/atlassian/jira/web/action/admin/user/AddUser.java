package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for handling the requests to add a new JIRA User.
 */
@WebSudoRequired
public class AddUser extends JiraWebActionSupport
{
    private String username;
    private String password;
    private String confirm;
    private String fullname;
    private String email;
    private Long directoryId;
    private boolean sendEmail = false;
    private final UserService userService;
    private final UserUtil userUtil;
    private final UserManager userManager;
    private final ApplicationProperties applicationProperties;
    private UserService.CreateUserValidationResult result;

    public AddUser(UserService userService, UserUtil userUtil, UserManager userManager, ApplicationProperties applicationProperties)
    {
        this.userService = userService;
        this.userUtil = userUtil;
        this.userManager = userManager;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Processes a request to render the input form to fill out the new user's details(username, password, full-name, email ...)
     * @return {@link #INPUT} the input form to fill out the new user's details(username, password, full-name, email ...)
     */
    @Override
    public String doDefault()
    {
        return INPUT;
    }

    protected void doValidation()
    {
        result = userService.validateCreateUserForAdmin(
                getLoggedInUser(),
                getUsername(),
                getPassword(),
                getConfirm(),
                getEmail(),
                getFullname(),
                getDirectoryId()
        );

        if (!result.isValid())
        {
            addErrorCollection(result.getErrorCollection());
        }
    }

    /**
     * Processes a request to create a user using the specified url parameters.
     * @return if there are input error this will return {@link #ERROR}; otherwise, it will redirect to the View User
     * page for the created user.
     */
    @Override
    @RequiresXsrfCheck
    protected String doExecute()
    {
        try
        {
            // send password if the user has not disabled when creating.
            if (sendEmail)
            {
                userService.createUserWithNotification(result);
            }
            else
            {
                userService.createUserNoNotification(result);
            }
        }
        catch (PermissionException e)
        {
            addError("username", getText("admin.errors.user.no.permission.to.create"));
        }
        catch (CreateException e)
        {
            addError("username", getText("admin.errors.user.cannot.create", e.getMessage()));
        }

        if (getHasErrorMessages())
        {
            return ERROR;
        }
        else
        {
            return getRedirect("ViewUser.jspa?name=" + JiraUrlCodec.encode(username.toLowerCase()));
        }
    }

    public boolean hasReachedUserLimit()
    {
        return !userUtil.canActivateNumberOfUsers(1);
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username.trim();
    }

    public String getFullname()
    {
        return fullname;
    }

    public void setFullname(String fullname)
    {
        this.fullname = fullname;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        if (!TextUtils.stringSet(password))
        {
            this.password = null;
        }
        else
        {
            this.password = password;
        }
    }

    public boolean hasPasswordWritableDirectory()
    {
        return !isExternalUserManagement() && userManager.hasPasswordWritableDirectory();
    }

    public boolean isSendEmail()
    {
        return sendEmail;
    }

    public void setSendEmail(boolean sendEmail)
    {
        this.sendEmail = sendEmail;
    }

    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(String confirm)
    {
        if (!TextUtils.stringSet(confirm))
        {
            this.confirm = null;
        }
        else
        {
            this.confirm = confirm;
        }
    }

    public Long getDirectoryId()
    {
        return directoryId;
    }

    public void setDirectoryId(Long directoryId)
    {
        this.directoryId = directoryId;
    }

    public List<Directory> getDirectories()
    {
        return userManager.getWritableDirectories();
    }

    public Map<Long, Boolean> getCanDirectoryUpdatePasswordMap()
    {
        final List<Directory> directories = getDirectories();
        final Map<Long, Boolean> result = new HashMap<Long, Boolean>(directories.size());
        for (final Directory directory : directories)
        {
            final boolean canUpdatePassword;
            if (isExternalUserManagement())
            {
                canUpdatePassword = false;
            }
            else
            {
                canUpdatePassword = userManager.canDirectoryUpdateUserPassword(directory);
            }
            result.put(directory.getId(), canUpdatePassword);
        }
        return result;
    }

    public UserUtil getUserUtil()
    {
        return userUtil;
    }

    private boolean isExternalUserManagement()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
    }
}