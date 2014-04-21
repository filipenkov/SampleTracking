/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.bc.license.JiraLicenseUpdaterService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.SystemPropertyKeys;
import com.atlassian.jira.license.LicenseDetails;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.system.JiraSystemRestarter;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.constraint.IsNull;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockServletContext;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import webwork.action.ServletActionContext;

import java.util.Date;

public class TestConfirmNewInstallationWithOldLicense extends LegacyJiraMockTestCase
{
    private static final String PASSWORD = "testpassword";
    private PermissionManager oldPermissionManager;
    private JiraLicenseUpdaterService jiraLicenseService;
    private BuildUtilsInfo buildUtilsInfo;
    private JiraSystemRestarter jiraSystemRestarter;
    private static final String A_VALID_LICENSE = "a valid license";
    private ApplicationProperties applicationProperties;

    public TestConfirmNewInstallationWithOldLicense(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        oldPermissionManager = ComponentAccessor.getPermissionManager();
        jiraLicenseService = createMock(JiraLicenseUpdaterService.class);
        buildUtilsInfo = createMock(BuildUtilsInfo.class);
        jiraSystemRestarter  = createMock(JiraSystemRestarter.class);
        applicationProperties = ComponentManager.getComponentInstanceOfType(ApplicationProperties.class);
        applicationProperties.setString(APKeys.JIRA_PATCHED_VERSION, "610");
    }

    @Override
    protected void tearDown() throws Exception
    {
        UtilsForTests.cleanUsers();
        ManagerFactory.addService(PermissionManager.class, oldPermissionManager);
        oldPermissionManager = null;
        super.tearDown();
    }

