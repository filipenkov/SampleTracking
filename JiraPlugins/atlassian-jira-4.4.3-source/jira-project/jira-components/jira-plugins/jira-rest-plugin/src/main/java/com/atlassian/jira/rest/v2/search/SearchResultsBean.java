package com.atlassian.jira.rest.v2.search;

import com.atlassian.jira.rest.v2.issue.IssueBean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.List;

/**
 * JAXB bean for returning search results.
 *
 * @since v4.3
 */
@XmlRootElement
public class SearchResultsBean
{
    /**
     * The example SearchResultsBean used in automatically-generated documentation.
     */
    public static final SearchResultsBean DOC_EXAMPLE = new SearchResultsBean(0, 50, 1, Arrays.asList(IssueBean.SHORT_DOC_EXAMPLE));

    @XmlElement
    public Integer startAt;

    @XmlElement
    public Integer maxResults;

    @XmlElement
    public Integer total;

    @XmlElement
    public List<IssueBean> issues;

    public SearchResultsBean()
    {
    }

    public SearchResultsBean(Integer startAt, Integer maxResults, Integer total, List<IssueBean> issues)
    {
        this.startAt = startAt;
        this.maxResults = maxResults;
        this.total = total;
        this.issues = issues;
    }
}
