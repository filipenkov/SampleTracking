package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

/**
 * Representation of a comment in the JIRA REST API.
 *
 * @since v4.3
 */
public class Comment
{
    public String self;
    public String created;
    public String updated;
    public String body;
    public User author;
    public User updateAuthor;

    static public class Visibility
    {
        public String type;
        public String value;

        public Visibility() {}

        public Visibility(final String type, final String value)
        {
            this.type = type;
            this.value = value;
        }
    }
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
