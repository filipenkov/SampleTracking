package com.atlassian.crowd.embedded.admin;

import com.atlassian.crowd.embedded.admin.util.HtmlEncoder;
import com.atlassian.crowd.embedded.admin.util.MapBuilder;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.impl.ImmutableDirectory;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.CrowdException;
import com.atlassian.crowd.exception.DirectoryInstantiationException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.manager.directory.DirectoryPermissionException;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.constants.DirectoryTermKeys;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for the two directory configuration controllers.
 */
public abstract class ConfigurationController extends SimpleFormController
{
    private static final Logger log = LoggerFactory.getLogger(ConfigurationController.class);
    private CrowdDirectoryService crowdDirectoryService;
    protected DirectoryMapper directoryMapper;
    protected DirectoryRetriever directoryRetriever;
    private I18nResolver i18nResolver;
    private TransactionTemplate transactionTemplate;
    private HtmlEncoder htmlEncoder;
    private DirectoryManager directoryManager;
    private ApplicationProperties applicationProperties;

    protected Map referenceData(HttpServletRequest request) throws Exception
    {
        return MapBuilder.build("htmlEncoder", htmlEncoder);
    }


    protected final ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception
    {
        Directory directory = createDirectory(command);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("htmlEncoder", htmlEncoder);

        if (WebUtils.hasSubmitParameter(request, "save"))
        {
            try
            {
                final Directory savedDirectory = saveDirectory(directory);
				final String successView = StringUtils.replace(getSuccessView(), "{directoryId}", String.valueOf(savedDirectory.getId()));
				return new ModelAndView(successView, errors.getModel());
            }
            catch (DirectoryInstantiationException e)
            {
                String error = htmlEncoder.encode(e.getMessage());
                addObjectError(errors, "embedded.crowd.save.directory.failed", error);
            }
        }
        else if (WebUtils.hasSubmitParameter(request, "test"))
        {
            // test the configuration
            try
            {
                crowdDirectoryService.testConnection(directory);
                log.info("Configuration test successful for user directory: [ {} ], type: [ {} ]", directory.getName(), directory.getType());
                model.put("testSuccessful", true);
                model.put("successMessage", i18nResolver.getText("embedded.crowd.connection.test.successful"));
                model.put("successMessage", i18nResolver.getText("embedded.crowd.connection.test.successful.caution"));
            }
            catch (OperationFailedException e)
            {
                log.error("Configuration test failed for user directory: [ " + directory.getName() + "], " +
                    "type: [ " + directory.getType() + " ]", e);
                String rawMessage = e.getMessage();
                String error = htmlEncoder.encode(rawMessage);
                addObjectError(errors, "embedded.crowd.connection.test.failed", error);
                if (rawMessage.toLowerCase().contains("error code"))
                {
                    model.put("addErrorCodeLink", true);
                }
            }
        }
        else
        {
            addObjectError(errors, "embedded.crowd.validation.submission.mode.missing");
        }
        return showForm(request, response, errors, model);
    }

    protected abstract Directory createDirectory(Object command);

    /**
     * Creates updated directory by merging new directory with old directory.
     *
     * @param oldDirectory existing directory
     * @param newDirectory directory with new values
     * @return updated directory
     */
    protected Directory createUpdatedDirectory(final Directory oldDirectory, final Directory newDirectory)
    {
        // Create a directory builder based on the new directory.
        final ImmutableDirectory.Builder builder = ImmutableDirectory.newBuilder(newDirectory);

        // Creation date comes from the old directory.
        builder.setCreatedDate(oldDirectory.getCreatedDate());

        // Preserve old attributes that are not overridden by new attributes.
        final Map<String, String> updatedAttributes = new HashMap<String, String>(oldDirectory.getAttributes());
        updatedAttributes.putAll(newDirectory.getAttributes());
        builder.setAttributes(updatedAttributes);

        return builder.toDirectory();
    }

    private boolean directoryNameInUse(String directoryName)
    {
        EntityQuery<Directory> directoryQuery = QueryBuilder.queryFor(Directory.class, EntityDescriptor.directory())
            .with(Restriction.on(DirectoryTermKeys.NAME).exactlyMatching(directoryName))
            .returningAtMost(EntityQuery.ALL_RESULTS);

        return !directoryManager.searchDirectories(directoryQuery).isEmpty();
    }

