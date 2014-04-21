package com.atlassian.jira.issue.link;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.util.UserTestUtil;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestIssueLinkTypeDestroyer extends LegacyJiraMockTestCase
{
    private IssueLinkTypeDestroyerImpl iltd;
    private Mock mockIssueLinkTypeManager;
    private User testUser;
    private Long id;
    private ArrayList issueLinks;

    public TestIssueLinkTypeDestroyer(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        mockIssueLinkTypeManager = new Mock(IssueLinkTypeManager.class);
        mockIssueLinkTypeManager.setStrict(true);

        testUser = UserTestUtil.getUser("test user");

        id = new Long(0);
        issueLinks = new ArrayList();
        final GenericValue issueLinkGV1 = UtilsForTests.getTestEntity("IssueLink", EasyMap.build("source", new Long(1000), "destination", new Long(2000), "linktype", id));
        issueLinks.add(new IssueLink(issueLinkGV1, null, null));
        final GenericValue issueLinkGV2 = UtilsForTests.getTestEntity("IssueLink", EasyMap.build("source", new Long(3000), "destination", new Long(5000), "linktype", id));
        issueLinks.add(new IssueLink(issueLinkGV2, null, null));
    }

    public void testRemoveIssueLinkTypeNoSwap() throws RemoveException
    {
        MyIssueLinkManager issueLinkManager = new MyIssueLinkManager()
        {
            int called;
            int expectedCalled = issueLinks.size();

            public Collection getIssueLinks(Long issueLinkTypeId)
            {
                assertEquals(id, issueLinkTypeId);
                return issueLinks;
            }

            public void changeIssueLinkType(final IssueLink issueLink, final IssueLinkType swapLinkType, final com.atlassian.crowd.embedded.api.User remoteUser)
                    throws RemoveException
            {
                changeIssueLinkType(issueLink, swapLinkType, (User) remoteUser);
            }

            public boolean isLinkingEnabled()
            {
                throw new UnsupportedOperationException("Not implemented.");
            }

            public void clearCache()
            {
                // nothing
            }

            public void verify()
            {
                assertEquals(expectedCalled, called);
            }

            public void removeIssueLink(IssueLink issueLink, User remoteUser)
            {
                assertEquals(testUser, remoteUser);
                assertTrue(issueLinks.contains(issueLink));
                called++;
                if (called > expectedCalled)
                {
                    fail("removeIssueLink called '" + called + "' times instead of '" + expectedCalled + "'.");
                }
            }

            @Override
            public LinkCollection getLinkCollection(Issue issue, User remoteUser, boolean excludeSystemLinks)
            {
                return getLinkCollection(issue, remoteUser);
            }
        };

        mockIssueLinkTypeManager.expectVoid("removeIssueLinkType", P.args(new IsEqual(id)));

        setupDestroyer((IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), issueLinkManager);
        iltd.removeIssueLinkType(id, null, testUser);

        issueLinkManager.verify();
        verifyMocks();
    }

    public void testRemoveIssueLinkTypeSwap() throws RemoveException
    {
        final GenericValue issueLinkTypeGV = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("linkname", "test link type name", "outward", "test outward", "inward", "test inward"));
        final IssueLinkType expectedSwapIssueLinkType = new IssueLinkType(issueLinkTypeGV);


        MyIssueLinkManager issueLinkManager = new MyIssueLinkManager()
        {
            int called;
            int expectedCalled = issueLinks.size();

            public Collection getIssueLinks(Long issueLinkTypeId)
            {
                assertEquals(id, issueLinkTypeId);
                return issueLinks;
            }

            public void verify()
            {
                assertEquals(expectedCalled, called);
            }

            public void changeIssueLinkType(IssueLink issueLink, IssueLinkType swapLinkType, User remoteUser) throws RemoveException
            {
                assertEquals(testUser, remoteUser);
                assertTrue(issueLinks.contains(issueLink));
                assertEquals(expectedSwapIssueLinkType, swapLinkType);
                called++;
                if (called > expectedCalled)
                {
                    fail("removeIssueLink called '" + called + "' times instead of '" + expectedCalled + "'.");
                }
            }

            public boolean isLinkingEnabled()
            {
                throw new UnsupportedOperationException("Not implemented.");
            }

            public void clearCache()
            {
                // nothing
            }

            @Override
            public LinkCollection getLinkCollection(Issue issue, User remoteUser, boolean excludeSystemLinks)
            {
                return getLinkCollection(issue, remoteUser);
            }
        };

        mockIssueLinkTypeManager.expectVoid("removeIssueLinkType", P.args(new IsEqual(id)));

        setupDestroyer((IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), issueLinkManager);
        iltd.removeIssueLinkType(id, expectedSwapIssueLinkType, testUser);

        issueLinkManager.verify();
        verifyMocks();
    }

    private void setupDestroyer(IssueLinkTypeManager issueLinkTypeManager, IssueLinkManager issueLinkManager)
    {
        iltd = new IssueLinkTypeDestroyerImpl(issueLinkTypeManager, issueLinkManager);
    }

    private void verifyMocks()
    {
        mockIssueLinkTypeManager.verify();
    }
}

