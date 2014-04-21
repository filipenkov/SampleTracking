package com.atlassian.crowd.embedded.admin.list;

import com.atlassian.crowd.directory.DelegatedAuthenticationDirectory;
import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.embedded.admin.DirectoryRetriever;
import com.atlassian.crowd.embedded.admin.jirajdbc.JiraJdbcDirectoryConfiguration;
import com.atlassian.crowd.embedded.admin.ldap.LdapConfigurationController;
import com.atlassian.crowd.embedded.admin.util.HtmlEncoder;
import com.atlassian.crowd.embedded.admin.util.MapBuilder;
import com.atlassian.crowd.embedded.admin.util.SimpleMessage;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectorySynchronisationInformation;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.PermissionOption;
import com.atlassian.crowd.embedded.impl.ImmutableDirectory;
import com.atlassian.crowd.exception.DirectoryCurrentlySynchronisingException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.plugin.web.springmvc.xsrf.XsrfTokenGenerator;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.Message;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles listing directories, enabling/disabling and removing directories. New directory creation is handled by
 * the {@link NewDirectoryController} and specific controllers for each directory type (like {@link LdapConfigurationController}).
 */
public final class DirectoriesController
{
    private static final String TYPE_KEY_PREFIX = "embedded.crowd.directory.type.";

    private static final Logger log = LoggerFactory.getLogger(DirectoriesController.class);
    private static final String LIST_DIRECTORIES_VIEW = "list-directories";
    private CrowdService crowdService;
    private CrowdDirectoryService crowdDirectoryService;
    private UserManager userManager;
    private TransactionTemplate transactionTemplate;
    private DirectoryRetriever directoryRetriever;
    private ApplicationProperties applicationProperties;
    private XsrfTokenGenerator xsrfTokenGenerator;
    private LDAPPropertiesMapper ldapPropertiesMapper;
    private HtmlEncoder htmlEncoder;

