package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.TimeTrackingSystemField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.rest.api.field.FieldBean;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

/**
 * Unit tests for issue resource time tracking.
 *
 * @since v4.2
 */
public class IssueResourceTimeTrackingTest extends IssueResourceTest
{
    // these should be multiples of 60
    final long originalEstimateSeconds = 120L;
    final long timeSpentSeconds = 180L;
    final long remainingSeconds = 60L;

    Issue issue;
    TimeTrackingSystemField timeTrackingField;
    FieldLayoutItem fieldValueItem;

    /**
     * Verifies that the time tracking information is returned properly when time tracking is enabled.
     *
     * @throws Exception if anything goes wrong
     */
    public void testGetFieldValueTimeTracking() throws Exception
    {
        expect(applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING)).andReturn(true);
        replayMocks();

        IssueResource issueResource = createIssueResource();
        @SuppressWarnings ({ "unchecked" }) FieldBean<TimeTrackingBean> value = issueResource.getFieldValue(fieldValueItem, issue);

        TimeTrackingBean bean = value.getValue();
        assertNotNull(bean);
        assertEquals(originalEstimateSeconds, bean.getOriginalEstimateMinutes() * 60);
        assertEquals(timeSpentSeconds, bean.getTimeSpentMinutes() * 60);
        assertEquals(remainingSeconds, bean.getEstimateMinutes() * 60);
    }

    /**
     * Verifies that the time tracking information is not returned if time tracking is disabled.
     *
     * @throws Exception if anything goes wrong
     */
    public void testGetFieldValueTimeTrackingEnabled() throws Exception
    {
        expect(applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING)).andReturn(false);
        replayMocks();

        IssueResource issueResource = createIssueResource();
        assertNull("Bean should not be returned when time tracking is disabled", issueResource.getFieldValue(fieldValueItem, issue));
    }

    @Override
    protected void doSetUp()
    {
        issue = createMock(Issue.class);
        expect(issue.getOriginalEstimate()).andReturn(originalEstimateSeconds);
        expect(issue.getTimeSpent()).andReturn(timeSpentSeconds);
        expect(issue.getEstimate()).andReturn(remainingSeconds);

        timeTrackingField = createMock(TimeTrackingSystemField.class);
        expect(timeTrackingField.getId()).andReturn("timetracking").anyTimes();

        fieldValueItem = createMock(FieldLayoutItem.class);
        expect(fieldValueItem.getOrderableField()).andReturn(timeTrackingField);
    }

    protected void replayMocks()
    {
        super.replayMocks();
        replay(issue,
                timeTrackingField,
                fieldValueItem);
    }
}
