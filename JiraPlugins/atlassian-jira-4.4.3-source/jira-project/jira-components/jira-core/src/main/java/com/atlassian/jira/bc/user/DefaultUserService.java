package com.atlassian.jira.bc.user;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.user.UserEventType;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.bean.I18nBean;
import com.opensymphony.user.ImmutableException;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Default implementation of {@link com.atlassian.jira.bc.user.UserService} interface. Contains metohods to create/delete users hiding UserUtil internals.
 *
 * @since v4.0
 */
public class DefaultUserService implements UserService
{
    private static final int MAX_FIELD_LENGTH = 255;

    private final UserUtil userUtil;
    private final UserManager userManager;
    private final PermissionManager permissionManager;
    private final ApplicationProperties applicationProperties;
    private final JiraContactHelper jiraContactHelper;

    public DefaultUserService(
            UserUtil userUtil,
            PermissionManager permissionManager,
            ApplicationProperties applicationProperties,
            UserManager userManager, JiraContactHelper jiraContactHelper)
    {
        this.userUtil = userUtil;
        this.permissionManager = permissionManager;
        this.applicationProperties = applicationProperties;
        this.userManager = userManager;
        this.jiraContactHelper = jiraContactHelper;
    }

    public CreateUserValidationResult validateCreateUserForSignupOrSetup(final User user, final String username, final String password,
            final String confirmPassword, final String email, final String fullname)
    {
        final I18nHelper i18nBean = getI18nBean(user);
        final ErrorCollection errors = new SimpleErrorCollection();

        if (applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT))
        {
            String link = getContactAdminLink(i18nBean);
            errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.add.user.external.managment", link));
            return new CreateUserValidationResult(errors);
        }

        errors.addErrorCollection(validateCreateUser(i18nBean, username, password, confirmPassword, email, fullname, null));
        //for setup or public signups the password is a required field.
        if (StringUtils.isEmpty(password))
        {
            errors.addError(FieldName.PASSWORD, i18nBean.getText("signup.error.password.required"));
        }

        if (errors.hasAnyErrors())
        {
            return new CreateUserValidationResult(errors);
        }

