/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.action.version;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.version.OfBizVersionStore;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.ListeningTestCase;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;

import java.util.Collections;
import java.util.List;

public class TestOfBizVersionStore extends ListeningTestCase
{

    GenericValue version1 = new MockGenericValue("Version", EasyMap.build("name", "version1", "id", new Long(1), "sequence", new Long(0)));
    GenericValue version2 = new MockGenericValue("Version", EasyMap.build("name", "version2", "id", new Long(2), "sequence", new Long(1)));

    @Test
    public void testGetAllVersionsReturnsCorrectDBValues()
    {
        MockOfBizDelegator delegator = new MockOfBizDelegator(EasyList.build(version1, version2), EasyList.build(version1, version2));
        OfBizVersionStore versionStore = new OfBizVersionStore(delegator);

        List versionGVs = versionStore.getAllVersions();
        assertEquals(2, versionGVs.size());
        assertEquals(version1, versionGVs.get(0));
        assertEquals(version2, versionGVs.get(1));
        delegator.verify();
    }

    @Test
    public void testGetAllVersionsReturnsEmptyListWhenNoVersions()
    {
        MockOfBizDelegator delegator = new MockOfBizDelegator(Collections.EMPTY_LIST,  Collections.EMPTY_LIST);
        OfBizVersionStore versionStore = new OfBizVersionStore(delegator);

        List versions = versionStore.getAllVersions();
        assertEquals(0, versions.size());
        delegator.verify();
    }

    @Test
    public void testCreateVersion()
    {
        GenericValue version1 = new MockGenericValue("Version", EasyMap.build("name", "version1"));
        MockOfBizDelegator delegator = new MockOfBizDelegator(Collections.EMPTY_LIST, EasyList.build(version1));
        OfBizVersionStore versionStore = new OfBizVersionStore(delegator);

        GenericValue version = versionStore.createVersion(EasyMap.build("name", "version1"));
        assertEquals("version1", version.getString("name"));
        delegator.verify();
    }

    @Test
    public void testGetVersionReturnsCorrectDatabaseVersion()
    {
        MockOfBizDelegator delegator = new MockOfBizDelegator(EasyList.build(version1, version2), EasyList.build(version1, version2));
        OfBizVersionStore versionStore = new OfBizVersionStore(delegator);

        GenericValue version = versionStore.getVersion(new Long(1));
        assertEquals(version1, version);
    }

    @Test
    public void testGetVersionReturnsNullIfNoVersionFound()
    {
        MockOfBizDelegator delegator = new MockOfBizDelegator(Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        OfBizVersionStore versionStore = new OfBizVersionStore(delegator);

        GenericValue version = versionStore.getVersion(new Long(1));
        assertEquals(null, version);
    }

    @Test
    public void testDeleteVersionRemovesVersionFromDatabase()
    {
        MockOfBizDelegator delegator = new MockOfBizDelegator(EasyList.build(version1, version2), EasyList.build(version2));
        OfBizVersionStore versionStore = new OfBizVersionStore(delegator);

        versionStore.deleteVersion(version1);
        delegator.verify();
    }
}
