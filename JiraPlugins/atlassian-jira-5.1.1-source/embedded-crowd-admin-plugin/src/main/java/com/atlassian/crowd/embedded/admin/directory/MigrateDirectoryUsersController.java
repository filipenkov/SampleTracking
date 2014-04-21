package com.atlassian.crowd.embedded.admin.directory;

import com.atlassian.crowd.embedded.admin.util.HtmlEncoder;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.embedded.impl.ImmutableDirectory;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class MigrateDirectoryUsersController extends SimpleFormController
{
    private static final Logger log = LoggerFactory.getLogger(MigrateDirectoryUsersController.class);
    private CrowdDirectoryService crowdDirectoryService;
    private UserManager userManager;
    private I18nResolver i18nResolver;
    private TransactionTemplate transactionTemplate;
    private HtmlEncoder htmlEncoder;
    private DirectoryManager directoryManager;
    private ApplicationProperties applicationProperties;

    protected Map referenceData(HttpServletRequest request) throws Exception
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("htmlEncoder", htmlEncoder);
        model.put("directories", getDirectories());
        return model;
    }

    private Map<String, String> getDirectories()
    {
        Map<String, String> directories = new LinkedHashMap<String, String>();

        // switch keys and values because the Crowd map is back-to-front
        for (Directory directory : crowdDirectoryService.findAllDirectories())
        {
            if (directory.getType() == DirectoryType.INTERNAL || directory.getType() == DirectoryType.DELEGATING)
            {
                directories.put(directory.getId().toString(), directory.getName());
            }
        }
        return directories;
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception
    {
        return createCommand();
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception
    {
        String remoteUser = userManager.getRemoteUsername(request);

        MigrateDirectoryUsersCommand migrateUsersCommand = (MigrateDirectoryUsersCommand) command;

        migrateUsers(migrateUsersCommand.getFromDirectoryId(), migrateUsersCommand.getToDirectoryId(), remoteUser, migrateUsersCommand, errors);

        if (errors.hasErrors())
        {
            return showForm(request, response, errors, referenceData(request));
        }
        return showForm(request, response, errors, referenceData(request));
    }

    private void migrateUsers(final long fromDirectoryId, final long toDirectoryId, final String remoteUser, final MigrateDirectoryUsersCommand migrateUsersCommand, final BindException errors)
    {
        // Check the to & from directories

        Directory from = validateDirectory(fromDirectoryId, errors, "fromDirectoryId");
        Directory to = validateDirectory(toDirectoryId, errors, "toDirectoryId");
        if (to != null && to.equals(from))
        {
            errors.addError(new FieldError("migration", "toDirectoryId", i18nResolver.getText("embedded.crowd.directory.migrate.users.field.directory.same")));
        }
        if (errors.hasErrors())
        {
            return;
        }

        setDirectoryEnabled(from, false);
        setDirectoryEnabled(to, false);

        // Copy
        SearchRestriction restriction = NullRestrictionImpl.INSTANCE;
        UserQuery<User> query = new UserQuery<User>(User.class, restriction, 0, -1);
        try
        {
            // TODO: Sucking all users in like this may be problematic for Confluence.  If planning to enable this
            // TODO: feature for other products make sure the performance impacts are understood.
            List<User> users = directoryManager.searchUsers(fromDirectoryId, query);
            final AtomicLong migratedCount = new AtomicLong(0);
            for (Iterator<User> iter = users.iterator(); iter.hasNext(); )
            {
                final User user = iter.next();
                transactionTemplate.execute(new TransactionCallback() {
                    public Object doInTransaction()
                    {
                        try
                        {
                            migrateUser(fromDirectoryId, toDirectoryId, remoteUser, user, migratedCount);
                        }
                        catch (Exception e)
                        {
                            throw new RuntimeException(e);
                        }
                        return null;
                    }
                });
            }
            migrateUsersCommand.setTestSuccessful(true);
            migrateUsersCommand.setTotalCount(users.size());
            migrateUsersCommand.setMigratedCount(migratedCount.get());
        }
        catch (Exception e)
        {
            log.error("User migration failed", e);
            errors.addError(new ObjectError("migration", i18nResolver.getText("embedded.crowd.directory.migrate.users.error", htmlEncoder.encode(e.getMessage()))));
        }

        // Enable both directories
        setDirectoryEnabled(from, true);
        setDirectoryEnabled(to, true);
    }

    private void migrateUser(final long fromDirectoryId, final long toDirectoryId, final String remoteUser, final User user, final AtomicLong migratedCount) throws Exception
    {
        if (!user.getName().equalsIgnoreCase(remoteUser))
        {
            UserWithAttributes userWithAttributes = directoryManager.findUserWithAttributesByName(fromDirectoryId, user.getName());
            try
            {
                final UserTemplate newUser = new UserTemplate(user);
                newUser.setDirectoryId(toDirectoryId);
                directoryManager.addUser(toDirectoryId, newUser, new PasswordCredential(generatePassword()));
            }
            catch (InvalidUserException e)
            {
                // That's fine just go on to the next user.  Don't copy the groups.
                return;
            }
            // Migrate attributes
            Set<String> keys = userWithAttributes.getKeys();
            Map<String, Set<String>> attributes = new HashMap<String, Set<String>>();
            for (String key : keys)
            {
                Set<String> values = userWithAttributes.getValues(key);
                attributes.put(key, values);
            }
            directoryManager.storeUserAttributes(toDirectoryId, user.getName(), attributes);

            MembershipQuery<Group> groupQuery = QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user.getName()).returningAtMost(EntityQuery.ALL_RESULTS);
            List<Group> groups = directoryManager.searchDirectGroupRelationships(fromDirectoryId, groupQuery);
            for (Group group : groups)
            {
                // We may need to add the group first
                try
                {
                    directoryManager.findGroupByName(toDirectoryId, group.getName());
                }
                catch (GroupNotFoundException ex)
                {
                    final GroupTemplate newGroup = new GroupTemplate(group);
                    newGroup.setDirectoryId(toDirectoryId);
                    directoryManager.addGroup(toDirectoryId, newGroup);
                }
                directoryManager.addUserToGroup(toDirectoryId, user.getName(), group.getName());
                directoryManager.removeUserFromGroup(fromDirectoryId, user.getName(), group.getName());
            }
            directoryManager.removeUser(fromDirectoryId, user.getName());
            migratedCount.addAndGet(1);
        }
    }

    private Directory validateDirectory(final Long directoryId, final BindException errors, String field)
    {
        if (directoryId == -1)
        {
            errors.addError(new FieldError("migration", field, i18nResolver.getText("embedded.crowd.directory.migrate.users.field.directory.required")));
            return null;
        }
        Directory directory = crowdDirectoryService.findDirectoryById(directoryId);
        if (directory == null)
        {
            errors.addError(new FieldError("migration", field, i18nResolver.getText("embedded.crowd.directory.migrate.users.field.directory.not.found")));
        }
        else
        {
            if (directory.getType() != DirectoryType.INTERNAL && directory.getType() != DirectoryType.DELEGATING)
            {
                errors.addError(new FieldError("migration", field, i18nResolver.getText("embedded.crowd.directory.migrate.users.field.directory.wrong.type")));
            }
            final Set<OperationType> allowedOperations = directory.getAllowedOperations();
            if (!allowedOperations.contains(OperationType.CREATE_USER) ||
                    !allowedOperations.contains(OperationType.CREATE_GROUP) ||
                    !allowedOperations.contains(OperationType.DELETE_USER) ||
                    !allowedOperations.contains(OperationType.DELETE_GROUP))
            {
                errors.addError(new FieldError("migration", field, i18nResolver.getText("embedded.crowd.directory.migrate.users.field.directory.read.only")));
            }
        }
        return directory;
    }

    private void setDirectoryEnabled(final Directory from, final boolean enabled)
    {
        transactionTemplate.execute(new TransactionCallback()
        {
            public Object doInTransaction()
            {
                ImmutableDirectory.Builder builder = ImmutableDirectory.newBuilder(from);
                builder.setActive(enabled);
                Directory updatedDirectory = builder.toDirectory();
                crowdDirectoryService.updateDirectory(updatedDirectory);
                log.info("User directory {}: [ {} ], type: [ {} ]", new String[] { enabled ? "enabled" : "disabled", from.getName(), from.getType().toString() });
                return null;
            }
        });
    }

    /**
     * Generates a Random Password that can be used when the user has entered a blank password.
     * <p>
     * The password is guaranteed to contain at least one upper-case letter, lower-case letter and number in case the
     * backend user Directory has password restrictions.
     *
     * @return a random password.
     */
    public static String generatePassword()
    {
        // Crowd requires a password, so we set it randomly
        // and so the user cannot ever log in with it.
        // We append ABab23 so as to pass most password REGEX type tests.
        Random random = new Random();
        return new BigInteger(130, random).toString(32) + "ABab23";
    }

    public CrowdDirectoryService getCrowdDirectoryService()
    {
        return crowdDirectoryService;
    }

    public void setCrowdDirectoryService(final CrowdDirectoryService crowdDirectoryService)
    {
        this.crowdDirectoryService = crowdDirectoryService;
    }

    public I18nResolver getI18nResolver()
    {
        return i18nResolver;
    }

    public void setI18nResolver(final I18nResolver i18nResolver)
    {
        this.i18nResolver = i18nResolver;
    }

    public TransactionTemplate getTransactionTemplate()
    {
        return transactionTemplate;
    }

    public void setTransactionTemplate(final TransactionTemplate transactionTemplate)
    {
        this.transactionTemplate = transactionTemplate;
    }

    public HtmlEncoder getHtmlEncoder()
    {
        return htmlEncoder;
    }

    public void setHtmlEncoder(final HtmlEncoder htmlEncoder)
    {
        this.htmlEncoder = htmlEncoder;
    }

    public DirectoryManager getDirectoryManager()
    {
        return directoryManager;
    }

    public void setDirectoryManager(final DirectoryManager directoryManager)
    {
        this.directoryManager = directoryManager;
    }

    public ApplicationProperties getApplicationProperties()
    {
        return applicationProperties;
    }

    public void setApplicationProperties(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public UserManager getUserManager()
    {
        return userManager;
    }

    public void setUserManager(final UserManager userManager)
    {
        this.userManager = userManager;
    }
}
