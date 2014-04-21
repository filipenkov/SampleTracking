package com.atlassian.jira.user;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * Copyright 2007 Atlassian Software.
 * All rights reserved.
 */
public class TestOfbizExternalEntityStore extends LegacyJiraMockTestCase
{
    public void testCreate() throws GenericEntityException
    {
        UtilsForTestSetup.deleteAllEntities();

        List<GenericValue> externalEntities = CoreFactory.getGenericDelegator().findAll("ExternalEntity");
        assertEquals(0, externalEntities.size());

        final OfbizExternalEntityStore creator = new OfbizExternalEntityStore(CoreFactory.getGenericDelegator());
        final Long id = creator.createIfDoesNotExist("name");

        externalEntities = CoreFactory.getGenericDelegator().findAll("ExternalEntity");
        assertEquals(1, externalEntities.size());
        assertEquals("name", (externalEntities.get(0)).getString("name"));
        assertEquals(id, (externalEntities.get(0)).getLong("id"));

        // Try to create a second time and make sure we get the same id
        final Long secondId = creator.createIfDoesNotExist("name");
        assertEquals(id, secondId);

        externalEntities = CoreFactory.getGenericDelegator().findAll("ExternalEntity");
        assertEquals(1, externalEntities.size());
    }

    public void testIllegalName()
    {
        final OfbizExternalEntityStore entityStore = new OfbizExternalEntityStore(null);
        try
        {
            entityStore.createIfDoesNotExist(null);
            fail("Name cannot be null");
        }
        catch (final IllegalArgumentException e)
        {
            // this is expected
        }
    }

}
