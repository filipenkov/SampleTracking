package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.ListeningTestCase;
import org.ofbiz.core.entity.GenericValue;

public class TestProjectComparator extends ListeningTestCase
{
    ProjectComparator projectComparator = new ProjectComparator();
    GenericValue issue1 = new MockGenericValue("Issue", EasyMap.build("key", "ONE-1"));
    GenericValue issue2 = new MockGenericValue("Issue", EasyMap.build("key", "TWO-1"));

    @Test
    public void testProjectSortSimple()
    {
        assertTrue(projectComparator.compare(issue1, issue1) == 0);
        assertTrue(projectComparator.compare(issue2, issue2) == 0);

        assertTrue(projectComparator.compare(issue1, issue2) < 0);
        assertTrue(projectComparator.compare(issue2, issue1) > 0);

        assertTrue(projectComparator.compare(issue1, null) < 0);
        assertTrue(projectComparator.compare(issue2, null) < 0);
        assertTrue(projectComparator.compare(null, issue1) > 0);
        assertTrue(projectComparator.compare(null, issue2) > 0);
    }
}
