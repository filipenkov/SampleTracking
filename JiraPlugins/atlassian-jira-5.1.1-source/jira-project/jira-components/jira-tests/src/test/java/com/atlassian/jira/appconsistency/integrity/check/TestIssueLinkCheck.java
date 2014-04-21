package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.appconsistency.integrity.amendment.DeleteEntityAmendment;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;

import java.util.ArrayList;
import java.util.List;

public class TestIssueLinkCheck extends LegacyJiraMockTestCase
{
    private static final int MAX_VALID_ISSUE_ID = 3;
    private List mockDB = new ArrayList();
    private List validIssues;
    private GenericValue validIssueLink;

    protected void setUp() throws Exception
    {
        super.setUp();

        //setup mock db
        //add issues
        validIssues = makeMockIssues();
        mockDB.addAll(validIssues);
        //add valid issue link
        validIssueLink = makeMockIssueLink(1, 1, 2);
        mockDB.add(validIssueLink);
        //add corrupt issue links
        mockDB.add(makeMockIssueLink(101, 1001, 2));
        mockDB.add(makeMockIssueLink(102, 2, 1001));
        mockDB.add(makeMockIssueLink(103, 1001, 1001));
    }

    public void testPreview() throws IntegrityException
    {
        List expectedDB = mockDB;

        MockOfBizDelegator mockOfBizDelegator = new MockOfBizDelegator(mockDB, expectedDB);

        IssueLinkCheck check = new IssueLinkCheck(mockOfBizDelegator, 0);
        List amendments = check.preview();

        //check that only corrupt issue links are returned
        assertEquals(3, amendments.size());
        assertEquals("101", (getAmendmentValue((DeleteEntityAmendment) amendments.get(0), "id")));
        assertEquals("102", (getAmendmentValue((DeleteEntityAmendment) amendments.get(1), "id")));
        assertEquals("103", (getAmendmentValue((DeleteEntityAmendment) amendments.get(2), "id")));

        //verify the expected db (no changes)
        mockOfBizDelegator.verifyAll();
    }

    public void testCorrect() throws IntegrityException
    {
        //setup expected db
        List expectedDB = new ArrayList();
        expectedDB.addAll(validIssues);
        expectedDB.add(validIssueLink);

        MockOfBizDelegator mockOfBizDelegator = new MockOfBizDelegator(mockDB, expectedDB);

        IssueLinkCheck check = new IssueLinkCheck(mockOfBizDelegator, 0);
        List amendments = check.correct();

        //check that only corrupt issue links are returned
        assertEquals(3, amendments.size());
        assertEquals("101", (getAmendmentValue((DeleteEntityAmendment) amendments.get(0), "id")));
        assertEquals("102", (getAmendmentValue((DeleteEntityAmendment) amendments.get(1), "id")));
        assertEquals("103", (getAmendmentValue((DeleteEntityAmendment) amendments.get(2), "id")));

        //verify the expected db (has changes)
        mockOfBizDelegator.verifyAll();

        //check if we run the preview/correct again, no corrupt issue links are found
        amendments = check.preview();
        assertTrue(amendments.isEmpty());
        amendments = check.correct();
        assertTrue(amendments.isEmpty());
    }

    private String getAmendmentValue(DeleteEntityAmendment amendment, String field)
    {
        return amendment.getEntity().getString(field);
    }

    private GenericValue makeMockIssueLink(long id, long source, long destination)
    {
        return UtilsForTests.getTestEntity("IssueLink", EasyMap.build("id", new Long(id), "source", new Long(source), "destination", new Long(destination)));
    }

    private List makeMockIssues()
    {
        List issues = new ArrayList();
        for (int i = 0; i < MAX_VALID_ISSUE_ID; i++)
        {
            issues.add(UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(i))));
        }
        return issues;
    }
}