    // Test that an error is raised (by doVildation()) when the user name is not supplied
    public void testErrorIfNoUserName() throws Exception
    {
        expect(buildUtilsInfo.getCurrentBuildNumber()).andReturn("610").anyTimes();
        replay(buildUtilsInfo);
        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense = new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter);
        confirmNewInstallationWithOldLicense.setPassword(PASSWORD);
        confirmNewInstallationWithOldLicense.setConfirm("confirm");
        confirmNewInstallationWithOldLicense.execute();
        assertEquals(1, confirmNewInstallationWithOldLicense.getErrorMessages().size());
    }

    // Test that an error is raised (by doValidation()) when the wrong user name is supplied
    public void testErrorIfWrongUserName() throws Exception
    {
        expect(buildUtilsInfo.getCurrentBuildNumber()).andReturn("610").anyTimes();
        replay(buildUtilsInfo);
        final User user = UtilsForTests.getTestUser("testuser");
        user.setPassword(PASSWORD);

        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission",
            new Constraint[] { new IsEqual(new Integer(Permissions.ADMINISTER)), new IsNull(), new IsEqual(user), P.IS_ANYTHING }, Boolean.TRUE);
        mockPermissionManager.setStrict(true);
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) mockPermissionManager.proxy());

        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense = new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter);
        confirmNewInstallationWithOldLicense.setUserName("baduser");
        confirmNewInstallationWithOldLicense.execute();
        assertEquals(1, confirmNewInstallationWithOldLicense.getErrorMessages().size());
    }

    // Test that an error is raised (by doValidation()) when the wrong password is supplied
    public void testErrorIfWrongPassword() throws Exception
    {
        expect(buildUtilsInfo.getCurrentBuildNumber()).andReturn("610").anyTimes();
        replay(buildUtilsInfo);
        final User user = UtilsForTests.getTestUser("testuser");
        user.setPassword(PASSWORD);

        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission",
            new Constraint[] { new IsEqual(new Integer(Permissions.ADMINISTER)), new IsNull(), new IsEqual(user), P.IS_ANYTHING }, Boolean.TRUE);
        mockPermissionManager.setStrict(true);
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) mockPermissionManager.proxy());

        final MockServletContext mockServletContext = new MockServletContext();
        ServletActionContext.setServletContext(mockServletContext);

        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense = new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter);
        confirmNewInstallationWithOldLicense.setUserName(user.getName());
        confirmNewInstallationWithOldLicense.setPassword("badpassword");

        // Set the confirmation so that no errors are raised for its absense (or the absense of the new license key)
        confirmNewInstallationWithOldLicense.setConfirm("confirm");
        confirmNewInstallationWithOldLicense.execute();
        checkSingleElementCollection(confirmNewInstallationWithOldLicense.getErrorMessages(), "The username or password are incorrect");
    }

    // Test that the error is given when the user name and password are correct but the user is NOT admin user
    public void testErrorIfNotAdmin() throws Exception
    {
        expect(buildUtilsInfo.getCurrentBuildNumber()).andReturn("610").anyTimes();
        replay(buildUtilsInfo);
        final User user = UtilsForTests.getTestUser("testuser");
        user.setPassword(PASSWORD);

        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission",
            new Constraint[] { new IsEqual(new Integer(Permissions.ADMINISTER)), new IsEqual(user) }, Boolean.FALSE);
        mockPermissionManager.setStrict(true);
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) mockPermissionManager.proxy());

        final MockServletContext mockServletContext = new MockServletContext();
        ServletActionContext.setServletContext(mockServletContext);

        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense = new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter);
        confirmNewInstallationWithOldLicense.setUserName(user.getName());
        confirmNewInstallationWithOldLicense.setPassword(PASSWORD);

        // Set the confirmation so that no errors are raised for its absense (or the absense of the new license key)
        confirmNewInstallationWithOldLicense.setConfirm("confirm");
        confirmNewInstallationWithOldLicense.execute();
        assertEquals(1, confirmNewInstallationWithOldLicense.getErrors().size());
        assertEquals(
            "User does not have administration permission. It is possible that the version you have upgraded from does not have recognised administrators.  This can be overridden by adding system property '-Dnon.admin.upgrade=true' when starting JIRA.",
            confirmNewInstallationWithOldLicense.getErrors().get("userName"));
    }

    // Test that an error is NOT raised (by doValidation()) when the the correct admin credentials are supplied
    public void testNoErrorIfAdmin() throws Exception
    {
        expect(buildUtilsInfo.getCurrentBuildNumber()).andReturn("610").anyTimes();
        replay(buildUtilsInfo);
        final User user = UtilsForTests.getTestUser("testadminuser");
        user.setPassword(PASSWORD);

        final Group group = UtilsForTests.getTestGroup("testadmingroup");
        user.addToGroup(group);

        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission",
            new Constraint[] { new IsEqual(new Integer(Permissions.ADMINISTER)), new IsEqual(user) }, Boolean.TRUE);
        mockPermissionManager.setStrict(true);
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) mockPermissionManager.proxy());

        final MockServletContext mockServletContext = new MockServletContext();
        ServletActionContext.setServletContext(mockServletContext);

        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense = new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter);
        confirmNewInstallationWithOldLicense.setUserName(user.getName());
        confirmNewInstallationWithOldLicense.setPassword(PASSWORD);
        confirmNewInstallationWithOldLicense.setConfirm("confirm");
        confirmNewInstallationWithOldLicense.execute();
        assertEquals(0, confirmNewInstallationWithOldLicense.getErrorMessages().size());
        assertEquals(0, confirmNewInstallationWithOldLicense.getErrors().size());
        mockPermissionManager.verify();
    }

    // Test that an error is NOT raised (by doValidation()) when the the correct admin credentials are supplied
    public void testNoErrorIfNotAdminWithSystemProperty() throws Exception
    {
        expect(buildUtilsInfo.getCurrentBuildNumber()).andReturn("610").anyTimes();
        replay(buildUtilsInfo);
        System.setProperty(SystemPropertyKeys.UPGRADE_SYSTEM_PROPERTY, "true");

        final User user = UtilsForTests.getTestUser("testadminuser");
        user.setPassword(PASSWORD);

        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectNotCalled("hasPermission");
        mockPermissionManager.setStrict(true);
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) mockPermissionManager.proxy());

        final MockServletContext mockServletContext = new MockServletContext();
        ServletActionContext.setServletContext(mockServletContext);

        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense = new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter);
        confirmNewInstallationWithOldLicense.setUserName(user.getName());
        confirmNewInstallationWithOldLicense.setPassword(PASSWORD);
        confirmNewInstallationWithOldLicense.setConfirm("confirm");
        confirmNewInstallationWithOldLicense.execute();
        assertEquals(0, confirmNewInstallationWithOldLicense.getErrorMessages().size());
        assertEquals(0, confirmNewInstallationWithOldLicense.getErrors().size());
        mockPermissionManager.verify();
    }

    // Test that error message is raised if no license key or confirmation is given
    public void testErrorIfNoLicenseKeyAndNoConfirm() throws Exception
    {
        expect(buildUtilsInfo.getCurrentBuildNumber()).andReturn("610").anyTimes();
        replay(buildUtilsInfo);
        final User user = UtilsForTests.getTestUser("testadminuser");
        user.setPassword("testadminpassword");

        final Group group = UtilsForTests.getTestGroup("testadmingroup");
        user.addToGroup(group);

        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission",
            new Constraint[] { new IsEqual(new Integer(Permissions.ADMINISTER)), new IsEqual(user) }, Boolean.TRUE);
        mockPermissionManager.setStrict(true);
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) mockPermissionManager.proxy());

        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense = new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter);
        confirmNewInstallationWithOldLicense.setUserName("testadminuser");
        confirmNewInstallationWithOldLicense.setPassword("testadminpassword");
        confirmNewInstallationWithOldLicense.execute();
        assertEquals(1, confirmNewInstallationWithOldLicense.getErrorMessages().size());
        assertEquals(0, confirmNewInstallationWithOldLicense.getErrors().size());
    }


    // Ensure that no error is raised when a license key is supplied
    public void testNoErrorIfLicenseKey() throws Exception
    {
        final User user = UtilsForTests.getTestUser("testadminuser");
        user.setPassword(PASSWORD);

        final Group group = UtilsForTests.getTestGroup("testadmingroup");
        user.addToGroup(group);

        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission",
            new Constraint[] { new IsEqual(new Integer(Permissions.ADMINISTER)), new IsEqual(user) }, Boolean.TRUE);
        mockPermissionManager.setStrict(true);
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) mockPermissionManager.proxy());

        final JiraLicenseService.ValidationResult validationResult = createMock(JiraLicenseService.ValidationResult.class);
        final LicenseDetails licenseDetails = createMock(LicenseDetails.class);
        final Date buildDate = new Date();
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        expect(jiraLicenseService.validate(EasyMock.<I18nHelper>anyObject(), eq(A_VALID_LICENSE))).andReturn(validationResult);
        expect(validationResult.getErrorCollection()).andReturn(errorCollection);

        expect(jiraLicenseService.setLicense(validationResult)).andReturn(licenseDetails);
        expect(buildUtilsInfo.getCurrentBuildNumber()).andReturn("610").anyTimes();
        expect(buildUtilsInfo.getCurrentBuildDate()).andReturn(buildDate);
        expect(licenseDetails.isMaintenanceValidForBuildDate(buildDate)).andReturn(true);
        replay(jiraLicenseService, buildUtilsInfo, licenseDetails, validationResult);


        final MockServletContext mockServletContext = new MockServletContext();
        ServletActionContext.setServletContext(mockServletContext);

        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense = new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter);
        confirmNewInstallationWithOldLicense.setUserName(user.getName());
        confirmNewInstallationWithOldLicense.setPassword(PASSWORD);
        confirmNewInstallationWithOldLicense.setLicense(A_VALID_LICENSE);
        confirmNewInstallationWithOldLicense.execute();
        assertEquals(0, confirmNewInstallationWithOldLicense.getErrorMessages().size());
        assertEquals(0, confirmNewInstallationWithOldLicense.getErrors().size());

       verify(jiraLicenseService, buildUtilsInfo, licenseDetails, validationResult);

    }

    // Ensure that no error is raised if the confirmation is supplied
    public void testNoErrorIfConfirm() throws Exception
    {
        expect(buildUtilsInfo.getCurrentBuildNumber()).andReturn("610").anyTimes();
        replay(buildUtilsInfo);
        final User user = UtilsForTests.getTestUser("testadminuser");
        user.setPassword(PASSWORD);

        final Group group = UtilsForTests.getTestGroup("testadmingroup");
        user.addToGroup(group);

        final Mock mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.expectAndReturn("hasPermission",
            new Constraint[] { new IsEqual(new Integer(Permissions.ADMINISTER)), new IsEqual(user) }, Boolean.TRUE);
        mockPermissionManager.setStrict(true);
        ManagerFactory.addService(PermissionManager.class, (PermissionManager) mockPermissionManager.proxy());

        final MockServletContext mockServletContext = new MockServletContext();
        ServletActionContext.setServletContext(mockServletContext);

        final ConfirmNewInstallationWithOldLicense confirmNewInstallationWithOldLicense = new ConfirmNewInstallationWithOldLicense(jiraLicenseService, buildUtilsInfo, jiraSystemRestarter);
        confirmNewInstallationWithOldLicense.setUserName(user.getName());
        confirmNewInstallationWithOldLicense.setPassword(PASSWORD);
        confirmNewInstallationWithOldLicense.setConfirm("confirm");
        confirmNewInstallationWithOldLicense.execute();
        assertEquals(0, confirmNewInstallationWithOldLicense.getErrorMessages().size());
        assertEquals(0, confirmNewInstallationWithOldLicense.getErrors().size());
    }
}
