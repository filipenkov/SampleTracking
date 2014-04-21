package com.atlassian.jira.extra.icalfeed.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.project.version.Version;
import com.atlassian.query.Query;
import org.joda.time.DateTime;

import java.text.ParseException;
import java.util.Collection;
import java.util.Set;

public interface EntityAsEventService
{
    Result search(Query query, Set<String> dateFieldNames, boolean includeFixVersions, User user) throws SearchException, ParseException;

    static class Result
    {
        public final Collection<IssueDateResult> issues;

        public final Collection<Version> affectedVersions;

        public final Collection<Version> fixedVersions;

        public Result(Collection<IssueDateResult> issues, Collection<Version> affectedVersions, Collection<Version> fixedVersions)
        {
            this.issues = issues;
            this.affectedVersions = affectedVersions;
            this.fixedVersions = fixedVersions;
        }
    }

    static class IssueDateResult
    {
        public final User assignee;

        public final String dateFieldKey;

        public final String dateFieldName;

        public final DateTime start;

        public final DateTime end;

        public final boolean allDay;

        public final String issueKey;

        public final String issueSummary;

        public final String issueDescription;

        public final String type;

        public final String typeIconUrl;

        public final String status;

        public final String statusIconUrl;

        public final DateTime issueCreated;

        public final DateTime issueUpdated;

        public IssueDateResult(User assignee, String dateFieldKey, String dateFieldName, DateTime start, DateTime end, boolean allDay, String issueKey, String issueSummary, String issueDescription, String type, String typeIconUrl, String status, String statusIconUrl, DateTime issueCreated, DateTime issueUpdated)
        {
            this.assignee = assignee;
            this.dateFieldKey = dateFieldKey;
            this.dateFieldName = dateFieldName;
            this.start = start;
            this.end = end;
            this.allDay = allDay;
            this.issueKey = issueKey;
            this.issueSummary = issueSummary;
            this.issueDescription = issueDescription;
            this.type = type;
            this.typeIconUrl = typeIconUrl;
            this.status = status;
            this.statusIconUrl = statusIconUrl;
            this.issueCreated = issueCreated;
            this.issueUpdated = issueUpdated;
        }
    }
}
