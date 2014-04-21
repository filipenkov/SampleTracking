package com.atlassian.crowd.embedded.admin.support;

import com.atlassian.crowd.directory.DelegatedAuthenticationDirectory;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import com.atlassian.crowd.directory.loader.DirectoryInstanceLoader;
import com.atlassian.crowd.embedded.admin.DirectoryRetriever;
import com.atlassian.crowd.embedded.admin.util.HtmlEncoder;
import com.atlassian.crowd.embedded.admin.util.SimpleMessage;
import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.PermissionOption;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.message.Message;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class TroubleshootingController extends SimpleFormController
{
    private static final Logger log = LoggerFactory.getLogger(TroubleshootingController.class);

    private static final String TYPE_KEY_PREFIX = "embedded.crowd.directory.type.";
    private static final String TEST_CONNECT = "embedded.crowd.directory.test.connect";
    private static final String TEST_AUTHENTICATE = "embedded.crowd.directory.test.authenticate";
    private static final String TEST_GET_USER = "embedded.crowd.directory.test.get.user";
    private static final String TEST_GET_GROUP = "embedded.crowd.directory.test.get.group";
    private static final String TEST_GET_MEMBERS = "embedded.crowd.directory.test.get.members";
    private static final String TEST_GET_MEMBERS_FAIL = TEST_GET_MEMBERS + ".fail";
    private static final String TEST_GET_MEMBERSHIPS = "embedded.crowd.directory.test.get.memberships";
    private static final String TEST_GET_MEMBERSHIPS_FAIL = TEST_GET_MEMBERSHIPS + ".fail";

    private CrowdDirectoryService crowdDirectoryService;
    private DirectoryManager directoryManager;
    private HtmlEncoder htmlEncoder;
    private DirectoryRetriever directoryRetriever;
    private I18nResolver i18nResolver;
    private TroubleshootingCommand credential;
    private LDAPPropertiesMapper ldapPropertiesMapper;
    private DirectoryInstanceLoader directoryInstanceLoader;

    protected Map referenceData(HttpServletRequest request)
    {
        Map model = new HashMap();
        model.put("htmlEncoder", htmlEncoder);

        try
        {
            Directory directory = directoryRetriever.getDirectory(request);
            model.put("directoryName", directory.getName());
            model.put("directoryType", getTypeName(directory));
        }
        catch (DirectoryNotFoundException e)
        {
            log.error("Directory not found: ", e);

            model.put("errors", Collections.singleton(i18nResolver.getText("embedded.crowd.directory.not.found")));
            return model;
        }
        return model;
    }

    private boolean testConnection(final RemoteDirectory directory, final List<TestResult> testResults)
    {
        try
        {
            directory.testConnection();
            testResults.add(new TestResult(i18nResolver.getText(TEST_CONNECT), true, true));
            return true;
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            TestResult testResult = new TestResult(i18nResolver.getText(TEST_CONNECT), true, false);
            testResult.addError(htmlEncoder.encode(e.getMessage()));
            testResults.add(testResult);
            return false;
        }
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request,	HttpServletResponse response, Object command, BindException errors) throws Exception
    {


        Map model = new HashMap();
        List<TestResult> testResults = new ArrayList<TestResult>();

        TroubleshootingCommand tCommand = (TroubleshootingCommand) command;

        RemoteDirectory rawDirectory = null;
        Directory directory = crowdDirectoryService.findDirectoryById(tCommand.getDirectoryId());
        if (directory == null)
        {
            addObjectError(errors, "embedded.crowd.directory.not.found");
        }
        else
        {
            rawDirectory = directoryInstanceLoader.getRawDirectory(directory.getId(), directory.getImplementationClass(), directory.getAttributes());
        }
        if (rawDirectory == null)
        {
            addObjectError(errors, "embedded.crowd.directory.not.found");
        }
        else
        {

            // We perform a series of tests and add any error to the error collection
            boolean connectionOk = testConnection(rawDirectory, testResults);

            User user = null;
            if (connectionOk && !StringUtils.isEmpty(tCommand.getUsername()))
            {
                user = testGetUser(rawDirectory, tCommand.getUsername().trim(), testResults);
            }
            else
            {
                testResults.add(new TestResult(i18nResolver.getText(TEST_GET_USER), false, false));
            }

            if (user != null)
            {
                testMemberships(rawDirectory,  user.getName(), testResults);
            }
            else
            {
                testResults.add(new TestResult(i18nResolver.getText(TEST_GET_MEMBERSHIPS_FAIL), false, false));
                testResults.add(new TestResult(i18nResolver.getText(TEST_GET_GROUP), false, false));
                testResults.add(new TestResult(i18nResolver.getText(TEST_GET_MEMBERS_FAIL), false, false));
            }

            if (user != null && !StringUtils.isEmpty(user.getName()) && !StringUtils.isEmpty(tCommand.getPassword()))
            {
                user = testAuthenticate(rawDirectory,  user.getName(), tCommand.getPassword(), testResults);
            }
            else
            {
                testResults.add(new TestResult(i18nResolver.getText(TEST_AUTHENTICATE), false, false));
            }

            model.put("testResults", testResults);
        }
        return showForm(request, response, errors, model);
    }

    private User testGetUser(final RemoteDirectory directory, String username, final List<TestResult> testResults)
    {
        User user = null;
        try
        {
            // set the context class loader to this one, so that sun LDAP classes use the right classloader
            // (the same classloader as Crowd embedded itself).
            // See JRADEV-6188.
            // A better fix would be within crowd embedded itself, CWD-2414/CWD-2244
            final Thread currentThread = Thread.currentThread();
            final ClassLoader origCCL = currentThread.getContextClassLoader();
            try
            {
                currentThread.setContextClassLoader(directoryInstanceLoader.getClass().getClassLoader());
                user = directory.findUserByName(username);
            }
            finally
            {
                currentThread.setContextClassLoader(origCCL);
            }
            testResults.add(new TestResult(i18nResolver.getText(TEST_GET_USER), true, true));
        }
        catch (Exception e)
        {
            TestResult testResult = new TestResult(i18nResolver.getText(TEST_GET_USER), true, false);
            testResult.addError(htmlEncoder.encode(e.getMessage()));
            testResults.add(testResult);
        }
        return user;
    }

    private void testMemberships(final RemoteDirectory directory, String username, final List<TestResult> testResults)
    {
        List<Group> groups = null;
        try
        {
            final MembershipQuery<Group> query = QueryBuilder.createMembershipQuery(EntityQuery.ALL_RESULTS, 0, false,
                    EntityDescriptor.group(), Group.class, EntityDescriptor.user(), username);

            // set the context class loader to this one, so that sun LDAP classes use the right classloader
            // (the same classloader as Crowd embedded itself).
            // See JRADEV-6188.
            // A better fix would be within crowd embedded itself, CWD-2414/CWD-2244
            final Thread currentThread = Thread.currentThread();
            final ClassLoader origCCL = currentThread.getContextClassLoader();
            try
            {
                currentThread.setContextClassLoader(directoryInstanceLoader.getClass().getClassLoader());
                groups = directory.searchGroupRelationships(query);
            }
            finally
            {
                currentThread.setContextClassLoader(origCCL);
            }
            testResults.add(new TestResult(i18nResolver.getText(TEST_GET_MEMBERSHIPS, groups.size()), true, groups.size() > 0));

        }
        catch (Exception e)
        {
            TestResult testResult = new TestResult(i18nResolver.getText(TEST_GET_MEMBERSHIPS_FAIL), true, false);
            testResult.addError(htmlEncoder.encode(e.getMessage()));
            testResults.add(testResult);

            testResults.add(new TestResult(i18nResolver.getText(TEST_GET_GROUP), false, false));
            testResults.add(new TestResult(i18nResolver.getText(TEST_GET_MEMBERS_FAIL), false, false));
        }

        if (groups != null && groups.size() > 0)
        {
            Group group = null;
            try
            {
                // set the context class loader to this one, so that sun LDAP classes use the right classloader
                // (the same classloader as Crowd embedded itself).
                // See JRADEV-6188.
                // A better fix would be within crowd embedded itself, CWD-2414/CWD-2244
                final Thread currentThread = Thread.currentThread();
                final ClassLoader origCCL = currentThread.getContextClassLoader();
                try
                {
                    currentThread.setContextClassLoader(directoryInstanceLoader.getClass().getClassLoader());
                    group = directory.findGroupByName(groups.get(0).getName());
                }
                finally
                {
                    currentThread.setContextClassLoader(origCCL);
                }
                testResults.add(new TestResult(i18nResolver.getText(TEST_GET_GROUP), true, true));
            }
            catch (Exception e)
            {
                TestResult testResult = new TestResult(i18nResolver.getText(TEST_GET_GROUP), true, false);
                testResult.addError(htmlEncoder.encode(e.getMessage()));
                testResults.add(testResult);

                testResults.add(new TestResult(i18nResolver.getText(TEST_GET_MEMBERS_FAIL), false, false));
            }
            if (group != null)
            {

                try
                {
                    final MembershipQuery<User> query = QueryBuilder.createMembershipQuery(EntityQuery.ALL_RESULTS, 0, true,
                            EntityDescriptor.user(), User.class, EntityDescriptor.group(), group.getName());
                    // set the context class loader to this one, so that sun LDAP classes use the right classloader
                    // (the same classloader as Crowd embedded itself).
                    // See JRADEV-6188.
                    // A better fix would be within crowd embedded itself, CWD-2414/CWD-2244
                    final Thread currentThread = Thread.currentThread();
                    final ClassLoader origCCL = currentThread.getContextClassLoader();
                    List<User> members;
                    try
                    {
                        currentThread.setContextClassLoader(directoryInstanceLoader.getClass().getClassLoader());
                        members = directory.searchGroupRelationships(query);
                    }
                    finally
                    {
                        currentThread.setContextClassLoader(origCCL);
                    }
                    testResults.add(new TestResult(i18nResolver.getText(TEST_GET_MEMBERS, members.size()), true, members.size() > 0));
                }
                catch (Exception e)
                {
                    TestResult testResult = new TestResult(i18nResolver.getText(TEST_GET_MEMBERS_FAIL), true, false);
                    testResult.addError(htmlEncoder.encode(e.getMessage()));
                    testResults.add(testResult);
                }
            }
        }
        else
        {
            testResults.add(new TestResult(i18nResolver.getText(TEST_GET_GROUP), false, false));
            testResults.add(new TestResult(i18nResolver.getText(TEST_GET_MEMBERS_FAIL), false, false));
        }
    }

    private User testAuthenticate(final RemoteDirectory directory, final String username, final String password, final List<TestResult> testResults)
    {
        User user = null;
        try
        {
            directoryManager.authenticateUser(directory.getDirectoryId(), username, new PasswordCredential(password));
            // set the context class loader to this one, so that sun LDAP classes use the right classloader
            // (the same classloader as Crowd embedded itself).
            // See JRADEV-6188.
            // A better fix would be within crowd embedded itself, CWD-2414/CWD-2244
            final Thread currentThread = Thread.currentThread();
            final ClassLoader origCCL = currentThread.getContextClassLoader();
            try
            {
                currentThread.setContextClassLoader(directoryInstanceLoader.getClass().getClassLoader());
                user = directory.authenticate(username, new PasswordCredential(password));
            }
            finally
            {
                currentThread.setContextClassLoader(origCCL);
            }
            testResults.add(new TestResult(i18nResolver.getText(TEST_AUTHENTICATE), true, true));
        }
        catch (Exception e)
        {
            TestResult testResult = new TestResult(i18nResolver.getText(TEST_AUTHENTICATE), true, false);
            testResult.addError(htmlEncoder.encode(e.getMessage()));
            testResults.add(testResult);
        }
        return user;
    }

    public void setCrowdDirectoryService(CrowdDirectoryService CrowdDirectoryService)
    {
        this.crowdDirectoryService = CrowdDirectoryService;
    }

    public void setDirectoryManager(final DirectoryManager directoryManager)
    {
        this.directoryManager = directoryManager;
    }

    public void setDirectoryInstanceLoader(final DirectoryInstanceLoader directoryInstanceLoader)
    {
        this.directoryInstanceLoader = directoryInstanceLoader;
    }

    public void setHtmlEncoder(final HtmlEncoder htmlEncoder)
    {
        this.htmlEncoder = htmlEncoder;
    }

    public void setDirectoryRetriever(DirectoryRetriever directoryRetriever)
    {
        this.directoryRetriever = directoryRetriever;
    }

    public void setLdapPropertiesMapper(final LDAPPropertiesMapper ldapPropertiesMapper)
    {
        this.ldapPropertiesMapper = ldapPropertiesMapper;
    }

    public final void setI18nResolver(I18nResolver i18nResolver)
    {
        this.i18nResolver = i18nResolver;
    }

    protected Object formBackingObject(HttpServletRequest request) throws Exception
    {
        TroubleshootingCommand command = new TroubleshootingCommand();
        try
        {
            long directoryId = Long.parseLong(request.getParameter("directoryId"));
            command.setDirectoryId(directoryId);
        }
        catch (NumberFormatException e)
        {
            command.setDirectoryId(-1);
        }
        return command;
    }

    private void addObjectError(BindException errors, String message, Serializable... arguments)
    {
        errors.addError(new ObjectError("credential", i18nResolver.getText(message, arguments)));
    }

    public class TestResult
    {
        private String test;
        private boolean performed;
        private boolean result;
        private boolean addErrorCodeLink;
        private List<String> errors = new ArrayList<String>();

        private TestResult(final String test, final boolean performed, final boolean result)
        {
            this.test = test;
            this.performed = performed;
            this.result = result;
        }

        public String getTest()
        {
            return test;
        }

        public boolean getResult()
        {
            return result;
        }

        public boolean isPerformed()
        {
            return performed;
        }

        public List<String> getErrors()
        {
            return errors;
        }

        public void addError(final String error)
        {
            errors.add(error);
            if (error.toLowerCase().contains("error code"))
            {
                addErrorCodeLink = true;
            }
        }

        public boolean isAddErrorCodeLink()
        {
            return addErrorCodeLink;
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

}
