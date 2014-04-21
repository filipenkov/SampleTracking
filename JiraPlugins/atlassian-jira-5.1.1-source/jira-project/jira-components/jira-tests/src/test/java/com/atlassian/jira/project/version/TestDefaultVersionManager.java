package com.atlassian.jira.project.version;

import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.project.MockVersion;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.CollectionReorderer;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Quick test for {@link DefaultVersionManager}. The legacy and slower tests are available in {@link
 * TestDefaultVersionManagerLegacy}
 *
 * @since v4.4
 */
public class TestDefaultVersionManager extends ListeningTestCase
{
    @Test
    public void testIsVersionOverDue() throws Exception
    {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.add(Calendar.MONTH, -1);
        Date releaseDate = calendar.getTime();

        DefaultVersionManager manager = new DefaultVersionManager(null, null, null, null, null, null);
        MockVersion version = new MockVersion(574438, "My Test Version");

        assertFalse("Version without released date should not be overdue.", manager.isVersionOverDue(version));

        version.setReleaseDate(releaseDate);
        assertTrue("Version should be overdue.", manager.isVersionOverDue(version));

        version.setReleased(true);
        assertFalse("Released version should never be overdue.", manager.isVersionOverDue(version));

        version.setReleased(false);
        version.setArchived(true);
        assertFalse("Archieved version should never be overdue.", manager.isVersionOverDue(version));

        version = new MockVersion(28829292, "My new Version");

        calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);