    private Map<String, Object> getReferenceData(HttpServletRequest request)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("directories", getDirectoryListItems(request));
        model.put("newDirectoryTypes", NewDirectoryType.getValidNewDirectoryTypes(getApplicationType(applicationProperties.getDisplayName())));
        model.put("highlightDirectoryId", request.getParameter("highlightDirectoryId"));
        // Add a reference to the context to itself and also add the request.  Needed to render web item links.
        model.put("context", model);
        model.put("req", request);
        model.put("htmlEncoder", htmlEncoder);
        return model;
    }

    public ModelAndView list(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        return new ModelAndView(LIST_DIRECTORIES_VIEW, getReferenceData(request));
    }

    public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        if (!isXsrfTokenPresentAndCorrect(request))
            return redirectWithSessionTimeoutWarning(request);

        try
        {
            Directory directory = directoryRetriever.getDirectory(request);
            switch (directory.getType())
            {
                case INTERNAL:
                    return new ModelAndView("redirect:/plugins/servlet/embedded-crowd/configure/internal/", MapBuilder.build(DirectoryRetriever.PARAMETER_NAME, directory.getId()));
                case CROWD:
                    return new ModelAndView("redirect:/plugins/servlet/embedded-crowd/configure/crowd/", MapBuilder.build(DirectoryRetriever.PARAMETER_NAME, directory.getId()));
                case DELEGATING:
                    return new ModelAndView("redirect:/plugins/servlet/embedded-crowd/configure/delegatingldap/", MapBuilder.build(DirectoryRetriever.PARAMETER_NAME, directory.getId()));
                case CUSTOM:
                    if (JiraJdbcDirectoryConfiguration.DIRECTORY_CLASS.equals(directory.getImplementationClass()))
                        return new ModelAndView("redirect:/plugins/servlet/embedded-crowd/configure/jirajdbc/", MapBuilder.build(DirectoryRetriever.PARAMETER_NAME, directory.getId()));
                default:
                    return new ModelAndView("redirect:/plugins/servlet/embedded-crowd/configure/ldap/", MapBuilder.build(DirectoryRetriever.PARAMETER_NAME, directory.getId()));
            }
        }
        catch (DirectoryNotFoundException e)
        {
            return directoryNotFound(request);
        }
    }

    public ModelAndView disable(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        if (!isXsrfTokenPresentAndCorrect(request))
            return redirectWithSessionTimeoutWarning(request);

        Directory directory = directoryRetriever.getDirectory(request);
        User currentUser = crowdService.getUser(userManager.getRemoteUsername(request));
        if (!canModifyDirectory(currentUser, directory))
        {
            return directoryInError(request, SimpleMessage.instance("embedded.crowd.current.directory.cannot.disable.remove"));
        }

        return withDirectoryInTransaction(request, new DirectoryOperation()
        {
            public void withDirectory(Directory directory)
            {
                ImmutableDirectory.Builder builder = ImmutableDirectory.newBuilder(directory);
                builder.setActive(false);
                Directory updatedDirectory = builder.toDirectory();
                crowdDirectoryService.updateDirectory(updatedDirectory);
                log.info("User directory disabled: [ {} ], type: [ {} ]", directory.getName(), directory.getType());
            }
        });
    }

    public ModelAndView remove(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        if (!isXsrfTokenPresentAndCorrect(request))
            return redirectWithSessionTimeoutWarning(request);

        Directory directory = directoryRetriever.getDirectory(request);
        User currentUser = crowdService.getUser(userManager.getRemoteUsername(request));
        if (!canModifyDirectory(currentUser, directory))
        {
            return directoryInError(request, SimpleMessage.instance("embedded.crowd.current.directory.cannot.disable.remove"));
        }

        switch (directory.getType())
        {
            case INTERNAL:
                return directoryInError(request, SimpleMessage.instance("embedded.crowd.internal.directory.cannot.remove"));
            default:
                return withDirectoryInTransaction(request, new DirectoryOperation()
                {
                    public void withDirectory(Directory directory)
                    {
                            try
                            {
                                crowdDirectoryService.removeDirectory(directory.getId());
                            }
                            catch (DirectoryCurrentlySynchronisingException e)
                            {
                                throw new DirectoryOperationException(e);
                            }
                            log.info("User directory removed: [ {} ], type: [ {} ]", directory.getName(), directory.getType());
                        }
                });
        }
    }

    public ModelAndView enable(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        if (!isXsrfTokenPresentAndCorrect(request))
            return redirectWithSessionTimeoutWarning(request);

        return withDirectoryInTransaction(request, new DirectoryOperation()
        {
            public void withDirectory(Directory directory)
            {
                ImmutableDirectory.Builder builder = ImmutableDirectory.newBuilder(directory);
                builder.setActive(true);
                Directory updatedDirectory = builder.toDirectory();
                crowdDirectoryService.updateDirectory(updatedDirectory);
                log.info("User directory enabled: [ {} ], type: [ {} ]", directory.getName(), directory.getType());
            }
        });
    }

    public ModelAndView moveUp(final HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        if (!isXsrfTokenPresentAndCorrect(request))
            return redirectWithSessionTimeoutWarning(request);

        return withDirectoryInTransaction(request, new DirectoryOperation()
        {
            public void withDirectory(Directory directory)
            {
                List<Long> directoryIds = getDirectoryIds();
                int currentIndex = directoryIds.indexOf(directory.getId());
                crowdDirectoryService.setDirectoryPosition(directory.getId(), currentIndex > 0 ? currentIndex - 1 : 0);
                if (!userManager.isSystemAdmin(userManager.getRemoteUsername()))
                {
                    crowdDirectoryService.setDirectoryPosition(directory.getId(), currentIndex);
                    throw new DirectoryOperationNotPermittedException("Current user would have lost system admin privileges if directory was moved.", SimpleMessage.instance("embedded.crowd.internal.directory.cannot.reorder"));
                }
                log.info("User directory moved up: [ {} ], type: [ {} ]", directory.getName(), directory.getType());
            }
        });
    }

    public ModelAndView moveDown(final HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        if (!isXsrfTokenPresentAndCorrect(request))
            return redirectWithSessionTimeoutWarning(request);

        return withDirectoryInTransaction(request, new DirectoryOperation()
        {
            public void withDirectory(Directory directory)
            {
                List<Long> directoryIds = getDirectoryIds();
                int currentIndex = directoryIds.indexOf(directory.getId());
                int maxIndex = directoryIds.size() - 1;
                crowdDirectoryService.setDirectoryPosition(directory.getId(), currentIndex < maxIndex ? currentIndex + 1 : maxIndex);
                if (!userManager.isSystemAdmin(userManager.getRemoteUsername()))
                {
                    crowdDirectoryService.setDirectoryPosition(directory.getId(), currentIndex);
                    throw new DirectoryOperationNotPermittedException("Current user would have lost system admin privileges if directory was moved.", SimpleMessage.instance("embedded.crowd.internal.directory.cannot.reorder"));
                }
                log.info("User directory moved down: [ {} ], type: [ {} ]", directory.getName(), directory.getType());
            }
        });
    }

    private List<Long> getDirectoryIds()
    {
        List<Directory> directories = crowdDirectoryService.findAllDirectories();
        List<Long> ids = new ArrayList<Long>(directories.size());
        for (Directory directory : directories)
        {
            ids.add(directory.getId());
        }
        return ids;
    }

    public ModelAndView sync(final HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        if (!isXsrfTokenPresentAndCorrect(request))
            return redirectWithSessionTimeoutWarning(request);

        Directory directory = directoryRetriever.getDirectory(request);
        log.info("User directory synchronisation requested: [ {} ], type: [ {} ]", directory.getName(), directory.getType());
        crowdDirectoryService.synchroniseDirectory(directory.getId());
        return new ModelAndView("redirect:/plugins/servlet/embedded-crowd/directories/list?highlightDirectoryId=" + directory.getId());
    }

    /**
     * Does an operation with the directory, then either returns you to the list of directories
     * or returns an error that the operation could not be completed.
     */
    private ModelAndView withDirectoryInTransaction(final HttpServletRequest request, final DirectoryOperation operation)
    {
        final Directory directory;
        try
        {
            directory = directoryRetriever.getDirectory(request);
            transactionTemplate.execute(new TransactionCallback() {
                public Object doInTransaction()
                {
                    operation.withDirectory(directory);
                    return null;
                }
            });
        }
        catch (DirectoryNotFoundException e)
        {
            log.error("Directory not found: ", e);
            return directoryNotFound(request);
        }
        catch (DirectoryOperationException e)
        {
            log.error("The directory operation failed: ", e);
            if(e.getCause() instanceof DirectoryCurrentlySynchronisingException)
            {
                return directoryInError(request, SimpleMessage.instance("embedded.crowd.directory.not.removable.during.sync"));
            }
            else
            {
                String error = htmlEncoder.encode(e.getMessage());
                return directoryInError(request, SimpleMessage.instance("embedded.crowd.directory.operation.error", error));
            }
        }
        catch (DirectoryOperationNotPermittedException e)
        {
            Message message = e.getI18nMessage();
            if (message != null)
            {
                return directoryInError(request, message);
            }
            else
            {
                String error = htmlEncoder.encode(e.getMessage());
                return directoryInError(request, SimpleMessage.instance("embedded.crowd.directory.operation.error", error));
            }
        }
        return new ModelAndView("redirect:/plugins/servlet/embedded-crowd/directories/list?highlightDirectoryId=" + directory.getId());
    }

    private ModelAndView directoryInError(HttpServletRequest request, Message message)
    {
        Map<String, Object> model = getReferenceData(request);
        model.put("errors", Collections.singleton(message));
        return new ModelAndView(LIST_DIRECTORIES_VIEW, model);
    }

    private ModelAndView directoryNotFound(HttpServletRequest request)
    {
        return directoryInError(request, SimpleMessage.instance("embedded.crowd.directory.not.found"));
    }

    private List<DirectoryListItem> getDirectoryListItems(HttpServletRequest request)
    {
        User currentUser = crowdService.getUser(userManager.getRemoteUsername(request));
        List<DirectoryListItem> directoryListItems = new ArrayList<DirectoryListItem>();
        List<Directory> directories = crowdDirectoryService.findAllDirectories();
        for (int i = 0; i < directories.size(); i++)
        {
            Directory directory = directories.get(i);
            ListItemPosition position = new ListItemPosition(i, directories.size());
            DirectorySynchronisationInformation syncInfo = crowdDirectoryService.getDirectorySynchronisationInformation(directory.getId());
            directoryListItems.add(new DirectoryListItem(directory, getTypeName(directory), currentUser, position, syncInfo, getApplicationType(applicationProperties.getDisplayName())));
        }
        return directoryListItems;
    }

    public void setCrowdDirectoryService(CrowdDirectoryService CrowdDirectoryService)
    {
        this.crowdDirectoryService = CrowdDirectoryService;
    }

    public void setCrowdService(CrowdService crowdService)
    {
        this.crowdService = crowdService;
    }

    public void setLdapPropertiesMapper(LDAPPropertiesMapper ldapPropertiesMapper)
    {
        this.ldapPropertiesMapper = ldapPropertiesMapper;
    }

    public void setHtmlEncoder(final HtmlEncoder htmlEncoder)
    {
        this.htmlEncoder = htmlEncoder;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate)
    {
        this.transactionTemplate = transactionTemplate;
    }

    public void setDirectoryRetriever(DirectoryRetriever directoryRetriever)
    {
        this.directoryRetriever = directoryRetriever;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setApplicationProperties(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public void setXsrfTokenGenerator(XsrfTokenGenerator xsrfTokenGenerator)
    {
        this.xsrfTokenGenerator = xsrfTokenGenerator;
    }

    private boolean isXsrfTokenPresentAndCorrect(HttpServletRequest request)
    {
        return xsrfTokenGenerator.validateToken(request, request.getParameter(XsrfTokenGenerator.REQUEST_PARAM_NAME));
    }

    private ModelAndView redirectWithSessionTimeoutWarning(HttpServletRequest request)
    {
        return new ModelAndView(new RedirectView(request.getServletPath() + "/directories/list?timeout=true", true));
    }

    private static interface DirectoryOperation
    {
        public void withDirectory(Directory directory) throws DirectoryOperationException;
    }

    public final class DirectoryOperationException extends RuntimeException
    {
        public DirectoryOperationException(String message)
        {
            super(message);
        }

        public DirectoryOperationException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public DirectoryOperationException(Throwable cause)
        {
            super(cause);
        }
    }

    public final class DirectoryOperationNotPermittedException extends RuntimeException
    {
        private Message message;

        public DirectoryOperationNotPermittedException(String englishMessage, Message i18nMessage)
        {
            super(englishMessage);
            this.message = i18nMessage;
        }

        public Message getI18nMessage()
        {
            return message;
        }
    }

    private Message getTypeName(Directory directory)
    {
        DirectoryType directoryType = directory.getType();
        switch (directoryType)
        {
            case CONNECTOR:
                String implemntationName = getNameForImplementation(directory.getImplementationClass());
                String name = implemntationName == null ? directoryType.name() : implemntationName;
                PermissionOption permissionOption = PermissionOption.fromAllowedOperations(directory.getAllowedOperations());
                return SimpleMessage.instance(TYPE_KEY_PREFIX + directoryType.name() + "." + permissionOption.name(), name);
            case CUSTOM:
                return SimpleMessage.instance(TYPE_KEY_PREFIX + directoryType.name() + getClassNameOnly(directory.getImplementationClass()));
            case DELEGATING:
                final String implementationClass = directory.getValue(DelegatedAuthenticationDirectory.ATTRIBUTE_LDAP_DIRECTORY_CLASS);
                return SimpleMessage.instance(TYPE_KEY_PREFIX + directoryType.name(), getNameForImplementation(implementationClass));
            default:
                return SimpleMessage.instance(TYPE_KEY_PREFIX + directoryType.name());
        }
    }

    private String getNameForImplementation(final String implementationClass)
    {

        Map<String, String> implementations = ldapPropertiesMapper.getImplementations();
        for (Map.Entry<String, String> entry : implementations.entrySet())
        {
            if (entry.getValue().equals(implementationClass))
            {
                return entry.getKey();
            }
        }
        return null;
    }

    private String getClassNameOnly(String implementationClass)
    {
        return implementationClass.substring(implementationClass.lastIndexOf("."));
    }

    private boolean canModifyDirectory(User currentUser, Directory directory)
    {
        return currentUser.getDirectoryId() != directory.getId();
    }

    /**
     * Represents an operation on one of the directories in the UI. These link back to methods on this class.
     */
    public enum Operation
    {
        EDIT("edit"),
        ENABLE("enable"),
        DISABLE("disable"),
        REMOVE("remove"),
        TROUBLESHOOT("troubleshoot");

        private static final String LABEL_PREFIX = "embedded.crowd.operation.";
        private static final String URL_PREFIX = "/plugins/servlet/embedded-crowd/directories/";

        private final String methodName;

        Operation(String methodName)
        {
            this.methodName = methodName;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getUrl(Directory directory)
        {
            return URL_PREFIX + methodName + "?" + DirectoryRetriever.PARAMETER_NAME + "=" + directory.getId();
        }

        public Message getMessage()
        {
            return SimpleMessage.instance(LABEL_PREFIX + name());
        }
    }

    public ApplicationType getApplicationType(String applicationName)
    {
        try
        {
            return ApplicationType.valueOf(applicationName.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            return ApplicationType.GENERIC_APPLICATION;
        }
    }
}
