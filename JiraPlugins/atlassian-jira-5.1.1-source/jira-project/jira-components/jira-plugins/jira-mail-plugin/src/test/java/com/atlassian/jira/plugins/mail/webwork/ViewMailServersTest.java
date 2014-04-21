/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.mail.webwork;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.mail.MailLoggingManager;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugins.mail.MockAbstractMessageHandler;
import com.atlassian.jira.plugins.mail.extensions.MessageHandlerModuleDescriptor;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.JiraServiceContainerImpl;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.service.services.mail.MailFetcherService;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.map.MapPropertySet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import java.util.Collections;

public class ViewMailServersTest extends AbstractMailServerTest
{

    public static final String NO_PROJECT = "catchemail=catch@email.com,createusers=true,notifyusers=true,ccwatcher=false,ccassignee=false,stripquotes=false";
    public static final String WITH_PROJECT = "project=TST,issuetype=1,catchemail=catch@email.com,createusers=true,notifyusers=true,ccwatcher=false,ccassignee=false,stripquotes=false";
    public static final String SAMPLE_PARAMS = "project=TST,issuetype=1,catchemail=catch@email.com,createusers=true,unknown_param=value,notifyusers=true,ccwatcher=false,ccassignee=false,stripquotes=false,reporterusername=reporter,bulk=delete";

    @Before
    public void setUp() throws Exception
    {
        worker.addMock(MailLoggingManager.class, Mockito.mock(MailLoggingManager.class, Mockito.RETURNS_MOCKS));
        final PluginAccessor pluginAccessor = Mockito.mock(PluginAccessor.class, Mockito.RETURNS_MOCKS);
        Mockito.when(pluginAccessor.getClassLoader()).thenReturn(getClass().getClassLoader());
        worker.addMock(PluginAccessor.class, pluginAccessor);
        worker.addMock(ApplicationProperties.class, Mockito.mock(ApplicationProperties.class));
    }

    @Test
    public void testGetProjectFromService() throws Exception
    {

        ViewMailServers action = Mockito.mock(ViewMailServers.class, Mockito.CALLS_REAL_METHODS);

        Assert.assertNull(action.getRelatedProjectKey(mockService(NO_PROJECT)));
        Assert.assertEquals("TST", action.getRelatedProjectKey(mockService(WITH_PROJECT)));

    }

    @Test
    public void testParsingParams() throws Exception
    {
        ViewMailServers action = Mockito.mock(ViewMailServers.class, Mockito.CALLS_REAL_METHODS);

        final ImmutableMap<String, String> expectedParams = ImmutableMap.<String, String>builder()
                .put("catchemail", "catch@email.com")
                .put("createusers", "true")
                .put("notifyusers", "true")
                .put("ccwatcher", "false")
                .put("ccassignee", "false")
                .put("stripquotes", "false")
                .build();

        Assert.assertEquals(expectedParams, action.parseHandlerParams(mockService(NO_PROJECT)));

        Assert.assertEquals(MapBuilder.newBuilder("noval", null, "withequals", "with=equals", "lastempty", null).toHashMap(),
                action.parseHandlerParams(mockService("noval=,withequals=with=equals,lastempty=")));

        Assert.assertEquals(Collections.<String, String>emptyMap(), action.parseHandlerParams(mockService("")));
    }

    @Test
    public void testParseProjects() throws Exception
    {
        ViewMailServers action = Mockito.mock(ViewMailServers.class, Mockito.CALLS_REAL_METHODS);

        Assert.assertNull(action.getRelatedProjectKey(mockService("")));
        Assert.assertNull(action.getRelatedProjectKey(mockService(NO_PROJECT)));
        Assert.assertEquals("TST", action.getRelatedProjectKey(mockService(WITH_PROJECT)));
    }

