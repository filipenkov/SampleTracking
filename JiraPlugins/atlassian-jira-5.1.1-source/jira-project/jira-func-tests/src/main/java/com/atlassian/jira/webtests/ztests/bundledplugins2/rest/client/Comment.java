package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Representation of a comment in the JIRA REST API.
 *
 * @since v4.3
 */
@JsonSerialize
public class Comment
{
    public String self;
    public String id;
    public String created;
    public String updated;
    public String body;
    public UserJson author;
    public UserJson updateAuthor;
    public Visibility visibility;

    public Comment()
    {
    }
    
    public Comment(String body, String roleLevel)
    {
        this.body = body;
        this.visibility = new Visibility("ROLE", roleLevel);
    }
}
