package com.atlassian.jira.collector.plugin.components;

import com.atlassian.jira.issue.Issue;
import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Arrays;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;

public class TestCollectorActivityHelperImpl
{

    @Test
    public void testNormalisation()
    {
        final CollectorActivityHelperImpl helper = new CollectorActivityHelperImpl(null);
        final Issue mockIssue1 = createMock(Issue.class);
        final Issue mockIssue2 = createMock(Issue.class);
        final Issue mockIssue3 = createMock(Issue.class);
        final Issue mockIssue4 = createMock(Issue.class);
        final DateTime onedayago = new DateTime();
        final DateTime threedaysago = new DateTime().dayOfYear().addToCopy(-3);
        final DateTime sevendaysago = new DateTime().dayOfYear().addToCopy(-7);
        expect(mockIssue1.getCreated()).andReturn(new Timestamp(onedayago.getMillis()));
        expect(mockIssue2.getCreated()).andReturn(new Timestamp(threedaysago.getMillis()));
        expect(mockIssue3.getCreated()).andReturn(new Timestamp(threedaysago.getMillis()));
        expect(mockIssue4.getCreated()).andReturn(new Timestamp(sevendaysago.getMillis()));


        replay(mockIssue1, mockIssue2, mockIssue3, mockIssue4);
        final int[] normalizedSums = helper.getNormalizedSums(6, ImmutableList.of(mockIssue1, mockIssue2, mockIssue3, mockIssue4));
        final int[] expected = { 0, 0, 2, 0, 0, 1 };
        assertTrue(Arrays.equals(expected, normalizedSums));

        verify(mockIssue1, mockIssue2, mockIssue3, mockIssue4);
    }
}
