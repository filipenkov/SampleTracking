/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail.webwork;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.mail.MailLoggingManager;
import com.atlassian.jira.mock.MockFeatureManager;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugins.mail.MockAbstractMessageHandler;
import com.atlassian.jira.plugins.mail.internal.DefaultMessageHandlerFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.service.util.handler.MessageHandlerFactory;
import com.atlassian.mail.MailFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 *
 * @since v5.0
 */
public abstract class AbstractMailServerTest
{
    @Mock
    private PermissionManager permissionManager;

    protected MockMailServerManager serverManager;
    protected MockComponentWorker worker;
    protected MockFeatureManager mockFeatureManager;

    @Before
    public void setUpBase() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        serverManager = new MockMailServerManager();
        MailFactory.setServerManager(serverManager);

        worker = new MockComponentWorker();
        ComponentAccessor.initialiseWorker(worker);

        worker.addMock(WebResourceManager.class, Mockito.mock(WebResourceManager.class));
        final MockAuthenticationContext authenticationContext = new MockAuthenticationContext(null);
        final ResourceBundle pluginBundle = new PropertyResourceBundle(getClass().getResourceAsStream("/com/atlassian/jira/plugins/mail/messages.properties"));
        final ResourceBundle defaultResourceBundle = authenticationContext.getI18nHelper().getDefaultResourceBundle();
        final Method setParent = ResourceBundle.class.getDeclaredMethod("setParent", ResourceBundle.class);
        setParent.setAccessible(true);
        setParent.invoke(defaultResourceBundle, pluginBundle);
        worker.addMock(JiraAuthenticationContext.class, authenticationContext);
        worker.addMock(PermissionManager.class, permissionManager);
        final MailLoggingManager mailLoggingManager = Mockito.mock(MailLoggingManager.class, Mockito.RETURNS_MOCKS);
        worker.addMock(MailLoggingManager.class, mailLoggingManager);

        // wseliga: I hate that I have to do it
        // these tests are so brittle and so fucked-up and not prepared for any kind of reasonable dependency inject
        // that I have to plug here my mock ComponentClassManager which is used only in one place:
        // by newly introduced DefaultMessageHandlerFactory which instatiates all handlers
        // without it many tests would fail because altough they test something different they still
        // cause init() on MessageHandler to be executed and init fails if not mocked correctly
        // one of the reasons is that creating deep mocks of a class (not interface) with Mockito has apparently
        // side-effects like static final fields being not initialized at all (!) - in our case
        // any attempt to invoke a method on log field causes NPE.
        // All in all: this stinking pile of unit tests needs a few weeks of full developer attention and they have to be
        // seriously refactored.
        worker.addMock(ComponentClassManager.class, new ComponentClassManager()
        {
            @Override
            public <T> T newInstance(String className) throws ClassNotFoundException
            {
                return (T) Mockito.mock(MockAbstractMessageHandler.class);
            }

            @Override
            public <T> T newInstanceFromPlugin(Class<T> clazz, Plugin plugin)
            {
                return null;
            }

            @Override
            public <T> Class<T> loadClass(String className) throws ClassNotFoundException
            {
                return null;
            }
        }
        );

        mockFeatureManager = new MockFeatureManager();
        worker.addMock(FeatureManager.class, mockFeatureManager);
        final PluginAccessor pluginAccessor = Mockito.mock(PluginAccessor.class);
        worker.addMock(PluginAccessor.class, pluginAccessor);

        worker.addMock(MessageHandlerFactory.class, new DefaultMessageHandlerFactory(mailLoggingManager, pluginAccessor));

        Mockito.when(permissionManager.hasPermission(Mockito.eq(Permissions.SYSTEM_ADMIN), Mockito.<User>any())).thenReturn(true);
    }
}