        return new CreateUserValidationResult(username, password, email, fullname);
    }

    @Override
    public CreateUserValidationResult validateCreateUserForSignupOrSetup(com.opensymphony.user.User user, String username, String password, String confirmPassword, String email, String fullname)
    {
        return validateCreateUserForSignupOrSetup((User) user, username, password, confirmPassword, email, fullname);
    }

    public CreateUserValidationResult validateCreateUserForAdminPasswordRequired(final User user, final String username,
            final String password,
            final String confirmPassword, final String email, final String fullname)
    {
        final I18nHelper i18nBean = getI18nBean(user);
        final ErrorCollection errors = new SimpleErrorCollection();

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.user.no.permission.to.create"));
            return new CreateUserValidationResult(errors);
        }

        if (applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT))
        {
            String link = getContactAdminLink(i18nBean);
            errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.add.user.external.managment", link));
            return new CreateUserValidationResult(errors);
        }

        errors.addErrorCollection(validateCreateUser(i18nBean, username, password, confirmPassword, email, fullname, null));
        //for setup or public signups the password is a required field.
        if (StringUtils.isEmpty(password))
        {
            errors.addError(FieldName.PASSWORD, i18nBean.getText("signup.error.password.required"));
        }

        if (errors.hasAnyErrors())
        {
            return new CreateUserValidationResult(errors);
        }

        return new CreateUserValidationResult(username, password, email, fullname, null);
    }

    @Override
    public CreateUserValidationResult validateCreateUserForAdminPasswordRequired(com.opensymphony.user.User user, String username, String password, String confirmPassword, String email, String fullname)
    {
        return validateCreateUserForAdminPasswordRequired((User) user, username, password, confirmPassword, email, fullname);
    }

    @Override
    public CreateUserValidationResult validateCreateUserForAdmin(final User user, final String username, final String password,
            final String confirmPassword, final String email, final String fullname)
    {
        return validateCreateUserForAdmin(user, username, password, confirmPassword, email, fullname, null);
    }

    @Override
    public CreateUserValidationResult validateCreateUserForAdmin(User user, String username, String password, String confirmPassword, String email, String fullname, Long directoryId)
    {
        final I18nHelper i18nBean = getI18nBean(user);
        final ErrorCollection errors = new SimpleErrorCollection();

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.user.no.permission.to.create"));
            return new CreateUserValidationResult(errors);
        }

        if (applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT))
        {
            String link = getContactAdminLink(i18nBean);
            errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.add.user.external.managment", link));
            return new CreateUserValidationResult(errors);
        }

        errors.addErrorCollection(validateCreateUser(i18nBean, username, password, confirmPassword, email, fullname, directoryId));

        if (directoryId == null)
        {
            // We will add to first writable directory - validate that there is one.
            if (userManager.getWritableDirectories().size() == 0)
            {
                String link = getContactAdminLink(i18nBean);
                errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.add.user.external.managment", link));
            }
        }
        else
        {
            Directory directory = userManager.getDirectory(directoryId);
            if (directory == null)
            {
                errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.add.user.no.such.directory", directoryId));
            }
            else
            {
                if (!directory.getAllowedOperations().contains(OperationType.CREATE_USER))
                {
                    errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.add.user.read.only.directory", directory.getName()));
                }
            }
        }

        if (errors.hasAnyErrors())
        {
            return new CreateUserValidationResult(errors);
        }

        return new CreateUserValidationResult(username, password, email, fullname, directoryId);
    }

    @Override
    public CreateUserValidationResult validateCreateUserForAdmin(com.opensymphony.user.User user, String username, String password, String confirmPassword, String email, String fullname)
    {
        return validateCreateUserForAdmin((User) user, username, password, confirmPassword, email, fullname);
    }

    private ErrorCollection validateCreateUser(I18nHelper i18nBean, final String username, final String password,
            final String confirmPassword, final String email, final String fullname, final Long directoryId)
    {
        final ErrorCollection errors = new SimpleErrorCollection();

        //validate the user params
        if (StringUtils.isEmpty(email))
        {
            errors.addError(FieldName.EMAIL, i18nBean.getText("signup.error.email.required"));
        }
        else if (email.length() > MAX_FIELD_LENGTH)
        {
            errors.addError(FieldName.EMAIL, i18nBean.getText("signup.error.email.greater.than.max.chars"));
        }
        else if (!TextUtils.verifyEmail(email))
        {
            errors.addError(FieldName.EMAIL, i18nBean.getText("signup.error.email.valid"));
        }

        if (StringUtils.isEmpty(fullname))
        {
            errors.addError(FieldName.FULLNAME, i18nBean.getText("signup.error.fullname.required"));
        }
        else if (fullname.length() > MAX_FIELD_LENGTH)
        {
            errors.addError(FieldName.FULLNAME, i18nBean.getText("signup.error.full.name.greater.than.max.chars"));
        }

        if (StringUtils.isEmpty(username))
        {
            errors.addError(FieldName.NAME, i18nBean.getText("signup.error.username.required"));
        }
        else if (username.length() > MAX_FIELD_LENGTH)
        {
            errors.addError(FieldName.NAME, i18nBean.getText("signup.error.username.greater.than.max.chars"));
        }
        else
        {
            for (int i = 0; i < username.length(); i++)
            {
                char c = username.charAt(i);
                if (Character.isUpperCase(c))
                {
                    errors.addError(FieldName.NAME, i18nBean.getText("signup.error.username.allLowercase"));
                    break;
                }
            }
            if (!errors.getErrors().containsKey(FieldName.NAME))
            {
                if (directoryId == null)
                {
                    // Create user in first directory - check if they exist anywhere
                    if (userUtil.userExists(username))
                    {
                        errors.addError(FieldName.NAME, i18nBean.getText("signup.error.username.exists"));
                    }
                }
                else
                {
                    // Creating user in given directory - only check this directory - we allow to create a duplicate.
                    if (userManager.findUserInDirectory(username, directoryId) != null)
                    {
                        errors.addError(FieldName.NAME, i18nBean.getText("signup.error.username.exists"));
                    }
                }
            }
        }

        // If a password has been specified then we need to check they are the same
        // else there is no password specified then check to see if we need one.
        if (StringUtils.isNotEmpty(confirmPassword) || StringUtils.isNotEmpty(password))
        {
            if (password == null || !password.equals(confirmPassword))
            {
                errors.addError(FieldName.CONFIRM_PASSWORD, i18nBean.getText("signup.error.password.mustmatch"));
            }
        }
        return errors;
    }

    public com.opensymphony.user.User createUserNoEvent(final CreateUserValidationResult result)
            throws ImmutableException
    {
        Assertions.notNull("You can not create a user with a null validation result.", result);
        Assertions.stateTrue("You can not create a user with an invalid validation result.", result.isValid());

        final String username = result.getUsername();
        final String password = result.getPassword();
        final String email = result.getEmail();
        final String fullname = result.getFullname();

        return userUtil.createUserNoEvent(username, password, email, fullname);
    }

    public User createUserNoNotification(final CreateUserValidationResult result)
            throws PermissionException, CreateException
    {
        Assertions.notNull("You can not create a user with a null validation result.", result);
        Assertions.stateTrue("You can not create a user with an invalid validation result.", result.isValid());

        final String username = result.getUsername();
        final String password = result.getPassword();
        final String email = result.getEmail();
        final String fullname = result.getFullname();
        final Long directoryId = result.getDirectoryId();

        return userUtil.createUserNoNotification(username, password, email, fullname, directoryId);
    }

    public com.opensymphony.user.User createUserForSignup(final CreateUserValidationResult result)
            throws ImmutableException
    {
        return createUserWithEvent(result, UserEventType.USER_SIGNUP);
    }

    public User createUserFromSignup(final CreateUserValidationResult result)
            throws PermissionException, CreateException
    {
        return createUserWithNotification(result, UserEventType.USER_SIGNUP);
    }

    public com.opensymphony.user.User createUser(final CreateUserValidationResult result)
            throws ImmutableException
    {
        return createUserWithEvent(result, UserEventType.USER_CREATED);
    }

    public User createUserWithNotification(final CreateUserValidationResult result)
            throws PermissionException, CreateException
    {
        return createUserWithNotification(result, UserEventType.USER_CREATED);
    }

    private User createUserWithNotification(final CreateUserValidationResult result, int eventType)
            throws PermissionException, CreateException
    {
        Assertions.notNull("You can not create a user, validation result", result);
        Assertions.stateTrue("You can not create a user with an invalid validation result.", result.isValid());

        final String username = result.getUsername();
        final String password = result.getPassword();
        final String email = result.getEmail();
        final String fullname = result.getFullname();
        final Long directoryId = result.getDirectoryId();

        return userUtil.createUserWithNotification(username, password, email, fullname, directoryId, eventType);
    }

    private com.opensymphony.user.User createUserWithEvent(final CreateUserValidationResult result, int eventType)
            throws ImmutableException
    {
        Assertions.notNull("You can not create a user, validation result", result);
        Assertions.stateTrue("You can not create a user with an invalid validation result.", result.isValid());

        final String username = result.getUsername();
        final String password = result.getPassword();
        final String email = result.getEmail();
        final String fullname = result.getFullname();

        return userUtil.createUserWithEvent(username, password, email, fullname, eventType);
    }

    public DeleteUserValidationResult validateDeleteUser(final User loggedInUser, final String username)
    {
        final I18nHelper i18nBean = getI18nBean(loggedInUser);
        final ErrorCollection errors = new SimpleErrorCollection();

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, loggedInUser))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.delete.no.permission"));
            return new DeleteUserValidationResult(errors);
        }
        if (applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT))
        {
            String link = getContactAdminLink(i18nBean);
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.cannot.delete.due.to.external.user.mgmt", link));
            return new DeleteUserValidationResult(errors);
        }

        if (username == null || "".equals(username))
        {
            errors.addError("username", i18nBean.getText("admin.errors.users.cannot.delete.due.to.invalid.username"));
        }
        if (errors.hasAnyErrors())
        {
            return new DeleteUserValidationResult(errors);
        }

        if (loggedInUser.getName().equalsIgnoreCase(username))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.cannot.delete.currently.logged.in"));
        }

        if (errors.hasAnyErrors())
        {
            return new DeleteUserValidationResult(errors);
        }

        User userForDelete = userUtil.getUser(username);
        if (userForDelete == null)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.user.does.not.exist"));
            return new DeleteUserValidationResult(errors);
        }

        try
        {
            final long numberOfReportedIssues = userUtil.getNumberOfReportedIssuesIgnoreSecurity(loggedInUser, userForDelete);
            if (numberOfReportedIssues > 0)
            {
                errors.addErrorMessage(i18nBean.getText("admin.errors.users.cannot.delete.due.to.reported.issues", "'" + username + "'", "" + numberOfReportedIssues));
            }

            final long numberOfAssignedIssues = userUtil.getNumberOfAssignedIssuesIgnoreSecurity(loggedInUser, userForDelete);
            if (numberOfAssignedIssues > 0)
            {
                errors.addErrorMessage(i18nBean.getText("admin.errors.users.cannot.delete.due.to.assigned.issues", "'" + username + "'", "" + numberOfAssignedIssues));

            }
            final long numberOfProjectsUserLeads = userUtil.getProjectsLeadBy(userForDelete).size();
            if (numberOfProjectsUserLeads > 0)
            {
                errors.addErrorMessage(i18nBean.getText("admin.errors.users.cannot.delete.due.to.project.lead", "'" + username + "'", "" + numberOfProjectsUserLeads));
            }

            if (userUtil.isNonSysAdminAttemptingToDeleteSysAdmin(loggedInUser, userForDelete))
            {
                errors.addErrorMessage(i18nBean.getText("admin.errors.users.cannot.delete.due.to.sysadmin"));
            }
        }
        catch (SearchException e)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.exception.occured.validating") + " " + e);
        }

        if (errors.hasAnyErrors())
        {
            return new DeleteUserValidationResult(errors);
        }

        return new DeleteUserValidationResult(userForDelete);
    }

    @Override
    public DeleteUserValidationResult validateDeleteUser(com.opensymphony.user.User loggedInUser, String username)
    {
        return validateDeleteUser((User) loggedInUser, username);
    }


    public void removeUser(final User user, final DeleteUserValidationResult result)
    {
        Assertions.notNull("You can not remove a user with a null validation result.", result);
        Assertions.stateTrue("You can not remove a user with an invalid validation result.", result.isValid());

        final User userForDelete = result.getUser();

        userUtil.removeUser(user, userForDelete);
    }

    @Override
    public void removeUser(com.opensymphony.user.User user, DeleteUserValidationResult result)
    {
        removeUser((User) user, result);
    }

    I18nHelper getI18nBean(final User user)
    {
        return new I18nBean(user);
    }

    private String getContactAdminLink(I18nHelper i18n)
    {
        return jiraContactHelper.getAdministratorContactMessage(i18n);
    }

}