    @Test
    public void testParseIssueType() throws Exception
    {
        ViewMailServers action = Mockito.mock(ViewMailServers.class, Mockito.CALLS_REAL_METHODS);

        Assert.assertEquals(null, action.getRelatedIssueId(mockService("")));
        Assert.assertEquals(null, action.getRelatedIssueId(mockService(NO_PROJECT)));
        Assert.assertEquals("1", action.getRelatedIssueId(mockService(WITH_PROJECT)));
        Assert.assertEquals(null, action.getRelatedIssueId(mockService("issuetype=")));
    }

    @Test
    public void testTranslateKnownParams() throws Exception
    {
        ViewMailServers action = Mockito.mock(ViewMailServers.class, Mockito.CALLS_REAL_METHODS);

        final ImmutableList<Pair<String, String>> expectedParams = ImmutableList.<Pair<String, String>>builder()
                .add(Pair.of("Strip Quotes", "false"))
                .add(Pair.of("Default Reporter", "reporter"))
                .add(Pair.of("Catch Email Address", "catch@email.com"))
                .add(Pair.of("Bulk", "delete"))
                .add(Pair.of("Forward Email", "forward@email.com"))
                .add(Pair.of("Create Users", "true"))
                .add(Pair.of("Notify Users", "true"))
                .add(Pair.of("CC Assignee", "false"))
                .add(Pair.of("CC Watchers", "false"))
                .add(Pair.of("unknown_param", "value"))
                .build();
        Assert.assertEquals(expectedParams, action.getServiceParams(mockService(SAMPLE_PARAMS)));
    }

    @Test
    public void testHandlerTypeName() throws Exception
    {
        PluginAccessor pluginAccessor = Mockito.mock(PluginAccessor.class);
        Mockito.when(pluginAccessor.getEnabledModuleDescriptorsByClass(MessageHandlerModuleDescriptor.class))
                .thenReturn(Collections.<MessageHandlerModuleDescriptor>emptyList());

        ViewMailServers action = new ViewMailServers(Mockito.mock(ServiceManager.class),
                Mockito.mock(ConstantsManager.class), Mockito.mock(ProjectManager.class), pluginAccessor) {
            @Nonnull
            @Override
            protected ComponentClassManager getComponentClassManager()
            {
                return Mockito.mock(ComponentClassManager.class);
            }
        };

        JiraServiceContainer service = mockService(WITH_PROJECT);

        Assert.assertEquals("com.atlassian.jira.plugins.mail.MockAbstractMessageHandler", action.getHandlerType(service));

        Mockito.when(service.getProperty("handler")).thenThrow(new ObjectConfigurationException("Test exception"));
        Assert.assertEquals("", action.getHandlerType(service));

        service = Mockito.mock(JiraServiceContainer.class);
        Mockito.when(service.isUsable()).thenReturn(true);
        Mockito.when(service.getProperty("handler")).thenReturn(null, "", "com.atlassian.jira.plugins.mail.handlers.CreateOrCommentHandler");

        Assert.assertEquals("", action.getHandlerType(service));
        Assert.assertEquals("", action.getHandlerType(service));
        Assert.assertEquals("com.atlassian.jira.plugins.mail.handlers.CreateOrCommentHandler", action.getHandlerType(service));
    }

    private JiraServiceContainer mockService(String handlerParams) throws ObjectConfigurationException
    {
        JiraServiceContainer service = new JiraServiceContainerImpl(
                Mockito.mock(MailFetcherService.class, Mockito.CALLS_REAL_METHODS), Long.valueOf(1));

        service.setName("Test handler name");
        MapPropertySet propertySet = new MapPropertySet();
        propertySet.setMap(ImmutableMap.builder().
                put("port", "25").
                put("usessl", "false").
                put("popserver", "10100").
                put("handler.params", handlerParams).
                put("forwardEmail", "forward@email.com").
                put("handler", MockAbstractMessageHandler.class.getName()).build());
        service.init(propertySet);

        return service;
    }
}
