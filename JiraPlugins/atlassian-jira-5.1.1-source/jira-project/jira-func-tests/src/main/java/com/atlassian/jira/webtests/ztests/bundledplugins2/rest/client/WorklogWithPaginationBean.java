package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import java.util.List;

public class WorklogWithPaginationBean
{
    public Integer startAt;
    public Integer maxResults;
    public Integer total;
    public List<Worklog> worklogs;
}