abstract class MyIssueLinkManager implements IssueLinkManager
{
    public void createIssueLink(Long sourceId, Long destinationId, Long issueLinkTypeId, Long sequence, User remoteUser) throws CreateException
    {
        throw new UnsupportedOperationException("This method should not have been called.");
    }

    public void createIssueLink(Long sourceId, Long destinationId, Long issueLinkTypeId, Long sequence, com.opensymphony.user.User remoteUser) throws CreateException
    {
        throw new UnsupportedOperationException("This method should not have been called.");
    }

    public void removeIssueLink(IssueLink issueLink, User remoteUser) throws RemoveException
    {
        throw new UnsupportedOperationException("This method should not have been called.");
    }

    public void removeIssueLink(IssueLink issueLink, com.opensymphony.user.User remoteUser) throws RemoveException
    {
        throw new UnsupportedOperationException("This method should not have been called.");
    }

    public int removeIssueLinks(GenericValue issue, User remoteUser) throws RemoveException
    {
        throw new UnsupportedOperationException("This method should not have been called.");
    }

    public int removeIssueLinks(GenericValue issue, com.opensymphony.user.User remoteUser) throws RemoveException
    {
        throw new UnsupportedOperationException("This method should not have been called.");
    }

    public LinkCollection getLinkCollection(GenericValue issue, com.opensymphony.user.User remoteUser)
    {
        throw new UnsupportedOperationException("This method should not have been called.");
    }

    public LinkCollection getLinkCollection(Issue issue, User remoteUser)
    {
        throw new UnsupportedOperationException("This method should not have been called.");
    }

    public LinkCollection getLinkCollection(Issue issue, com.opensymphony.user.User remoteUser)
    {
        throw new UnsupportedOperationException("This method should not have been called.");
    }

    public LinkCollection getLinkCollectionOverrideSecurity(final Issue issue)
    {
        throw new UnsupportedOperationException("This method should not have been called.");
    }

    public Collection getIssueLinks(Long issueLinkTypeId)
    {
        throw new UnsupportedOperationException("This method should not have been called.");
    }

    public List getOutwardLinks(Long sourceId)
    {
        throw new UnsupportedOperationException("This method should not have been called.");
    }

    public List getInwardLinks(Long destinationId)
    {
        throw new UnsupportedOperationException("This method should not have been called.");
    }

    public void moveIssueLink(List issueList, Long currentSequence, Long sequence)
    {
        throw new UnsupportedOperationException("This method should not have been called.");
    }

    public void resetSequences(List issueLinks)
    {
        throw new UnsupportedOperationException("This method should not have been called.");
    }

    public IssueLink getIssueLink(Long sourceId, Long destinationId, Long issueLinkTypeId)
    {
        throw new UnsupportedOperationException("This method should not have been called.");
    }

    public void changeIssueLinkType(IssueLink issueLink, IssueLinkType swapLinkType, User remoteUser) throws RemoveException
    {
        throw new UnsupportedOperationException("This method should not have been called.");
    }

    public void changeIssueLinkType(IssueLink issueLink, IssueLinkType swapLinkType, com.opensymphony.user.User remoteUser) throws RemoveException
    {
        throw new UnsupportedOperationException("This method should not have been called.");
    }

    public abstract void verify();
}
