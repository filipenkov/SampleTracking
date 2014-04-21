package com.atlassian.jira.rest.v2.issue;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import static com.atlassian.jira.issue.IssueFieldConstants.TIMETRACKING;
import static com.atlassian.jira.issue.IssueFieldConstants.TIME_ESTIMATE;
import static com.atlassian.jira.issue.IssueFieldConstants.TIME_ORIGINAL_ESTIMATE;
import static com.atlassian.jira.issue.IssueFieldConstants.TIME_SPENT;

/**
 * This bean holds the time tracking information that is sent back to clients of the REST API.
 *
 * @since v4.2
 */
@XmlRootElement (name = TIMETRACKING)
public class TimeTrackingBean
{
    /**
     * The original time estimateMinutes.
     */
    @XmlElement (name = TIME_ORIGINAL_ESTIMATE)
    private Long originalEstimateMinutes;

    /**
     * The remaining time estimateMinutes.
     */
    @XmlElement (name = TIME_ESTIMATE)
    private Long estimateMinutes;

    /**
     * The remaining time estimateMinutes.
     */
    @XmlElement (name = TIME_SPENT)
    private Long timeSpentMinutes;

    /**
     * Creates a new TimeTrackingBean.
     *
     * @param originalEstimateSeconds the original estimateMinutes
     * @param estimateSeconds the remaining estimateMinutes
     * @param timeSpentSeconds the time spent
     */
    public TimeTrackingBean(Long originalEstimateSeconds, Long estimateSeconds, Long timeSpentSeconds)
    {
        this.originalEstimateMinutes = secondsToMinutes(originalEstimateSeconds);
        this.estimateMinutes = secondsToMinutes(estimateSeconds);
        this.timeSpentMinutes = secondsToMinutes(timeSpentSeconds);
    }

    /**
     * Returns true iff at least one of this bean's properties is non-null.
     *
     * @return a boolean indicating whether at least one of this bean's properties is non-null
     */
    public boolean hasValues()
    {
        return originalEstimateMinutes != null || estimateMinutes != null || timeSpentMinutes != null;
    }

    public Long getOriginalEstimateMinutes()
    {
        return originalEstimateMinutes;
    }

    public Long getEstimateMinutes()
    {
        return estimateMinutes;
    }

    public Long getTimeSpentMinutes()
    {
        return timeSpentMinutes;
    }

    private Long secondsToMinutes(Long seconds)
    {
        return seconds != null ? seconds / 60 : null;
    }
}