    private Directory saveDirectory(final Directory directory) throws DirectoryInstantiationException
    {
        if (directory.getId() <= 0)
        {
            if (StringUtils.isEmpty(directory.getName()))
            {
                throw new DirectoryInstantiationException(i18nResolver.getText("embedded.crowd.validation.directory.name.required", directory.getName()));
            }

            if (directoryNameInUse(directory.getName()))
            {
                throw new DirectoryInstantiationException(i18nResolver.getText("embedded.crowd.validation.directory.name.conflict", directory.getName()));
            }

            return transactionTemplate.execute(new TransactionCallback<Directory>()
            {
                public Directory doInTransaction()
                {
                    log.info("User directory created: [ {} ], type: [ {} ]", directory.getName(), directory.getType());
                    Directory newDirectory = crowdDirectoryService.addDirectory(directory);
                    postprocessDirectory(newDirectory);
                    return newDirectory;
                }
            });
        }
        else
        {
            return transactionTemplate.execute(new TransactionCallback<Directory>()
            {
                public Directory doInTransaction()
                {
                    log.info("User directory updated: [ {} ], type: [ {} ]", directory.getName(), directory.getType());

                    final Directory oldDirectory = crowdDirectoryService.findDirectoryById(directory.getId());

                    final Directory updatedDirectory = createUpdatedDirectory(oldDirectory, directory);

                    Directory newDirectory = crowdDirectoryService.updateDirectory(updatedDirectory);
                    postprocessDirectory(newDirectory);
                    return newDirectory;
                }
            });
        }
    }

    protected String getDefaultLdapAutoAddGroups()
    {
        final String applicationName = applicationProperties.getDisplayName();
        try
        {
            final ApplicationType applicationType = ApplicationType.valueOf(applicationName.toUpperCase());
            switch (applicationType)
            {
                case CONFLUENCE:
                    return "confluence-users";
                case JIRA:
                    return "jira-users";
                default:
                    // fall through
                    log.warn("No auto default add group is defined for " + applicationName);
            }
        }
        catch (IllegalArgumentException e)
        {
            log.warn("'" + applicationName + "' is an unknown application. Default auto add groups will not be set.");
        }

        return "";
    }

    /**
     * Used for performing additional operations to the directory after it has been created.
     * <p/>
     * By default this method ensures that non-existing auto add groups are created.
     *
     * @param directory directory to perform post-processing for
     */
    protected void postprocessDirectory(Directory directory)
    {
        String groups = directory.getAttributes().get(DirectoryImpl.ATTRIBUTE_KEY_AUTO_ADD_GROUPS);

        if (StringUtils.isNotBlank(groups))
        {
            for (String groupName : StringUtils.split(groups, "|"))
            {
                try
                {
                    ensureGroupExistsInDirectory(directory.getId(), groupName);
                }
                catch (CrowdException e)
                {
                    log.warn("Failed to create group '" + groupName + "' for auto-add groups of '" + directory.getName() + "'", e);
                }
                catch (ApplicationPermissionException e)
                {
                    log.warn("Failed to create group '" + groupName + "' for auto-add groups of '" + directory.getName() + "'", e);
                }
            }
        }
    }

    /**
     * A simplified copy of the private method ApplicationServiceGeneric.ensureGroupExistsInDirectory.
     */
    private void ensureGroupExistsInDirectory(final long directoryId, final String groupName)
        throws GroupNotFoundException, ApplicationPermissionException, com.atlassian.crowd.exception.OperationFailedException, DirectoryNotFoundException
    {
        try
        {
            // See if the Group already exists in the given directory
            directoryManager.findGroupByName(directoryId, groupName);
        }
        catch (final GroupNotFoundException ex)
        {
            // Do not require that group to exist in any other directory
            try
            {
                directoryManager.addGroup(directoryId, new GroupTemplate(groupName, directoryId));
            }
            catch (final DirectoryPermissionException ex2)
            {
                throw new ApplicationPermissionException("Group '" + groupName + "' does not exist in the directory of the user and cannot be added.");
            }
            catch (InvalidGroupException e)
            {
                throw new com.atlassian.crowd.exception.OperationFailedException(e.getMessage(), e);
            }
        }
    }

    private void addObjectError(BindException errors, String message, Serializable... arguments)
    {
        errors.addError(new ObjectError("configuration", i18nResolver.getText(message, arguments)));
    }

    public final void setCrowdDirectoryService(CrowdDirectoryService crowdDirectoryService)
    {
        this.crowdDirectoryService = crowdDirectoryService;
    }

    public final void setDirectoryMapper(DirectoryMapper directoryMapper)
    {
        this.directoryMapper = directoryMapper;
    }

    public final void setDirectoryRetriever(DirectoryRetriever directoryRetriever)
    {
        this.directoryRetriever = directoryRetriever;
    }

    public final void setI18nResolver(I18nResolver i18nResolver)
    {
        this.i18nResolver = i18nResolver;
    }

    public final void setTransactionTemplate(TransactionTemplate transactionTemplate)
    {
        this.transactionTemplate = transactionTemplate;
    }

    protected I18nResolver getI18nResolver()
    {
        return i18nResolver;
    }

    public void setHtmlEncoder(HtmlEncoder htmlEncoder)
    {
        this.htmlEncoder = htmlEncoder;
    }

    public void setDirectoryManager(DirectoryManager directoryManager)
    {
        this.directoryManager = directoryManager;
    }

    public void setApplicationProperties(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }
}