        version.setReleaseDate(calendar.getTime());
        assertFalse("Version due midnight today should not be overdue.", manager.isVersionOverDue(version));
        calendar.add(Calendar.SECOND, 1);
        version.setReleaseDate(calendar.getTime());
        assertFalse("Version due today should not be overdue.", manager.isVersionOverDue(version));
        calendar.add(Calendar.SECOND, -2);
        version.setReleaseDate(calendar.getTime());
        assertTrue("Version due yesterday should be overdue.", manager.isVersionOverDue(version));
    }

    @Test
    public void testEditVersionDetails() throws Exception
    {
        IssueManager issueManager = mock(IssueManager.class);
        AssociationManager associationManager = mock(AssociationManager.class);
        IssueIndexManager issueIndexManager = mock(IssueIndexManager.class);
        ProjectManager projectManager = mock(ProjectManager.class);
        VersionStore versionStore = mock(VersionStore.class);

        VersionManager versionManager = new DefaultVersionManager(issueManager, new CollectionReorderer(), associationManager, issueIndexManager, projectManager, versionStore);
        MockProject project = new MockProject(1000l);
        project.setVersions(Lists.<Version>newArrayList(new MockVersion(1011l, "1.0"), new MockVersion(1012l, "2.0")));

        Version version = new MockVersion(1234l, "Beta 1", project);
        versionManager.editVersionDetails(version, "Beta 2", "Beta 2 release");
        verify(versionStore).storeVersion(Mockito.argThat(new IsVersionIdEqual(version)));
    }

    @Test
    public void testEditVersionDetailsNameIsDuplicate() throws Exception
    {
        IssueManager issueManager = mock(IssueManager.class);
        AssociationManager associationManager = mock(AssociationManager.class);
        IssueIndexManager issueIndexManager = mock(IssueIndexManager.class);
        ProjectManager projectManager = mock(ProjectManager.class);
        VersionStore versionStore = mock(VersionStore.class);

        VersionManager versionManager = new DefaultVersionManager(issueManager, new CollectionReorderer(), associationManager, issueIndexManager, projectManager, versionStore);
        MockProject project = new MockProject(1000l);
        project.setVersions(Lists.<Version>newArrayList(new MockVersion(1011l, "Beta 1"), new MockVersion(1012l, "2.0")));

        Version version = new MockVersion(1234l, "1.0", project);
        try
        {
            versionManager.editVersionDetails(version, "bEtA 1", "Beta 1 release");
            fail("Expected it to fail, because version Beta 1 already exists!");
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals("A version with this name already exists in this project.", ex.getMessage());
        }
    }

    @Test
    public void testEditVersionDetailsNameIsEmpty() throws Exception
    {
        IssueManager issueManager = mock(IssueManager.class);
        AssociationManager associationManager = mock(AssociationManager.class);
        IssueIndexManager issueIndexManager = mock(IssueIndexManager.class);
        ProjectManager projectManager = mock(ProjectManager.class);
        VersionStore versionStore = mock(VersionStore.class);

        VersionManager versionManager = new DefaultVersionManager(issueManager, new CollectionReorderer(), associationManager, issueIndexManager, projectManager, versionStore);
        MockProject project = new MockProject(1000l);
        project.setVersions(Lists.<Version>newArrayList(new MockVersion(1011l, "1.0"), new MockVersion(1012l, "2.0")));

        Version version = new MockVersion(1234l, "Beta 1", project);
        try
        {
            versionManager.editVersionDetails(version, "", "Beta 2 release");
            fail("Expected it to fail, because the new version name is empty!");
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals("You must specify a valid version name.", ex.getMessage());
        }
    }

    @Test
    public void testGetAllAffectedVersions() throws Exception
    {
        IssueManager issueManager = mock(IssueManager.class);
        AssociationManager associationManager = mock(AssociationManager.class);
        IssueIndexManager issueIndexManager = mock(IssueIndexManager.class);
        ProjectManager projectManager = mock(ProjectManager.class);
        VersionStore versionStore = mock(VersionStore.class);

        VersionManager versionManager = new DefaultVersionManager(issueManager, new CollectionReorderer(), associationManager, issueIndexManager, projectManager, versionStore);
        MockIssue issue = new MockIssue(10000l);
        MockGenericValue issueGv = new MockGenericValue("Issue");
        MockGenericValue version1GV = new MockGenericValue("Version", 1010l);
        MockGenericValue version2GV = new MockGenericValue("Version", 1020l);
        issue.setGenericValue(issueGv);
        when(associationManager.getSinkFromSource(issueGv, "Version", IssueRelationConstants.VERSION, false)).thenReturn(Lists.<GenericValue>newArrayList(version1GV, version2GV));
        Collection<Version> versions = versionManager.getAffectedVersionsFor(issue);
        assertEquals(2, versions.size());
        Iterator<Version> iterator = versions.iterator();
        assertEquals(new Long(1010l), iterator.next().getId());
        assertEquals(new Long(1020l), iterator.next().getId());
    }

    @Test
    public void testGetFixIssues() throws Exception
    {
        IssueManager issueManager = mock(IssueManager.class);
        AssociationManager associationManager = mock(AssociationManager.class);
        IssueIndexManager issueIndexManager = mock(IssueIndexManager.class);
        ProjectManager projectManager = mock(ProjectManager.class);
        VersionStore versionStore = mock(VersionStore.class);

        VersionManager versionManager = new DefaultVersionManager(issueManager, new CollectionReorderer(), associationManager, issueIndexManager, projectManager, versionStore);
        MockIssue issue = new MockIssue(10000l);
        MockGenericValue issueGv = new MockGenericValue("Issue");
        MockGenericValue version1GV = new MockGenericValue("Version", 1015l);
        MockGenericValue version2GV = new MockGenericValue("Version", 1025l);
        issue.setGenericValue(issueGv);
        when(associationManager.getSinkFromSource(issueGv, "Version", IssueRelationConstants.FIX_VERSION, false)).thenReturn(Lists.<GenericValue>newArrayList(version1GV, version2GV));
        Collection<Version> versions = versionManager.getFixVersionsFor(issue);
        assertEquals(2, versions.size());
        Iterator<Version> iterator = versions.iterator();
        assertEquals(new Long(1015l), iterator.next().getId());
        assertEquals(new Long(1025l), iterator.next().getId());
    }

    @Test
    public void testGetAffectedIssues() throws Exception
    {
        IssueManager issueManager = mock(IssueManager.class);
        AssociationManager associationManager = mock(AssociationManager.class);
        IssueIndexManager issueIndexManager = mock(IssueIndexManager.class);
        ProjectManager projectManager = mock(ProjectManager.class);
        VersionStore versionStore = mock(VersionStore.class);
        final IssueFactory issueFactory = mock(IssueFactory.class);

        VersionManager versionManager = new DefaultVersionManager(issueManager, new CollectionReorderer(), associationManager, issueIndexManager, projectManager, versionStore)
        {
            @Override
            protected IssueFactory getIssueFactory()
            {
                return issueFactory;
            }
        };
        MockVersion version = new MockVersion(1000l, "Beta 1");
        MockGenericValue issue1GV = new MockGenericValue("Issue", 1000l);
        MockGenericValue issue2GV = new MockGenericValue("Issue", 1010l);
        ArrayList<GenericValue> issueGVs = Lists.<GenericValue>newArrayList(issue1GV, issue2GV);

        when(issueManager.getIssuesByEntity(IssueRelationConstants.VERSION, version.getGenericValue())).thenReturn(issueGVs);
        when(issueFactory.getIssues(issueGVs)).thenReturn(Lists.<Issue>newArrayList(new MockIssue(1000l), new MockIssue(1010l)));
        Collection<Issue> issues = versionManager.getIssuesWithAffectsVersion(version);
        assertEquals(2, issues.size());
        Iterator<Issue> iterator = issues.iterator();
        assertEquals(new Long(1000l), iterator.next().getId());
        assertEquals(new Long(1010l), iterator.next().getId());
    }


    class IsVersionIdEqual extends ArgumentMatcher<Version>
    {
        private final Version version;

        IsVersionIdEqual(Version version)
        {
            this.version = version;
        }

        public boolean matches(Object compare)
        {
            return ((Version) compare).getId().equals(version.getId());
        }

    }
}
