package com.atlassian.streams.jira;

import java.sql.Timestamp;
import java.util.Date;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.comments.Comment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.streams.api.ActivityObjectTypes.comment;
import static com.atlassian.streams.api.ActivityVerbs.post;
import static com.atlassian.streams.api.ActivityVerbs.update;
import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Pair.pair;
import static com.atlassian.streams.jira.JiraActivityObjectTypes.issue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JiraActivityItemTest
{
    private final String ISSUE_SUMMARY = "issue summary";

    @Mock Issue issue;
    @Mock Comment comment;
    @Mock ChangeHistory changeHistory;

    @Before
    public void prepareIssue()
    {
        when(issue.getCreated()).thenReturn(new Timestamp(0L));
    }

    @Before
    public void prepareComment()
    {
        when(comment.getCreated()).thenReturn(new Timestamp(0L));
    }

    @Before
    public void prepareChangeHistory()
    {
        when(changeHistory.getTimePerformed()).thenReturn(new Timestamp(0L));
    }
    
    @Test
    public void assertThatItemDateIsWhenIssueWasCreatedDate()
    {
        JiraActivityItem item = new JiraActivityItem(issue, ISSUE_SUMMARY, pair(issue(), post()), none(String.class));
        assertThat(item.getDate(), is(equalTo((Date) issue.getCreated())));
    }

    @Test
    public void assertThatItemDateIsWhenCommentWasCreatedDate()
    {
        JiraActivityItem item = new JiraActivityItem(issue, ISSUE_SUMMARY, pair(comment(), post()), comment);
        assertThat(item.getDate(), is(equalTo(comment.getCreated())));

    }

    @Test
    public void assertThatItemDateIsWhenChangeWasPerformed()
    {
        JiraActivityItem item = new JiraActivityItem(issue, ISSUE_SUMMARY, pair(issue(), update()), comment, changeHistory);
        assertThat(item.getDate(), is(equalTo((Date) changeHistory.getTimePerformed())));
    }
}
