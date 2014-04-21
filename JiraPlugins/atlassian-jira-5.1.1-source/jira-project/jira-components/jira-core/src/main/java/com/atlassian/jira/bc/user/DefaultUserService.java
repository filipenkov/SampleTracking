package com.atlassian.jira.bc.user;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.user.UserEventType;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraContactHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.dbc.Assertions;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;

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
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final JiraContactHelper jiraContactHelper;
    private final I18nHelper.BeanFactory i18nFactory;

    public DefaultUserService(UserUtil userUtil, PermissionManager permissionManager, UserManager userManager,
            JiraContactHelper jiraContactHelper, I18nHelper.BeanFactory i18nFactory, JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.userUtil = userUtil;
        this.permissionManager = permissionManager;
        this.userManager = userManager;
        this.jiraContactHelper = jiraContactHelper;
        this.i18nFactory = i18nFactory;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Override
    public CreateUserValidationResult validateCreateUserForSignup(final User loggedInUser, final String username, final String password,
            final String confirmPassword, final String email, final String fullname)
    {
        return validateCreateUserForSignupOrSetup(loggedInUser, username, password, confirmPassword, email, fullname, true);
    }

    @Override
    public CreateUserValidationResult validateCreateUserForSetup(final User loggedInUser, final String username, final String password,
            final String confirmPassword, final String email, final String fullname)
    {
        return validateCreateUserForSignupOrSetup(loggedInUser, username, password, confirmPassword, email, fullname, false);
    }

    @Override
    public CreateUserValidationResult validateCreateUserForSignupOrSetup(final User loggedInUser, final String username, final String password,
            final String confirmPassword, final String email, final String fullname)
    {
        return validateCreateUserForSignupOrSetup(loggedInUser, username, password, confirmPassword, email, fullname, true);
    }

    private CreateUserValidationResult validateCreateUserForSignupOrSetup(final User loggedInUser, final String username, final String password,
            final String confirmPassword, final String email, final String fullname, boolean checkForWritableDirectory)
    {
        final I18nHelper i18nBean = getI18nBean(loggedInUser);
        final ErrorCollection errors = new SimpleErrorCollection();

        if (checkForWritableDirectory && !userManager.hasWritableDirectory())
        {
            String link = getContactAdminLink(i18nBean);
            errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.add.user.all.directories.read.only.contact.admin", link));
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

        if (!userManager.hasWritableDirectory())
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.add.user.all.directories.read.only"));
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
    public CreateUserValidationResult validateCreateUserForAdmin(final User user, final String username, final String password,
            final String confirmPassword, final String email, final String fullname)
    {
        return validateCreateUserForAdmin(user, username, password, confirmPassword, email, fullname, null);
    }

    @Override
    public CreateUserValidationResult validateCreateUserForAdmin(User user, String username, String password, String confirmPassword, String email, String fullname, @Nullable Long directoryId)
    {
        final I18nHelper i18nBean = getI18nBean(user);
        final ErrorCollection errors = new SimpleErrorCollection();

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.user.no.permission.to.create"));
            return new CreateUserValidationResult(errors);
        }

        if (!userManager.hasWritableDirectory())
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.add.user.all.directories.read.only"));
            return new CreateUserValidationResult(errors);
        }

        errors.addErrorCollection(validateCreateUser(i18nBean, username, password, confirmPassword, email, fullname, directoryId));

        if (directoryId == null)
        {
            // We will add to first writable directory - validate that there is one.
            if (userManager.getWritableDirectories().size() == 0)
            {
                errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.add.user.all.directories.read.only"));
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
    public CreateUsernameValidationResult validateCreateUsername(final User loggedInUser, final String username)
    {
        return validateCreateUsername(loggedInUser, username, null);
    }

    @Override
    public CreateUsernameValidationResult validateCreateUsername(final User loggedInUser, final String username, final Long directoryId)
    {
        final I18nHelper i18nBean = getI18nBean(loggedInUser);
        final ErrorCollection errors = new SimpleErrorCollection();

        validateCreateUsername(username, directoryId, i18nBean, errors);

        return new CreateUsernameValidationResult(username, directoryId, errors);
    }

    private ErrorCollection validateCreateUser(I18nHelper i18nBean, final String username, final String password,
            final String confirmPassword, final String email, final String fullname, @Nullable final Long directoryId)
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

        validateCreateUsername(username, directoryId, i18nBean, errors);

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

    private void validateCreateUsername(final String username, final Long directoryId, final I18nHelper i18nBean, final ErrorCollection errors)
    {
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
            if(StringUtils.containsAny(username, new char[] {'<', '>', '&'}))
            {
                errors.addError(FieldName.NAME, i18nBean.getText("signup.error.username.invalid.chars"));
            }

            if (!errors.getErrors().containsKey(FieldName.NAME))
            {
                if (directoryId == null)
                {
                    // Check if the username exists in any directory
                    if (userUtil.userExists(username))
                    {
                        errors.addError(FieldName.NAME, i18nBean.getText("signup.error.username.exists"));
                    }
                }
                else
                {
                    // Check if the username exists in the given directory - we allow duplicates in other directories
                    if (userManager.findUserInDirectory(username, directoryId) != null)
                    {
                        errors.addError(FieldName.NAME, i18nBean.getText("signup.error.username.exists"));
                    }
                }
            }
        }
    }

    @Override
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

    @Override
    public User createUserFromSignup(final CreateUserValidationResult result)
            throws PermissionException, CreateException
    {
        return createUserWithNotification(result, UserEventType.USER_SIGNUP);
    }

    @Override
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

    @Override
    public UpdateUserValidationResult validateUpdateUser(User user)
    {
        final User loggedInUser = jiraAuthenticationContext.getLoggedInUser();
        final I18nHelper i18nBean = getI18nBean(loggedInUser);
        final ErrorCollection errors = new SimpleErrorCollection();

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, loggedInUser))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.update.no.permission"));
            return new UpdateUserValidationResult(errors);
        }
        // Check the user actually exists
        User userToUpdate = userManager.getUser(user.getName());
        if (userToUpdate == null)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.user.does.not.exist"));
            return new UpdateUserValidationResult(errors);
        }
        // Is the directory writable?
        if (!userManager.canUpdateUser(userToUpdate))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.cannot.edit.user.directory.read.only"));
            return new UpdateUserValidationResult(errors);
        }
        // Is a standard admin trying to update a SysAdmin?
        if (isNonSysAdminAttemptingToUpdateSysAdmin(loggedInUser, userToUpdate))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.must.be.sysadmin.to.edit.sysadmin"));
            return new UpdateUserValidationResult(errors);
        }
        // Special checks for deactivate
        if (!user.isActive())
        {
            final Collection<ProjectComponent> components = userUtil.getComponentsUserLeads(userToUpdate);
            if (components.size() > 0)
            {
                String projectList = getDisplayableProjectList(getProjectsFor(components));
                // Show this error against the field, because we cannot post HTML to error messages, so we put up an
                // explicit error message with link to components. See EditUser.java and editprofile.jsp
                errors.addError("active", i18nBean.getText("admin.errors.users.cannot.deactivate.due.to.component.lead", projectList));
            }

            Collection<Project> projects = userUtil.getProjectsLeadBy(userToUpdate);
            if (projects.size() > 0)
            {
                String projectList = getDisplayableProjectList(projects);
                // Show this error against the field, because we cannot post HTML to error messages, so we put up an
                // explicit error message with link to components. See EditUser.java and editprofile.jsp
                errors.addError("active", i18nBean.getText("admin.errors.users.cannot.deactivate.due.to.project.lead", projectList));
            }

            if (loggedInUser.getName().equalsIgnoreCase(user.getName()))
            {
                errors.addErrorMessage(i18nBean.getText("admin.errors.users.cannot.deactivate.currently.logged.in"));
            }
        }

        if (errors.hasAnyErrors())
        {
            return new UpdateUserValidationResult(errors);
        }
        else
        {
            return new UpdateUserValidationResult(user);
        }
    }

    private Collection<Project> getProjectsFor(Collection<ProjectComponent> components)
    {
        ProjectManager projectManager = ComponentAccessor.getProjectManager();
        HashSet<Project> projects = new HashSet<Project>(components.size());
        for (ProjectComponent component : components)
        {
            projects.add(projectManager.getProjectObj(component.getProjectId()));
        }
        return projects;
    }

    private String getDisplayableProjectList(Collection<Project> projects)
    {
        final Collection<String> projectKeys = Collections2.transform(projects, new Function<Project, String>()
        {
            @Override
            public String apply(@Nullable Project from)
            {
                return from.getKey();
            }
        });
        return StringUtils.join(projectKeys, ", ");
    }

    private boolean isNonSysAdminAttemptingToUpdateSysAdmin(User loggedInUser, User userToUpdate)
    {
        return permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, userToUpdate) &&
                !permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, loggedInUser);
    }

    @Override
    public void updateUser(UpdateUserValidationResult updateUserValidationResult)
    {
        if (updateUserValidationResult.isValid())
        {
            userManager.updateUser(updateUserValidationResult.getUser());
        }
        else
        {
            throw new IllegalStateException("Invalid UpdateUserValidationResult");
        }
    }

    @Override
    public DeleteUserValidationResult validateDeleteUser(final User loggedInUser, final String username)
    {
        final I18nHelper i18nBean = getI18nBean(loggedInUser);
        final ErrorCollection errors = new SimpleErrorCollection();

        if (!permissionManager.hasPermission(Permissions.ADMINISTER, loggedInUser))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.delete.no.permission"));
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

        User userForDelete = userManager.getUser(username);
        if (userForDelete == null)
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.user.does.not.exist"));
            return new DeleteUserValidationResult(errors);
        }

        if (!userManager.canUpdateUser(userForDelete))
        {
            errors.addErrorMessage(i18nBean.getText("admin.errors.users.cannot.delete.user.read.only"));
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
    public void removeUser(final User loggedInUser, final DeleteUserValidationResult result)
    {
        Assertions.notNull("You can not remove a user with a null validation result.", result);
        Assertions.stateTrue("You can not remove a user with an invalid validation result.", result.isValid());

        final User userForDelete = result.getUser();

        userUtil.removeUser(loggedInUser, userForDelete);
    }

    I18nHelper getI18nBean(final User user)
    {
        return i18nFactory.getInstance(user);
    }

    private String getContactAdminLink(I18nHelper i18n)
    {
        return jiraContactHelper.getAdministratorContactMessage(i18n);
    }
}
