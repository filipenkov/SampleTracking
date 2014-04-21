/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.link;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.exception.StoreException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.managers.DefaultIssueManager;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.DefaultPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Iterator;

public class TestLinkCollection extends AbstractUsersTestCase
{
    IssueLinkManager issueLinkManager;

    public TestLinkCollection(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        final GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
        OfBizDelegator delegator = new DefaultOfBizDelegator(genericDelegator);

        MockApplicationProperties applicationProperties = new MockApplicationProperties();
        applicationProperties.setText(APKeys.JIRA_VIEW_ISSUE_LINKS_SORT_ORDER, "");

        issueLinkManager = new DefaultIssueLinkManager(delegator, new DefaultIssueLinkCreator(null, new DefaultIssueManager(delegator, null, null, null, null, null)), new DefaultIssueLinkTypeManager(delegator), null, null, null, applicationProperties);
    }

    public void testEmptyLinkCollection()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "ABC-1"));
        LinkCollection linkColl = issueLinkManager.getLinkCollection(issue, createMockUser("blah"));
        assertTrue(linkColl.getLinkTypes().isEmpty());
    }

    public void testLinkCollection()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "ABC-1"));
        GenericValue issueSource1 = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "ABC-10"));
        GenericValue issueSource2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "ABC-11"));
        GenericValue issueDest1 = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "ABC-20"));
        GenericValue issueDest2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "ABC-21"));
        GenericValue linkType1 = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("id", new Long(1), "linkname", "foo1", "inward", "bar1", "outward", "baz1"));
        GenericValue linkType2 = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("id", new Long(2), "linkname", "foo2", "inward", "bar2", "outward", "baz2"));
        UtilsForTests.getTestEntity("IssueLink", EasyMap.build("linktype", linkType1.getLong("id"), "source", issue.getLong("id"), "destination", issueDest1.getLong("id")));
        UtilsForTests.getTestEntity("IssueLink", EasyMap.build("linktype", linkType2.getLong("id"), "source", issue.getLong("id"), "destination", issueDest2.getLong("id")));
        UtilsForTests.getTestEntity("IssueLink", EasyMap.build("linktype", linkType1.getLong("id"), "source", issueSource1.getLong("id"), "destination", issue.getLong("id")));
        UtilsForTests.getTestEntity("IssueLink", EasyMap.build("linktype", linkType2.getLong("id"), "source", issueSource2.getLong("id"), "destination", issue.getLong("id")));

        // give testRemoteUser permissions to browse the first project but not the second
        PermissionManager oldPermissionManager = (PermissionManager) ManagerFactory.addService(PermissionManager.class, new DefaultPermissionManager()
            {
                public boolean hasPermission(int permissionsId, GenericValue entity, com.atlassian.crowd.embedded.api.User u)
                {
                    return permissionsId == Permissions.BROWSE;
                }
            }).getComponentInstance();

        try
        {
            LinkCollection linkColl = issueLinkManager.getLinkCollection(issue, createMockUser("blah"));
            assertFalse(linkColl.getLinkTypes().isEmpty());
            assertEquals(2, linkColl.getLinkTypes().size());
            assertTrue(linkColl.getLinkTypes().contains(new IssueLinkTypeImpl(linkType1)));
            assertTrue(linkColl.getLinkTypes().contains(new IssueLinkTypeImpl(linkType2)));

            boolean first = true;
            for (Iterator linkTypes = linkColl.getLinkTypes().iterator(); linkTypes.hasNext();)
            {
                IssueLinkType linkType = (IssueLinkType) linkTypes.next();
                if (first)
                {
                    collectionHas(linkColl.getInwardIssues(linkType.getString("linkname")), issueSource1);
                    collectionHas(linkColl.getOutwardIssues(linkType.getString("linkname")), issueDest1);
                    first = false;
                }
                else
                {
                    collectionHas(linkColl.getInwardIssues(linkType.getString("linkname")), issueSource2);
                    collectionHas(linkColl.getOutwardIssues(linkType.getString("linkname")), issueDest2);
                }
            }
        }
        finally
        {
            ManagerFactory.addService(PermissionManager.class, oldPermissionManager);
        }
    }

    public void collectionHas(Collection issues, GenericValue issueGV)
    {
        assertEquals(1, issues.size());
        Issue issue = (Issue) issues.iterator().next();
        assertTrue(issue.getGenericValue().equals(issueGV));
    }

    public void collectionHasNot(Collection issues, GenericValue issueGV)
    {
        assertEquals(1, issues.size());
        Issue issue = (Issue) issues.iterator().next();
        assertFalse(issue.getGenericValue().equals(issueGV));
    }

    /**
     * User's should not be able to see link to issues they have no permissions to view
     */
    public void testGetOutwardIssues()
            throws StoreException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        GenericValue project1 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1)));
        GenericValue project2 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(2)));
        GenericValue issue1 = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "ABC-1", "project", project1.getLong("id")));
        GenericValue issue2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "ABC-20", "project", project1.getLong("id")));
        GenericValue issue3 = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "XYZ-21", "project", project2.getLong("id")));
        GenericValue linkType = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("id", new Long(1), "linkname", "foo1", "inward", "bar1", "outward", "baz1"));
        UtilsForTests.getTestEntity("IssueLink", EasyMap.build("linktype", linkType.getLong("id"), "source", issue1.getLong("id"), "destination", issue2.getLong("id")));
        UtilsForTests.getTestEntity("IssueLink", EasyMap.build("linktype", linkType.getLong("id"), "source", issue1.getLong("id"), "destination", issue3.getLong("id")));

        // give testRemoteUser permissions to browse the first project but not the second
        final User testUser = createMockUser("blah");
        PermissionManager oldPermissionManager = (PermissionManager) ManagerFactory.addService(PermissionManager.class, new DefaultPermissionManager()
            {
                public boolean hasPermission(int permissionsId, GenericValue entity, com.atlassian.crowd.embedded.api.User u)
                {
                    return permissionsId == Permissions.BROWSE && entity.getLong("project").longValue() == 1 && u.equals(testUser);
                }
            }).getComponentInstance();

        try
        {
            LinkCollection lc = issueLinkManager.getLinkCollection(issue1, testUser);
            assertEquals(1, lc.getOutwardIssues(linkType.getString("linkname")).size());
            collectionHas(lc.getOutwardIssues(linkType.getString("linkname")), issue2);
            collectionHasNot(lc.getOutwardIssues(linkType.getString("linkname")), issue3);
        }
        finally
        {
            ManagerFactory.addService(PermissionManager.class, oldPermissionManager);
        }
    }

    /**
     * same should apply to inward links
     */
    public void testGetInwardIssues()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        GenericValue project1 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1)));
        GenericValue project2 = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(2)));
        GenericValue issue1 = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "ABC-1", "project", project1.getLong("id")));
        GenericValue issue2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "ABC-20", "project", project1.getLong("id")));
        GenericValue issue3 = UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "XYZ-21", "project", project2.getLong("id")));
        GenericValue linkType = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("id", new Long(1), "linkname", "foo1", "inward", "bar1", "outward", "baz1"));
        UtilsForTests.getTestEntity("IssueLink", EasyMap.build("linktype", linkType.getLong("id"), "source", issue2.getLong("id"), "destination", issue1.getLong("id")));
        UtilsForTests.getTestEntity("IssueLink", EasyMap.build("linktype", linkType.getLong("id"), "source", issue3.getLong("id"), "destination", issue1.getLong("id")));

        // give testRemoteUser permissions to browse the first project but not the second
        final User testUser = createMockUser("blah");
        PermissionManager oldPermissionManager = (PermissionManager) ManagerFactory.addService(PermissionManager.class, new DefaultPermissionManager()
            {
                public boolean hasPermission(int permissionsId, GenericValue entity, com.atlassian.crowd.embedded.api.User u)
                {
                    return permissionsId == Permissions.BROWSE && entity.getLong("project").longValue() == 1 && u.equals(testUser);
                }
            }).getComponentInstance();

        try
        {
            LinkCollection lc = issueLinkManager.getLinkCollection(issue1, testUser);
            assertEquals(1, lc.getInwardIssues(linkType.getString("linkname")).size());
            collectionHas(lc.getInwardIssues(linkType.getString("linkname")), issue2);
            collectionHasNot(lc.getInwardIssues(linkType.getString("linkname")), issue3);
        }
        finally
        {
            ManagerFactory.addService(PermissionManager.class, oldPermissionManager);
        }
    }
}
