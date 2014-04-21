/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project.version;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.CollectionReorderer;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.easymock.MockControl;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestDefaultVersionManagerLegacy extends LegacyJiraMockTestCase
{
    private static final Long PROJECT_ONE_ID = new Long(101);

    Project project1Obj;
    GenericValue project1;

    Version version1;
    Version version2;
    Version version3;
    Version version4;
    Version version5;
    Version version6;
    Version version7;
    Version version8;

    Version versionOne;

    GenericValue issue1;
    GenericValue issue2;
    GenericValue issue3;
    GenericValue issue4;
    GenericValue issue5;
    GenericValue issue6;

    MockIssue issueObj1;
    MockIssue issueObj2;

    Mock projectManager;
    Mock delegatorMock;
    Mock issueManagerMock;
    Mock associationManager;
    Mock issueIndexManagerMock;
    Mock outlookDateManager;
    ApplicationProperties applicationProperties;

    Collection swapIssues;
    Collection affectedIssues;
    Collection affectedIssueObjects;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        projectManager = new Mock(ProjectManager.class);
        delegatorMock = new Mock(OfBizDelegator.class);
        issueManagerMock = new Mock(IssueManager.class);
        associationManager = new Mock(AssociationManager.class);
        issueIndexManagerMock = new Mock(IssueIndexManager.class);
        outlookDateManager = new Mock(OutlookDateManager.class);

        project1 = new MockGenericValue("Project", EasyMap.build("key", "ABC", "name", "Project 1", "id", PROJECT_ONE_ID));
        project1Obj = new MockProject(PROJECT_ONE_ID, "ABC", "Project 1");

        version1 = new MyVersion(null, new MockGenericValue("Version", EasyMap.build("name", "Version 1", "id", new Long(1001), "sequence", new Long(
            1), "project", PROJECT_ONE_ID, "releasedate", null, "description", null)));
        version2 = new MyVersion(null, new MockGenericValue("Version", EasyMap.build("name", "Version 2", "id", new Long(1002), "sequence", new Long(
            2), "project", PROJECT_ONE_ID, "releasedate", new Timestamp(1), "description", "The description")));
        version3 = new MyVersion(null, new MockGenericValue("Version", EasyMap.build("name", "Version 3", "id", new Long(1003), "sequence", new Long(
            3), "project", PROJECT_ONE_ID, "archived", null, "released", null)));
        version4 = new MyVersion(null, new MockGenericValue("Version", EasyMap.build("name", "Version 4", "id", new Long(1004), "sequence", new Long(
            4), "project", PROJECT_ONE_ID, "archived", "true", "released", "true")));
        version5 = new VersionImpl(null, new MockGenericValue("Version", EasyMap.build("name", "Version 5", "id", new Long(1005), "sequence",
            new Long(5), "project", PROJECT_ONE_ID, "archived", "true", "released", "true")));
        version6 = new VersionImpl(null, new MockGenericValue("Version", EasyMap.build("name", "Version 6", "id", new Long(1006), "sequence",
            new Long(6), "project", PROJECT_ONE_ID, "archived", "true", "released", "true")));
        version7 = new VersionImpl(null, new MockGenericValue("Version", EasyMap.build("name", "Version 7", "id", new Long(1007), "sequence",
            new Long(7), "project", PROJECT_ONE_ID, "archived", null, "released", "true")));
        version8 = new MyVersion(null, new MockGenericValue("Version", EasyMap.build("name", "Version 8", "id", new Long(1008), "sequence", new Long(
            8), "project", PROJECT_ONE_ID, "archived", "true", "released", null)));

        versionOne = new MyVersion(null, new MockGenericValue("Version", EasyMap.build("name", "Version 1", "id", new Long(1005), "sequence",
            new Long(1), "project", PROJECT_ONE_ID, "archived", Boolean.FALSE, "released", Boolean.FALSE)));

        issue1 = new MockGenericValue("Issue", EasyMap.build("id", new Long(1), "fixfor", new Long(1001), "key", "issue1"));
        issue2 = new MockGenericValue("Issue", EasyMap.build("id", new Long(2), "fixfor", new Long(1001), "key", "issue2"));
        issue3 = new MockGenericValue("Issue", EasyMap.build("id", new Long(3), "fixfor", new Long(1001), "key", "issue3"));
        issue4 = new MockGenericValue("Issue", EasyMap.build("id", new Long(3), "fixfor", new Long(1001), "key", "issue4"));
        issue5 = new MockGenericValue("Issue", EasyMap.build("id", new Long(3), "fixfor", new Long(1001), "key", "issue5"));
        issue6 = new MockGenericValue("Issue", EasyMap.build("id", new Long(3), "fixfor", new Long(1001), "key", "issue6"));

        issueObj1 = new IdentityEqualsMockIssue(new Long(1));
        issueObj1.setAffectedVersions(Collections.EMPTY_LIST);
        issueObj1.setFixVersions(EasyList.build(version1));
        issueObj2 = new IdentityEqualsMockIssue(new Long(2));
        issueObj2.setAffectedVersions(Collections.EMPTY_LIST);
        issueObj2.setFixVersions(EasyList.build(version1));

        swapIssues = new ArrayList();
        swapIssues.add(issue2);
        swapIssues.add(issue1);

        affectedIssues = new ArrayList();
        affectedIssues.add(issue1);
        affectedIssues.add(issue2);

        affectedIssueObjects = new ArrayList();
        affectedIssueObjects.add(issueObj1);
        affectedIssueObjects.add(issueObj2);

        CoreFactory.getAssociationManager().createAssociation(issue1, version1.getGenericValue(), IssueRelationConstants.VERSION);
        CoreFactory.getAssociationManager().createAssociation(issue2, version2.getGenericValue(), IssueRelationConstants.VERSION);
        CoreFactory.getAssociationManager().createAssociation(issue3, version3.getGenericValue(), IssueRelationConstants.VERSION);
        CoreFactory.getAssociationManager().createAssociation(issue4, version4.getGenericValue(), IssueRelationConstants.VERSION);
        CoreFactory.getAssociationManager().createAssociation(issue5, version5.getGenericValue(), IssueRelationConstants.VERSION);
        CoreFactory.getAssociationManager().createAssociation(issue6, version6.getGenericValue(), IssueRelationConstants.VERSION);
        CoreFactory.getAssociationManager().createAssociation(issue1, version6.getGenericValue(), IssueRelationConstants.FIX_VERSION);
        CoreFactory.getAssociationManager().createAssociation(issue2, version5.getGenericValue(), IssueRelationConstants.FIX_VERSION);
        CoreFactory.getAssociationManager().createAssociation(issue3, version4.getGenericValue(), IssueRelationConstants.FIX_VERSION);
        CoreFactory.getAssociationManager().createAssociation(issue4, version3.getGenericValue(), IssueRelationConstants.FIX_VERSION);
        CoreFactory.getAssociationManager().createAssociation(issue5, version2.getGenericValue(), IssueRelationConstants.FIX_VERSION);
        CoreFactory.getAssociationManager().createAssociation(issue6, version1.getGenericValue(), IssueRelationConstants.FIX_VERSION);
    }

    protected void tearDown() throws Exception
    {

        projectManager = null;
        delegatorMock = null;
        issueManagerMock = null;
        associationManager = null;
        issueIndexManagerMock = null;
        outlookDateManager = null;
        applicationProperties = null;
        swapIssues = null;
        affectedIssues = null;

        super.tearDown();
    }

    // ---- Create Version Tests ----
    public void testCreateVersionInvalidParams() throws GenericEntityException
    {
        final DefaultVersionManager versionManager = new DefaultVersionManager(null, null, null, null, null, null);

        try
        {
            versionManager.createVersion(null, null, null, (Long) null, null);
            fail("Should have thrown create exception");
        }
        catch (final CreateException e)
        {
            assertTrue(e.getMessage() != null);
        }
    }

    public void testCreateFirstVersion() throws GenericEntityException, CreateException
    {
        _testCreateVersion(Collections.EMPTY_LIST, new Long(1));
    }

    public void testCreateSecondVersionHasCorrectSequenceId() throws GenericEntityException, CreateException
    {
        _testCreateVersion(EasyList.build(version1.getGenericValue()), new Long(2));
    }

    private void _testCreateVersion(final Collection existingVersionGVs, final Long expectedSequenceId) throws GenericEntityException, CreateException
    {
        delegatorMock.expectAndReturn("findByAnd", P.args(P.eq("Version"), P.eq(EasyMap.build("project", PROJECT_ONE_ID)), P.eq(EasyList.build("sequence"))), existingVersionGVs);
        final Map expectedList = EasyMap.build("name", "Version 1", "project", PROJECT_ONE_ID, "sequence", expectedSequenceId, "description", null);
        delegatorMock.expectAndReturn("createValue", P.args(P.eq(OfBizDelegator.VERSION), P.eq(expectedList)), version2.getGenericValue());

        delegatorMock.setStrict(true);
        delegatorMock.setStrict(true);

        final DefaultVersionManager versionManager = getVersionManager();
        versionManager.createVersion("Version 1", null, null, project1.getLong("id"), null);

        delegatorMock.verify();
        delegatorMock.verify();
    }

    public void testCreateVersionInFirstSequencePosition() throws GenericEntityException, CreateException
    {
        delegatorMock.expectAndReturn("findByAnd", P.args(P.eq("Version"), P.eq(EasyMap.build("project", PROJECT_ONE_ID)), P.eq(EasyList.build("sequence"))),
            EasyList.build(version1.getGenericValue()));
        final Map expectedList = EasyMap.build("name", "Version 2", "project", PROJECT_ONE_ID, "sequence", new Long(1), "description", null);
        delegatorMock.expectAndReturn("createValue", P.args(P.eq(OfBizDelegator.VERSION), P.eq(expectedList)), version2.getGenericValue());
        delegatorMock.expectVoid("store", P.args(P.eq(version1.getGenericValue())));
        issueManagerMock.expectAndReturn("getIssuesByEntity", P.ANY_ARGS, EasyList.build(version1.getGenericValue()));

        delegatorMock.setStrict(true);
        delegatorMock.setStrict(true);

        final DefaultVersionManager versionManager = getVersionManager();
        versionManager.createVersion("Version 2", null, null, project1.getLong("id"), new Long(-1));
        assertEquals(new Long(2), version1.getSequence());

        delegatorMock.verify();
        delegatorMock.verify();
    }

    public void testCreateVersionWithReleaseDateAndDescription() throws GenericEntityException, CreateException
    {
        final Date releaseDate = new Timestamp(1);
        delegatorMock.expectAndReturn("findByAnd", P.args(P.eq("Version"), P.eq(EasyMap.build("project", PROJECT_ONE_ID)), P.eq(EasyList.build("sequence"))),
            EasyList.build(version1.getGenericValue()));
        final Map expectedList = EasyMap.build("name", "Version 2", "project", PROJECT_ONE_ID, "sequence", version2.getSequence(), "releasedate",
            releaseDate, "description", "The Description");
        delegatorMock.expectAndReturn("createValue", P.args(P.eq(OfBizDelegator.VERSION), P.eq(expectedList)), version2.getGenericValue());

        delegatorMock.setStrict(true);

        final DefaultVersionManager versionManager = getVersionManager();
        versionManager.createVersion("Version 2", releaseDate, "The Description", project1.getLong("id"), null);

        delegatorMock.verify();
    }

    // ---- Version Scheduling Tests ----
    public void testStoreReorderedVersionListNoAffectedIssues() throws GenericEntityException
    {
        _testStoreReorderedVersionList(Collections.EMPTY_LIST);
    }

    public void testStoreReorderedVersionListWithAffectedIssues() throws GenericEntityException
    {

        _testStoreReorderedVersionList(swapIssues);
    }

    public void _testStoreReorderedVersionList(final Collection affectedIssues) throws GenericEntityException
    {
        //versions 2+3 are swapped, so we expect that the issues for these versions are flushed
        final List reorderedVersions = EasyList.build(version1, version3, version2, version4);

        final DefaultVersionManager defaultVersionManager = getVersionManager();
        defaultVersionManager.storeReorderedVersionList(reorderedVersions);

        assertEquals(new Long(2), version3.getSequence());
        assertEquals(new Long(3), version2.getSequence());

        issueManagerMock.verify();
    }

    private void _testDeleteValidator(Version version)
    {
        final DefaultVersionManager defaultVersionManager = getVersionManager();
        try
        {
            defaultVersionManager.deleteVersion(version);
            fail("Exception should have been thrown.");
        }
        catch (final IllegalArgumentException iae)
        {
            assertNotNull(iae.getMessage());
        }
    }

    public void testDeleteVersion() throws GenericEntityException
    {
        final MockControl mockVersionStoreControl = MockControl.createControl(VersionStore.class);
        final VersionStore mockVersionStore = (VersionStore) mockVersionStoreControl.getMock();

        mockVersionStore.deleteVersion(version1.getGenericValue());
        mockVersionStoreControl.replay();

        final AtomicBoolean reorderCalled = new AtomicBoolean(false);
        final DefaultVersionManager defaultVersionManager = new DefaultVersionManager(null, null, null, null, null, mockVersionStore)
        {
            void reorderVersionsInProject(final Version version)
            {
                reorderCalled.set(version.getId().equals(version1.getId()));
            }
        };

        defaultVersionManager.deleteVersion(version1);
        assertTrue(reorderCalled.get());
        mockVersionStoreControl.verify();
    }

    // ---- Version Edit Tests ----
    public void testEditVersionNameWithInvalidName() throws GenericEntityException
    {
        final DefaultVersionManager defaultVersionManager = getVersionManager();

        try
        {
            defaultVersionManager.editVersionDetails(null, null, null, null);
            fail("Exception should have been thrown.");
        }
        catch (final IllegalArgumentException iae)
        {
            assertNotNull(iae.getMessage());
        }
    }

    public void testEditVersionNameWithDuplicateName() throws GenericEntityException
    {
        final DefaultVersionManager defaultVersionManager = getVersionManager();

        try
        {
            defaultVersionManager.editVersionDetails(versionOne, "Version 1", null, project1);
        }
        catch (final IllegalArgumentException iae)
        {
            assertNotNull(iae.getMessage());
        }

        delegatorMock.verify();
    }

    public void testEditVersionName() throws GenericEntityException
    {
        final DefaultVersionManager defaultVersionManager = getVersionManager();

        try
        {
            defaultVersionManager.editVersionDetails(version1, "Version 2", null, project1);
        }
        catch (final IllegalArgumentException iae)
        {
            assertNotNull(iae.getMessage());
        }

        delegatorMock.verify();
        issueManagerMock.verify();
    }

    public void testMoveVersionAfterToFirstAndLast()
    {
        final DefaultVersionManager defaultVersionManager = getVersionManager();

        try
        {
            //assert the intial sequence order (1, 2, 3 and 4)
            assertEquals(new Long(1), version1.getSequence());
            assertEquals(new Long(2), version2.getSequence());
            assertEquals(new Long(3), version3.getSequence());
            assertEquals(new Long(4), version4.getSequence());

            List currentVersions = EasyList.build(version1.getGenericValue(), version2.getGenericValue(), version3.getGenericValue(),
                version4.getGenericValue());

            //move pos 1 to last using null (modifies all versions as it needs to shift everything forward by 1)
            // v1 ---+         v2
            // v2    |    =    v3
            // v3    |         v4
            // v4    |
            //    <--+         v1
            List expectedVersions = EasyList.build(version2.getGenericValue(), version3.getGenericValue(), version4.getGenericValue(),
                version1.getGenericValue());
            Constraint[] storeConstraints = P.args(P.or(P.or(P.eq(version2.getGenericValue()), P.eq(version3.getGenericValue())), P.or(
                P.eq(version4.getGenericValue()), P.eq(version1.getGenericValue()))));
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version1, null, expectedVersions);

            //move pos 2 to last using null (modifies 3, 4 and 1)
            // v2              v2
            // v3 ---+    =    v4
            // v4    |         v1
            // v1    |
            //    <--+         v3
            expectedVersions = EasyList.build(version2.getGenericValue(), version4.getGenericValue(), version1.getGenericValue(),
                version3.getGenericValue());
            storeConstraints = P.args(P.or(P.eq(version3.getGenericValue()), P.or(P.eq(version4.getGenericValue()), P.eq(version1.getGenericValue()))));
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version3, null, expectedVersions);

            //move pos 3 to last using null (modifies 1 and 3)
            // v2              v2
            // v4         =    v4
            // v1 ---+         v3
            // v3    |
            //    <--+         v1
            expectedVersions = EasyList.build(version2.getGenericValue(), version4.getGenericValue(), version3.getGenericValue(),
                version1.getGenericValue());
            storeConstraints = P.args(P.or(P.eq(version3.getGenericValue()), P.eq(version1.getGenericValue())));
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version1, null, expectedVersions);

            //move pos 4 to last using null (should do nothing)
            // v2              v2
            // v4         =    v4
            // v3              v3
            // v1 ---+         v1
            //    <--+
            expectedVersions = EasyList.build(version2.getGenericValue(), version4.getGenericValue(), version3.getGenericValue(),
                version1.getGenericValue());
            storeConstraints = null;
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version1, null, expectedVersions);

            //move pos 4 to first using -1 (modifies all versions as it needs to shift everything back by 1)
            //    <--+         v1
            // v2    |
            // v4    |    =    v2
            // v3    |         v4
            // v1 ---+         v3
            expectedVersions = EasyList.build(version1.getGenericValue(), version2.getGenericValue(), version4.getGenericValue(),
                version3.getGenericValue());
            storeConstraints = P.args(P.or(P.or(P.eq(version2.getGenericValue()), P.eq(version3.getGenericValue())), P.or(
                P.eq(version4.getGenericValue()), P.eq(version1.getGenericValue()))));
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version1, new Long(-1),
                expectedVersions);

            //move pos 3 to first using -1 (modifies 1, 2 and 4)
            //    <--+         v4
            // v1    |
            // v2    |    =    v1
            // v4 ---+         v2
            // v3              v3
            expectedVersions = EasyList.build(version4.getGenericValue(), version1.getGenericValue(), version2.getGenericValue(),
                version3.getGenericValue());
            storeConstraints = P.args(P.or(P.eq(version2.getGenericValue()), P.or(P.eq(version4.getGenericValue()), P.eq(version1.getGenericValue()))));
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version4, new Long(-1),
                expectedVersions);

            //move pos 2 to first using -1 (modifies 1 and 4)
            //    <--+         v1
            // v4    |
            // v1 ---+         v4
            // v2         =    v2
            // v3              v3
            expectedVersions = EasyList.build(version1.getGenericValue(), version4.getGenericValue(), version2.getGenericValue(),
                version3.getGenericValue());
            storeConstraints = P.args(P.or(P.eq(version4.getGenericValue()), P.eq(version1.getGenericValue())));
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version1, new Long(-1),
                expectedVersions);

            //move pos 1 to first using -1 (should do nothing)
            //    <--+         v1
            // v1 ---+
            // v4              v4
            // v2         =    v2
            // v3              v3
            expectedVersions = EasyList.build(version1.getGenericValue(), version4.getGenericValue(), version2.getGenericValue(),
                version3.getGenericValue());
            storeConstraints = null;
            assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version1, new Long(-1), expectedVersions);
        }
        catch (final IllegalArgumentException iae)
        {
            assertNotNull(iae.getMessage());
        }

        delegatorMock.verify();
        issueManagerMock.verify();
        projectManager.verify();
    }

    public void testMoveVersionAfterOnePosition()
    {
        final DefaultVersionManager defaultVersionManager = getVersionManager();

        try
        {
            //assert the intial sequence order (1, 2, 3 and 4)
            assertEquals(new Long(1), version1.getSequence());
            assertEquals(new Long(2), version2.getSequence());
            assertEquals(new Long(3), version3.getSequence());
            assertEquals(new Long(4), version4.getSequence());

            List currentVersions = EasyList.build(version1.getGenericValue(), version2.getGenericValue(), version3.getGenericValue(),
                version4.getGenericValue());

            //move v1 to second
            // v1 ---+         v2
            // v2 <--+    =    v1
            // v3              v3
            // v4              v4
            expectFindByPrimaryVersionKey(version2);
            List expectedVersions = EasyList.build(version2.getGenericValue(), version1.getGenericValue(), version3.getGenericValue(),
                version4.getGenericValue());
            Constraint[] storeConstraints = P.args((P.or(P.eq(version2.getGenericValue()), P.eq(version1.getGenericValue()))));
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version1, version2.getId(),
                expectedVersions);

            //move v1 to 3rd position
            // v2              v2
            // v1 ---+    =    v3
            // v3 <--+         v1
            // v4              v4
            expectFindByPrimaryVersionKey(version3);
            expectedVersions = EasyList.build(version2.getGenericValue(), version3.getGenericValue(), version1.getGenericValue(),
                version4.getGenericValue());
            storeConstraints = P.args((P.or(P.eq(version3.getGenericValue()), P.eq(version1.getGenericValue()))));
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version1, version3.getId(),
                expectedVersions);

            //move v1 to 4th position
            // v2              v2
            // v3              v3
            // v1 ---+    =    v4
            // v4 <--+         v1
            expectFindByPrimaryVersionKey(version4);
            expectedVersions = EasyList.build(version2.getGenericValue(), version3.getGenericValue(), version4.getGenericValue(),
                version1.getGenericValue());
            storeConstraints = P.args((P.or(P.eq(version4.getGenericValue()), P.eq(version1.getGenericValue()))));
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version1, version4.getId(),
                expectedVersions);

            //move v1 to 3rd position
            // v2              v2
            // v3              v3
            // v4 <--+    =    v4
            // v1 ---+         v1
            expectFindByPrimaryVersionKey(version4);
            expectedVersions = EasyList.build(version2.getGenericValue(), version3.getGenericValue(), version4.getGenericValue(),
                version1.getGenericValue());
            storeConstraints = P.args(P.or(P.eq(version4.getGenericValue()), P.eq(version1.getGenericValue())));
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version1, version4.getId(),
                expectedVersions);

            //move v1 to 2nd position
            // v2              v2
            // v3 <--+    =    v3
            // v4 ---+         v4
            // v1              v1
            expectFindByPrimaryVersionKey(version3);
            expectedVersions = EasyList.build(version2.getGenericValue(), version3.getGenericValue(), version4.getGenericValue(),
                version1.getGenericValue());
            storeConstraints = null;
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version4, version3.getId(),
                expectedVersions);

            //move v1 to 2nd position
            // v2 <--+    =    v2
            // v3 ---+         v3
            // v4              v4
            // v1              v1
            expectFindByPrimaryVersionKey(version2);
            expectedVersions = EasyList.build(version2.getGenericValue(), version3.getGenericValue(), version4.getGenericValue(),
                version1.getGenericValue());
            storeConstraints = null;
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version3, version2.getId(),
                expectedVersions);

            // v2 ---+         v3
            // v3    |    =    v4
            // v4    |         v1
            // v1 <--+         v2
            expectFindByPrimaryVersionKey(version1);
            expectedVersions = EasyList.build(version3.getGenericValue(), version4.getGenericValue(), version1.getGenericValue(),
                version2.getGenericValue());
            storeConstraints = P.args(P.or(P.or(P.eq(version2.getGenericValue()), P.eq(version3.getGenericValue())), P.or(
                P.eq(version4.getGenericValue()), P.eq(version1.getGenericValue()))));
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version2, version1.getId(),
                expectedVersions);

            // v3              v3
            // v4 ---+    =    v1
            // v1    |         v2
            // v2 <--+         v4
            expectFindByPrimaryVersionKey(version2);
            expectedVersions = EasyList.build(version3.getGenericValue(), version1.getGenericValue(), version2.getGenericValue(),
                version4.getGenericValue());
            storeConstraints = P.args(P.or(P.or(P.eq(version2.getGenericValue()), P.eq(version3.getGenericValue())), P.or(
                P.eq(version4.getGenericValue()), P.eq(version1.getGenericValue()))));
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version4, version2.getId(),
                expectedVersions);

            // v3              v3
            // v1 <--+    =    v1
            // v2    |         v4
            // v4 ---+         v2
            expectFindByPrimaryVersionKey(version1);
            expectedVersions = EasyList.build(version3.getGenericValue(), version1.getGenericValue(), version4.getGenericValue(),
                version2.getGenericValue());
            storeConstraints = P.args(P.or(P.or(P.eq(version2.getGenericValue()), P.eq(version4.getGenericValue())), P.eq(version1.getGenericValue())));
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version4, version1.getId(),
                expectedVersions);

            // v3 ---+    =    v1
            // v1    |         v4
            // v4 <--+         v3
            // v2              v2
            expectFindByPrimaryVersionKey(version4);
            expectedVersions = EasyList.build(version1.getGenericValue(), version4.getGenericValue(), version3.getGenericValue(),
                version2.getGenericValue());
            storeConstraints = P.args(P.or(P.eq(version3.getGenericValue()), P.or(P.eq(version4.getGenericValue()), P.eq(version1.getGenericValue()))));
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version3, version4.getId(),
                expectedVersions);

            // v1 <--+    =    v1
            // v4    |         v3
            // v3 ---+         v4
            // v2              v2
            expectFindByPrimaryVersionKey(version1);
            expectedVersions = EasyList.build(version1.getGenericValue(), version3.getGenericValue(), version4.getGenericValue(),
                version2.getGenericValue());
            storeConstraints = P.args(P.or(P.or(P.eq(version4.getGenericValue()), P.eq(version3.getGenericValue())), P.eq(version1.getGenericValue())));
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version3, version1.getId(),
                expectedVersions);

            // v1 <--+         v1
            // v3    |    =    v2
            // v4    |         v3
            // v2 ---+         v4
            expectFindByPrimaryVersionKey(version1);
            expectedVersions = EasyList.build(version1.getGenericValue(), version2.getGenericValue(), version3.getGenericValue(),
                version4.getGenericValue());
            storeConstraints = P.args(P.or(P.or(P.eq(version2.getGenericValue()), P.eq(version3.getGenericValue())), P.or(
                P.eq(version4.getGenericValue()), P.eq(version1.getGenericValue()))));
            currentVersions = assertMoveVersionAfter(defaultVersionManager, storeConstraints, currentVersions, version2, version1.getId(),
                expectedVersions);
        }
        catch (final IllegalArgumentException iae)
        {
            assertNotNull(iae.getMessage());
        }

        delegatorMock.verify();
        issueManagerMock.verify();
    }

    // ---- Release Version Tests ----
    public void testReleaseVersion() throws GenericEntityException
    {
        final DefaultVersionManager defaultVersionManager = getVersionManager();

        defaultVersionManager.releaseVersions(EasyList.build(version1), true);

        assertTrue(version1.isReleased());

        issueManagerMock.verify();
    }

    // ---- Version Archive Tests ----
    public void testArchiveVersion() throws Exception
    {
        delegatorMock.expectAndReturn("findById", P.args(P.eq("Version"), P.IS_NOT_NULL), version1.getGenericValue());
        delegatorMock.expectVoid("store", P.args(P.eq(version1.getGenericValue())));
        delegatorMock.setStrict(true);

        final DefaultVersionManager defaultVersionManager = getVersionManager();

        final String[] idsToArchive = { version1.getString("id") };
        final String[] idsToUnArchive = {};

        defaultVersionManager.archiveVersions(idsToArchive, idsToUnArchive);

        delegatorMock.verify();
        issueManagerMock.verify();
    }

    public void testGetUnarchivedVersions() throws Exception
    {
        final Collection unarchivedVersions = EasyList.build(version1.getGenericValue(), version2.getGenericValue(), version3.getGenericValue());

        delegatorMock.expectAndReturn("findByAnd", P.args(P.eq("Version"), P.eq(EasyMap.build("project", project1Obj.getId())), P.eq(EasyList.build("sequence"))), unarchivedVersions);
        delegatorMock.setStrict(true);

        final DefaultVersionManager defaultVersionManager = getVersionManager();

        final Collection unarchived = defaultVersionManager.getVersionsUnarchived(project1Obj.getId());
        assertTrue(!unarchived.isEmpty());
        assertEquals(3, unarchived.size());
        assertTrue(unarchived.contains(version1));
        assertTrue(unarchived.contains(version2));
        assertTrue(unarchived.contains(version3));

        //        assertTrue(!mdcm.getFlushes().isEmpty());
        //        assertEquals(9, mdcm.getFlushes().size());
        //        _testFlushes(issue1, IssueRelationConstants.VERSION);
    }

    public void testGetArchived() throws GenericEntityException
    {
        final Collection unarchivedVersions = EasyList.build(version4.getGenericValue(), version5.getGenericValue(), version6.getGenericValue());

        delegatorMock.expectAndReturn("findByAnd", P.args(P.eq("Version"), P.eq(EasyMap.build("project", project1Obj.getId())), P.eq(EasyList.build("sequence"))), unarchivedVersions);
        delegatorMock.setStrict(true);

        final DefaultVersionManager defaultVersionManager = getVersionManager();

        final Collection archived = defaultVersionManager.getVersionsArchived(project1);
        assertTrue(!archived.isEmpty());
        assertEquals(3, archived.size());
        assertTrue(archived.contains(version4));
        assertTrue(archived.contains(version5));
        assertTrue(archived.contains(version6));

    }

    public void testGetVersionsByName() throws GenericEntityException
    {
        final Collection allVersions = EasyList.build(versionOne.getGenericValue(), version1.getGenericValue(), version2.getGenericValue(),
            version3.getGenericValue(), version4.getGenericValue(), version5.getGenericValue(), version6.getGenericValue());

        delegatorMock.expectAndReturn("findAll", P.args(P.eq("Version"), P.eq(EasyList.build("sequence"))), allVersions);
        delegatorMock.setStrict(true);

        final DefaultVersionManager defaultVersionManager = getVersionManager();

        final Collection versionOnes = defaultVersionManager.getVersionsByName("Version 1");
        assertEquals(2, versionOnes.size());
        assertTrue(versionOnes.contains(versionOne));
        assertTrue(versionOnes.contains(version1));

        final Collection casedVersionOnes = defaultVersionManager.getVersionsByName("VerSioN 1");
        assertEquals(2, casedVersionOnes.size());
        assertTrue(casedVersionOnes.contains(versionOne));
        assertTrue(casedVersionOnes.contains(version1));

        final Collection unknownVersion = defaultVersionManager.getVersionsByName("xxx");
        assertTrue(unknownVersion.isEmpty());
    }

    public void testGetAllVersions() throws Exception
    {
        final Collection allVersionsIn = EasyList.build(versionOne.getGenericValue(), version1.getGenericValue(), version2.getGenericValue(),
            version3.getGenericValue(), version4.getGenericValue(), version5.getGenericValue(), version6.getGenericValue());

        delegatorMock.expectAndReturn("findAll", P.args(P.eq("Version"), P.eq(EasyList.build("sequence"))), allVersionsIn);
        delegatorMock.setStrict(true);

        final DefaultVersionManager defaultVersionManager = getVersionManager();

        final Collection allVersions = defaultVersionManager.getAllVersions();
        assertEquals(7, allVersions.size());
        assertTrue(allVersions.contains(versionOne));
        assertTrue(allVersions.contains(version1));
        assertTrue(allVersions.contains(version2));
        assertTrue(allVersions.contains(version3));
        assertTrue(allVersions.contains(version4));
        assertTrue(allVersions.contains(version5));
        assertTrue(allVersions.contains(version6));
    }

    public void testGetAllVersionsReleased() throws Exception
    {
        final Collection allVersionsIn = EasyList.build(versionOne.getGenericValue(), version1.getGenericValue(), version2.getGenericValue(),
            version3.getGenericValue(), version4.getGenericValue(), version5.getGenericValue(), version6.getGenericValue(),
            version7.getGenericValue());

        delegatorMock.expectAndReturn("findAll", P.args(P.eq("Version"), P.eq(EasyList.build("sequence"))), allVersionsIn);
        delegatorMock.setStrict(true);

        final DefaultVersionManager defaultVersionManager = getVersionManager();

        final Collection releasedVersions = defaultVersionManager.getAllVersionsReleased(true);
        assertEquals(4, releasedVersions.size());
        assertTrue(releasedVersions.contains(version4));
        assertTrue(releasedVersions.contains(version5));
        assertTrue(releasedVersions.contains(version6));
        assertTrue(releasedVersions.contains(version7));

        final Collection releasedVersionsNotArchived = defaultVersionManager.getAllVersionsReleased(false);
        assertEquals(1, releasedVersionsNotArchived.size());
        assertTrue(releasedVersionsNotArchived.contains(version7));
    }

    public void testGetAllVersionsUnreleased() throws Exception
    {
        final Collection allVersionsIn = EasyList.build(version1.getGenericValue(), version2.getGenericValue(), version3.getGenericValue(),
            version4.getGenericValue(), version5.getGenericValue(), version6.getGenericValue(), version8.getGenericValue());

        delegatorMock.expectAndReturn("findAll", P.args(P.eq("Version"), P.eq(EasyList.build("sequence"))), allVersionsIn);
        delegatorMock.setStrict(true);

        final DefaultVersionManager defaultVersionManager = getVersionManager();

        final Collection unreleasedVersions = defaultVersionManager.getAllVersionsUnreleased(true);
        assertEquals(4, unreleasedVersions.size());
        assertTrue(unreleasedVersions.contains(version1));
        assertTrue(unreleasedVersions.contains(version2));
        assertTrue(unreleasedVersions.contains(version3));
        assertTrue(unreleasedVersions.contains(version8));

        final Collection unreleasedVersionsNotArchived = defaultVersionManager.getAllVersionsUnreleased(false);
        assertEquals(3, unreleasedVersionsNotArchived.size());
        assertTrue(unreleasedVersions.contains(version1));
        assertTrue(unreleasedVersions.contains(version2));
        assertTrue(unreleasedVersions.contains(version3));
    }

    public void testIsVersionOverdue()
    {
        final DefaultVersionManager versionManager = getVersionManager();

        final Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTimeInMillis(System.currentTimeMillis());
        tomorrow.add(Calendar.DATE, 1);

        final Version vTomorrow = new MyVersion(null, new MockGenericValue("Version", EasyMap.build("name", "Version Tomorrow", "id",
            new Long(22222), "sequence", new Long(2), "project", PROJECT_ONE_ID, "releasedate", new Timestamp(tomorrow.getTimeInMillis()),
            "description", "The description")));
        assertFalse("Future day was marked incorrectly overdue", versionManager.isVersionOverDue(vTomorrow));

        final Calendar yesterday = Calendar.getInstance();
        yesterday.setTimeInMillis(System.currentTimeMillis());
        yesterday.add(Calendar.DATE, -1);

        final Version vOverdue = new MyVersion(null, new MockGenericValue("Version", EasyMap.build("name", "Version Yesterday", "id",
            new Long(33333), "sequence", new Long(2), "project", PROJECT_ONE_ID, "releasedate", new Timestamp(yesterday.getTimeInMillis()),
            "description", "The description")));
        assertTrue("Past date was marked as not overdue", versionManager.isVersionOverDue(vOverdue));

        final Version vToday = new MyVersion(null, new MockGenericValue("Version", EasyMap.build("name", "Version Today", "id", new Long(11111),
            "sequence", new Long(2), "project", PROJECT_ONE_ID, "releasedate", new Timestamp(System.currentTimeMillis()), "description",
            "The description")));
        assertFalse("Today was marked incorrectly overdue", versionManager.isVersionOverDue(vToday));
    }

    private DefaultVersionManager getVersionManager()
    {
        final OfBizVersionStore versionStore = new OfBizVersionStore((getDelegatorMock()));
        return new DefaultVersionManager(getIssueManagerMock(), new CollectionReorderer(), getAssociationManagerMock(),
            getIssueIndexManagerMock(), getProjectManagerMock(), versionStore);
    }

    private ProjectManager getProjectManagerMock()
    {
        return (ProjectManager) projectManager.proxy();
    }

    private IssueIndexManager getIssueIndexManagerMock()
    {
        return (IssueIndexManager) issueIndexManagerMock.proxy();
    }

    private OfBizDelegator getDelegatorMock()
    {
        return (OfBizDelegator) delegatorMock.proxy();
    }

    private AssociationManager getAssociationManagerMock()
    {
        return (AssociationManager) associationManager.proxy();
    }

    private IssueManager getIssueManagerMock()
    {
        return (IssueManager) issueManagerMock.proxy();
    }

    private class MyVersion extends VersionImpl
    {
        String name;
        long id;
        long sequence;
        boolean archived;
        boolean released;

        public MyVersion(final ProjectManager theProjectManager, final GenericValue genericValue)
        {
            super(getProjectManagerMock(), genericValue);
        }

        public GenericValue getProject()
        {
            return project1;
        }

        @Override
        public Project getProjectObject()
        {
            return new ProjectImpl(project1);
        }
    }

    private List assertMoveVersionAfter(final DefaultVersionManager defaultVersionManager, final Constraint[] storeConstraints, final List currentVersions, final Version toMove, final Long scheduleAfterVersionId, final List expectedVersions)
    {
        delegatorMock.expectAndReturn("findByAnd", P.args(P.eq("Version"), P.eq(EasyMap.build("project", PROJECT_ONE_ID)), P.eq(EasyList.build("sequence"))), currentVersions);
        if (storeConstraints == null)
        {
            delegatorMock.expectNotCalled("store");
        }
        else
        {
            delegatorMock.expectVoid("store", storeConstraints);
        }
        delegatorMock.setStrict(true);
        defaultVersionManager.moveVersionAfter(toMove, scheduleAfterVersionId);
        int expectedSequence = 1;
        for (final Iterator iterator = expectedVersions.iterator(); iterator.hasNext(); ++expectedSequence)
        {
            final GenericValue versionGV = (GenericValue) iterator.next();
            assertEquals(new Long(expectedSequence), versionGV.get("sequence"));
        }
        return expectedVersions;
    }

    private void expectFindByPrimaryVersionKey(final Version version)
    {
        delegatorMock.expectAndReturn("findById", P.args(P.eq("Version"), P.eq(version.getId())),
            version.getGenericValue());
    }

    private class IdentityEqualsMockIssue extends MockIssue
    {
        private IdentityEqualsMockIssue(final Long id)
        {
            super(id);
        }

        public boolean equals(final Object o)
        {
            if (!(o instanceof IdentityEqualsMockIssue))
            {
                return false;
            }
            final Long otherId = ((IdentityEqualsMockIssue) o).getId();
            return getId().equals(otherId);
        }

        public int hashCode()
        {
            return (int) getId().longValue();
        }
    }
}
