/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.template;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import com.atlassian.jira.scheme.SchemeEntity;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class TestDefaultTemplateManager extends LegacyJiraMockTestCase
{
    private DefaultTemplateManager templateManager;
    private Mock mockAppProps;
    private Mock mockEventTypeManager;
    static {
        UtilsForTestSetup.loadDatabaseDriver();
    }

    public void setUp() throws Exception
    {
        super.setUp();
        mockAppProps = new Mock(ApplicationProperties.class);
        mockEventTypeManager = new Mock(EventTypeManager.class);
        templateManager = new DefaultTemplateManager((ApplicationProperties) mockAppProps.proxy(), (EventTypeManager) mockEventTypeManager.proxy());
    }

    protected void tearDown() throws Exception
    {
        UtilsForTestSetup.deleteAllEntities();
    }

    public void testGetTemplateTypes()
    {
        Map templateMap;
        Collection templates;

        templateMap = templateManager.getTemplatesMap(Template.TEMPLATE_TYPE_ISSUEEVENT);
        templates = templateMap.values();
        // 16 issueevent templates
        assertEquals(16, templates.size());

        for (Iterator iterator = templates.iterator(); iterator.hasNext();)
        {
            Template template = (Template) iterator.next();
            assertEquals(Template.TEMPLATE_TYPE_ISSUEEVENT, template.getType());
        }

        // Filter Sub Template
        templateMap = templateManager.getTemplatesMap(Template.TEMPLATE_TYPE_FILTERSUB);
        templates = templateMap.values();
        // 1 filter subscription template
        assertEquals(1, templates.size());

        for (Iterator iterator = templates.iterator(); iterator.hasNext();)
        {
            Template template = (Template) iterator.next();
            assertEquals(Template.TEMPLATE_TYPE_FILTERSUB, template.getType());
        }
    }

    // Check that correct template is returned by specified id
    public void testGetTemplatebyId()
    {
        // Retrieve the 'Issue Created' template
        Long issueCreatedTemplateId = new Long(1);
        Template template = templateManager.getTemplate(issueCreatedTemplateId);

        assertEquals(issueCreatedTemplateId, template.getId());
        assertEquals("Issue Created", template.getName());
        assertEquals(Template.TEMPLATE_TYPE_ISSUEEVENT, template.getType());
    }

    // Check that correct default template is returned
    public void testGetDefaultTemplate()
    {
        // Add event
        EventTypeManager eventTypeManager = ComponentAccessor.getEventTypeManager();
        Map eventTypeParamasMap = EasyMap.build("id", new Long(1), "name", "Issue Created", "description", "This is the issue created event type descrition", "type", EventType.JIRA_SYSTEM_EVENT_TYPE);
        GenericValue issueEventTypeGV = UtilsForTests.getTestEntity(EventType.EVENT_TYPE, eventTypeParamasMap);
        EventType eventType = new EventType(issueEventTypeGV);
        eventTypeManager.addEventType(eventType);

        templateManager = new DefaultTemplateManager((ApplicationProperties) mockAppProps.proxy(), eventTypeManager);

        Template template = templateManager.getDefaultTemplate(eventType);
        Long issueCreatedTemplateId = new Long(1);

        assertEquals(issueCreatedTemplateId, template.getId());
        assertEquals("Issue Created", template.getName());
        assertEquals(Template.TEMPLATE_TYPE_ISSUEEVENT, template.getType());
    }

    public void testGetTemplateByNotificationEntity() throws GenericEntityException
    {
        // Add event
        EventTypeManager eventTypeManager = ComponentAccessor.getEventTypeManager();
        Map eventTypeParamasMap = EasyMap.build("id", new Long(1), "name", "Issue Created", "description", "This is the issue created event type descrition", "type", EventType.JIRA_SYSTEM_EVENT_TYPE);
        GenericValue issueEventTypeGV = UtilsForTests.getTestEntity(EventType.EVENT_TYPE, eventTypeParamasMap);
        EventType eventType = new EventType(issueEventTypeGV);
        eventTypeManager.addEventType(eventType);

        mockEventTypeManager.setStrict(true);
        mockEventTypeManager.expectAndReturn("getEventType", P.args(P.eq(new Long(1))), eventType);

        // The scheme entity is not assocaited with a template - should default back to the event it is associated with.
        SchemeEntity notificationSchemeEntity = new SchemeEntity(new Long(10000), "Current_Assignee", null, EventType.ISSUE_CREATED_ID, null, null);

        Template template  = templateManager.getTemplate(notificationSchemeEntity);
        Long issueCreatedTemplateId = new Long(1);

        assertEquals(issueCreatedTemplateId, template.getId());
        assertEquals("Issue Created", template.getName());
        assertEquals(Template.TEMPLATE_TYPE_ISSUEEVENT, template.getType());
        mockEventTypeManager.verify();
    }

    public void testGetTemplateByNotificationEntityWithNonDefaultTemplate() throws GenericEntityException
    {
        // Add event
        EventTypeManager eventTypeManager = ComponentAccessor.getEventTypeManager();
        Map eventTypeParamasMap = EasyMap.build("id", new Long(1), "name", "Issue Created", "description", "This is the issue created event type descrition", "type", EventType.JIRA_SYSTEM_EVENT_TYPE);
        GenericValue issueEventTypeGV = UtilsForTests.getTestEntity(EventType.EVENT_TYPE, eventTypeParamasMap);
        EventType eventType = new EventType(issueEventTypeGV);
        eventTypeManager.addEventType(eventType);

        // The scheme entity is assocaited with a template - should use this template rather than default assocaited with event that has been fired.
        // I.e the 'Issue Deleted' event has been fired - but the 'Issue Created' template will be used.
        SchemeEntity notificationSchemeEntity = new SchemeEntity(new Long(10000), "Current_Assignee", null, EventType.ISSUE_DELETED_ID, EventType.ISSUE_CREATED_ID, null);

        Template template  = templateManager.getTemplate(notificationSchemeEntity);
        Long issueCreatedTemplateId = new Long(1);

        assertEquals(issueCreatedTemplateId, template.getId());
        assertEquals("Issue Created", template.getName());
        assertEquals(Template.TEMPLATE_TYPE_ISSUEEVENT, template.getType());
    }

}
