package com.atlassian.jira.issue.link;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.ofbiz.core.entity.GenericValue;

public class TestIssueLink extends LegacyJiraMockTestCase
{
    private IssueLink issueLink;
    private GenericValue issueLinkTypeGV;
    private IssueLinkType issueLinkType;
    private GenericValue sourceIssue;
    private GenericValue destinationIssue;
    private GenericValue issueLinkGV;
    private Mock mockIssueLinkTypeManager;
    private Mock mockIssueManager;

    public TestIssueLink(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        issueLinkTypeGV = UtilsForTests.getTestEntity("IssueLinkType", EasyMap.build("linkname", "test link name", "outward", "test outward", "inward", "test inward", "style", "test style"));
        issueLinkType = new IssueLinkTypeImpl(issueLinkTypeGV);

        sourceIssue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test source summary", "key", "TST-1"));
        destinationIssue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test destination summary", "key", "TST-2"));

        mockIssueLinkTypeManager = new Mock(IssueLinkTypeManager.class);
        mockIssueLinkTypeManager.setStrict(true);

        mockIssueManager = new Mock(IssueManager.class);
        mockIssueManager.setStrict(true);
    }

    private void setupIssueLink(IssueLinkTypeManager issueLinkTypeManager, IssueManager issueManager)
    {
        issueLinkGV = UtilsForTests.getTestEntity("IssueLink", EasyMap.build("source", sourceIssue.getLong("id"), "destination", destinationIssue.getLong("id"), "linktype", issueLinkTypeGV.getLong("id"), "sequence", new Long(0)));
        issueLink = new IssueLinkImpl(issueLinkGV, issueLinkTypeManager, issueManager);
    }

    public void testConstructor()
    {
        final GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test summary"));

        try
        {
            new IssueLinkImpl(issue, null, null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Entity must be an 'IssueLink', not '" + issue.getEntityName() + "'.", e.getMessage());
        }
    }

    public void testGetters()
    {
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkType", P.args(new IsEqual(issueLinkTypeGV.getLong("id"))), issueLinkType);

        setupIssueLink((IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), (IssueManager) mockIssueManager.proxy());

        assertEquals(issueLinkGV.getLong("id"), issueLink.getId());
        assertEquals(sourceIssue.getLong("id"), issueLink.getSourceId());
        assertEquals(destinationIssue.getLong("id"), issueLink.getDestinationId());
        assertEquals(issueLinkTypeGV.getLong("id"), issueLink.getLinkTypeId());
        assertEquals(issueLinkGV.getLong("sequence"), issueLink.getSequence());

        assertEquals(issueLinkType, issueLink.getIssueLinkType());
        verifyMocks();

        setupIssueLink((IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), (IssueManager) mockIssueManager.proxy());
        verifyMocks();
    }

    private void verifyMocks()
    {
        mockIssueLinkTypeManager.verify();
        mockIssueManager.verify();
    }
}
