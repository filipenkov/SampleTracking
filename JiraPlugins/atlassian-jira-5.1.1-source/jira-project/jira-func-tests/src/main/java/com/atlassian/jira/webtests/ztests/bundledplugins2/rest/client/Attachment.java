package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import java.util.Map;

/**
 * Representation of an attachment in the JIRA REST API. Example JSON:
 * <pre>
 * {
 *   self: http://localhost:8090/jira/rest/api/2/attachment/10000
 *   filename: attachment.txt
 *   author: {
 *     self: http://localhost:8090/jira/rest/api/2/user?username=admin
 *     name: admin
 *     displayName: Administrator
 *   }
 *   created: 2010-06-09T15:59:34.602+1000
 *   size: 19
 *   mimeType: text/plain
 *   content: http://localhost:8090/jira/secure/attachment/10000/attachment.txt
 * }
 * </pre>
 *
 * @since v4.3
 */
public class Attachment
{
    public String id;
    public String self;
    public String filename;
    public User author;
    public String created;
    public long size;
    public String mimeType;
    public String content;
    public String thumbnail;
    public Map<String, Object> properties;
}
