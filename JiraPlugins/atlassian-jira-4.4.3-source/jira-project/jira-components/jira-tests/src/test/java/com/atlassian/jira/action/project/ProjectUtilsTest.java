/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.action.project;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.local.ListeningTestCase;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Map;

public class ProjectUtilsTest extends ListeningTestCase
{

    @Test
    public void testFields() throws CreateException, GenericEntityException
    {
        testFieldThrowsCreateException("key", new Object()); //key must be a string
        testFieldThrowsCreateException("id", new Object()); //id cannot be present
        testFieldThrowsCreateException("lead", new Object()); //lead must be a string
        testFieldThrowsCreateException("name", new Object()); //name must be a string
    }

    private void testFieldThrowsCreateException(String name, Object value) throws GenericEntityException
    {
        Map<String,Object> fields = getCorrectFields();
        fields.put(name, value);
        try
        {
            ProjectUtils.createProject(fields);
            fail("Creating project with field name '" + name + "' value '" + value + "' should throw CreateException");
        }
        catch (CreateException e)
        {
            assertTrue("Field name " + name + " should be in exception message " + e.getMessage(), e.getMessage().indexOf(name) != -1);
        }
    }

    private Map<String,Object> getCorrectFields()
    {
        return MapBuilder.<String,Object>newBuilder("key", "PRJ").add("name", "Project Name").add("lead", "testUser").toMutableMap();
    }
}
