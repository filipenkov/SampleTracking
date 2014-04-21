/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.user;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.user.GroupUtils;
import com.atlassian.core.user.UserUtils;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.bc.user.UserServiceResultHelper;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.servlet.JiraCaptchaService;
import com.atlassian.jira.servlet.JiraCaptchaServiceImpl;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.opensymphony.user.User;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import webwork.action.Action;
import webwork.action.ActionContext;

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class TestSignup extends AbstractUsersTestCase
{

    JiraCaptchaService jiraCaptchaService;
    private UserUtil userUtil;
    private UserService userService;

    public TestSignup(String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        jiraCaptchaService = new JiraCaptchaServiceImpl();
        userUtil = createMock(UserUtil.class);
        userService = createMock(UserService.class);
    }

    public void testValidationOfCaptcha() throws Exception
    {
        final String sessionId = "testSessionId";
        Object sessionDelegate = new Object()
        {
            public String getId()
            {
                return sessionId;
            }
        };
        final HttpSession session = (HttpSession) DuckTypeProxy.getProxy(HttpSession.class, sessionDelegate);

        Object requestDelegate = new Object()
        {
            public Object getAttribute(String string)
            {
                return null;
            }

            public HttpSession getSession(boolean create)
            {
                return session;
            }
        };
        HttpServletRequest request = (HttpServletRequest) DuckTypeProxy.getProxy(HttpServletRequest.class, requestDelegate);

        UserService.CreateUserValidationResult validationResult = UserServiceResultHelper.getCreateUserValidationResult();

        MockApplicationProperties applicationProperties = new MockApplicationProperties();
        applicationProperties.setOption(APKeys.JIRA_OPTION_CAPTCHA_ON_SIGNUP, true);

        expect(userUtil.canActivateNumberOfUsers(EasyMock.anyInt())).andReturn(true);

        expect(userService.validateCreateUserForSignupOrSetup(
                EasyMock.<User>anyObject(),
                EasyMock.<String>anyObject(),
                EasyMock.<String>anyObject(),
                EasyMock.<String>anyObject(),
                EasyMock.<String>anyObject(),
                EasyMock.<String>anyObject())).andReturn(validationResult);

        replay(userService, userUtil);
        Signup s = new Signup(applicationProperties, userService, userUtil, jiraCaptchaService);
        s.setUsername("abc");

        jiraCaptchaService.getImageCaptchaService().getImageChallengeForID(sessionId);
        HttpServletRequest oldRequest = ActionContext.getRequest();
        try
        {
            ActionContext.setRequest(request);
            String result = s.execute();

            assertEquals(Action.INPUT, result);
            assertEquals(s.getText("signup.error.captcha.incorrect"), s.getErrors().get("captcha"));
        }
        finally
        {
            ActionContext.setRequest(oldRequest);
        }
    }

    public void testNoValidationOfCaptchaIfOff() throws Exception
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        errors.addError("fullname", "You must specify a full name.");
        errors.addError("email", "You must specify an email address.");
        errors.addError("password", "You must specify a password and a confirmation password.");
        UserService.CreateUserValidationResult validationResult =
                UserServiceResultHelper.getCreateUserValidationResult(errors);

        MockApplicationProperties applicationProperties = new MockApplicationProperties();
        applicationProperties.setOption(APKeys.JIRA_OPTION_CAPTCHA_ON_SIGNUP, false);
        expect(userUtil.canActivateNumberOfUsers(EasyMock.anyInt())).andReturn(true);

        expect(userService.validateCreateUserForSignupOrSetup(
                EasyMock.<User>anyObject(),
                EasyMock.<String>anyObject(),
                EasyMock.<String>anyObject(),
                EasyMock.<String>anyObject(),
                EasyMock.<String>anyObject(),
                EasyMock.<String>anyObject())).andReturn(validationResult);

        replay(userService, userUtil);
        
        Signup s = new Signup(applicationProperties, userService, userUtil, jiraCaptchaService);
        s.setUsername("abc");

        String result = s.execute();

        assertEquals(Action.INPUT, result);
        assertNull(s.getErrors().get("captcha"));
    }

    public void testExecute() throws Exception
    {
        createMockDirectory();

        GroupUtils.getGroupSafely("g1");
        GroupUtils.getGroupSafely("g2");
        GroupUtils.getGroupSafely("g3");

        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.USE, "g1");
        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.USE, "g2");

        final UserUtil userUtil = ComponentAccessor.getUserUtil();
        final UserService userService = ComponentManager.getComponentInstanceOfType(UserService.class);
        Signup s = new Signup(new MockApplicationProperties(), userService, userUtil, new JiraCaptchaServiceImpl());
        s.setUsername("a");
        s.setPassword("b");
        s.setConfirm("b");
        s.setFullname("c");
        s.setEmail("d@d.com");

        String result = s.execute();

        assertEquals(Action.SUCCESS, result);

        User user = UserUtils.getUser("a");
        Collection groups = user.getGroups();
        assertEquals(2, groups.size());
        assertTrue(groups.contains("g1"));
        assertTrue(groups.contains("g2"));
        assertTrue(!groups.contains("g3"));
    }

    private Directory createMockDirectory()
    {
        // Create a directory for users to be created in
        DirectoryImpl directory = new DirectoryImpl("Internal", DirectoryType.INTERNAL, "xxx");
        directory.addAllowedOperation(OperationType.CREATE_USER);
        directory.addAllowedOperation(OperationType.UPDATE_USER);
        directory.addAllowedOperation(OperationType.DELETE_USER);
        ComponentAccessor.getComponentOfType(DirectoryDao.class).add(directory);

        return directory;
    }
}
