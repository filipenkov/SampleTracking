package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Representation of a worklog in the JIRA REST API.
 *
 * @since v4.3
 */
@JsonSerialize
public class Worklog
{
    public String id;
    public String self;
    public UserJson author;
    public UserJson updateAuthor;
    public String comment;
    public String created;
    public String updated;
    public String started;
    public String timeSpent;
    public Long timeSpentSeconds;
    public Visibility visibility;


    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        Worklog worklog = (Worklog) o;

        if (StringUtils.isNotBlank(comment) ? !comment.equals(worklog.comment) : StringUtils.isNotBlank(worklog.comment)) { return false; }
        if (created != null ? !created.equals(worklog.created) : worklog.created != null) { return false; }
        if (id != null ? !id.equals(worklog.id) : worklog.id != null) { return false; }
        if (self != null ? !self.equals(worklog.self) : worklog.self != null) { return false; }
        if (started != null ? !started.equals(worklog.started) : worklog.started != null) { return false; }
        if (timeSpent != null ? !timeSpent.equals(worklog.timeSpent) : worklog.timeSpent != null) { return false; }

        if (updateAuthor != null)
        {
            if (!updateAuthor.displayName.equals(worklog.updateAuthor.displayName)) {return false; }
            if (!updateAuthor.name.equals(worklog.updateAuthor.name)) {return false; }
            if (!updateAuthor.self.equals(worklog.updateAuthor.self)) {return false; }
        }
        if (author != null)
        {
            if (!author.displayName.equals(worklog.author.displayName)) {return false; }
            if (!author.name.equals(worklog.author.name)) {return false; }
            if (!author.self.equals(worklog.author.self)) {return false; }
        }

        if (visibility != null && worklog.visibility != null)
        {
            if (visibility.type != null ? !visibility.type.equals(worklog.visibility.type) : worklog.visibility.type != null) { return false; }
            if (visibility.value != null ? !visibility.value.equals(worklog.visibility.value) : worklog.visibility.value != null) { return false; }
        }
        return true;
    }
}
