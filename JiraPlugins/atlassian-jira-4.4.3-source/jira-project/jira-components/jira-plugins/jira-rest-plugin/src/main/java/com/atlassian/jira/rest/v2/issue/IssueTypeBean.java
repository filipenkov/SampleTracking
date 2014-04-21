package com.atlassian.jira.rest.v2.issue;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
* @since v4.2
*/
@XmlRootElement (name="issueType")
public class IssueTypeBean
{
    /**
     * Example representation of an issue type.
     * <pre>
     * {
     *   self: "http://localhost:8090/jira/rest/api/2.0/issueType/3",
     *   description: "A task that needs to be done.",
     *   iconUrl: "http://localhost:8090/jira/images/icons/task.gif",
     *   name: "Task",
     *   subtask: false
     * }
     * </pre>
     */
    static final IssueTypeBean DOC_EXAMPLE;
    static
    {
        try
        {
            DOC_EXAMPLE = new IssueTypeBean(
                    new URI("http://localhost:8090/jira/rest/api/2.0/issueType/3"),
                    "A task that needs to be done.",
                    "http://localhost:8090/jira/images/icons/task.gif",
                    "Task",
                    false
            );
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e); // never happens
        }
    }

    @XmlElement
    private URI self;

    @XmlElement
    private String description;

    @XmlElement
    private String iconUrl;

    @XmlElement
    private String name;

    @XmlElement
    private Boolean subtask;

    /**
     * Non-public constructor used for reflection-based tools.
     */
    private IssueTypeBean() {
        // empty
    }

    public IssueTypeBean(URI selfUri, String description, String iconUrl, String name, boolean isSubtask)
    {
        this.self = selfUri;
        this.description = description;
        this.iconUrl = iconUrl;
        this.name = name;
        this.subtask = isSubtask;
    }
}
