package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.rest.api.field.FieldBean;
import com.atlassian.jira.rest.v2.issue.project.ProjectBean;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.plugins.rest.common.expand.Expandable;
import com.atlassian.plugins.rest.common.expand.SelfExpanding;
import com.google.common.collect.Maps;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;

import static com.atlassian.jira.issue.IssueFieldConstants.DESCRIPTION;
import static com.atlassian.jira.issue.IssueFieldConstants.PROJECT;
import static com.atlassian.jira.issue.IssueFieldConstants.TIMETRACKING;
import static com.atlassian.jira.issue.IssueFieldConstants.UPDATED;

/**
 * @since v4.2
 */
@SuppressWarnings ( { "UnusedDeclaration", "FieldCanBeLocal" })
@XmlRootElement (name = "issue")
public class IssueBean
{
    /**
     * Short example for use in automatically generated documentation.
     */
    public static final IssueBean SHORT_DOC_EXAMPLE = new IssueBean("HSP-1", Examples.restURI("jira/rest/api/2.0/issue/HSP-1"));

    /**
     * Example IssueBean instance for use in automatically generated documentation.
     */
    static final IssueBean DOC_EXAMPLE;

    static
    {
        try
        {
            IssueBean issue = new IssueBean("EX-1", new URI("http://example.com:8080/jira/rest/api/2.0/issue/EX-1"));
            issue.fields = new HashMap<String, FieldBean>()
            {{
                    put(UPDATED, FieldBean.create(UPDATED, JiraDataTypes.getType(UPDATED), new Date(1)));
                    put(DESCRIPTION, FieldBean.create(DESCRIPTION, JiraDataTypes.getType(DESCRIPTION), "example bug report"));
                    put(PROJECT, FieldBean.create(PROJECT, JiraDataTypes.getType(PROJECT), ProjectBean.SHORT_DOC_EXAMPLE_1));
                    put(TIMETRACKING, FieldBean.create(TIMETRACKING, JiraDataTypes.getType(TIMETRACKING), new TimeTrackingBean(600L, 200L, 400L)));
                }};

            issue.addField(IssueFieldConstants.ATTACHMENT, FieldBean.create(IssueFieldConstants.ATTACHMENT, JiraDataTypes.getType(IssueFieldConstants.ATTACHMENT), CollectionBuilder.list(AttachmentBean.DOC_EXAMPLE)));
            issue.addField(IssueFieldConstants.COMMENT, FieldBean.create(IssueFieldConstants.COMMENT, JiraDataTypes.getType(IssueFieldConstants.COMMENT), CollectionBuilder.list(CommentBean.DOC_EXAMPLE)));
            issue.addField(IssueFieldConstants.WORKLOG, FieldBean.create(IssueFieldConstants.WORKLOG, JiraDataTypes.getType(IssueFieldConstants.WORKLOG), CollectionBuilder.list(WorklogBean.DOC_EXAMPLE)));

            issue.addField("sub-tasks", FieldBean.create("sub-tasks", JiraDataTypes.getType(IssueFieldConstants.ISSUE_LINKS),
                    CollectionBuilder.list(new IssueLinkBean("EX-2", new URI("http://example.com:8080/jira/rest/api/2.0/issue/EX-2"), LinkedIssueTypeBean.instance().name("Sub-task").direction(LinkedIssueTypeBean.Direction.OUTBOUND).build()))));
            issue.addField("links", FieldBean.create("links", JiraDataTypes.getType(IssueFieldConstants.ISSUE_LINKS),
                    CollectionBuilder.list(
                            new IssueLinkBean("PRJ-2", new URI("http://example.com:8080/jira/rest/api/2.0/issue/PRJ-2"), LinkedIssueTypeBean.instance().name("Dependent").description("depends on").direction(LinkedIssueTypeBean.Direction.OUTBOUND).build()),
                            new IssueLinkBean("PRJ-3", new URI("http://example.com:8080/jira/rest/api/2.0/issue/PRJ-3"), LinkedIssueTypeBean.instance().name("Dependent").description("is depended by").direction(LinkedIssueTypeBean.Direction.INBOUND).build())
                    )));


            issue.addField(IssueFieldConstants.WATCHERS, FieldBean.create(IssueFieldConstants.WATCHERS, JiraDataTypes.getType(IssueFieldConstants.WATCHERS), WatchersBean.DOC_EXAMPLE));
            // set this as the documentation example
            DOC_EXAMPLE = issue;
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException(e); // never happens
        }
    }

    @XmlAttribute
    private String expand;

    @XmlElement
    private URI self;

    @XmlElement
    private String key;

    /*
     * Issue fields. The values in the map will always be a FieldBean.
     */
    @XmlElement
    private HashMap<String, FieldBean> fields;

    @XmlElement
    private HashMap<String, Object> html;

    @XmlTransient
    private HashMap<String, Object> expandedHtml = Maps.newHashMap();

    @XmlTransient
    @Expandable ("html")
    private SelfExpanding htmlExpander = new SelfExpanding()
    {
        public void expand()
        {
            html = expandedHtml;
        }
    };

    /*
     * Use the concrete class so that JAXB doesn't get confused.
     */
    @XmlElement
    private URI transitions;

    public IssueBean() {}

    public IssueBean(final String key, URI selfUri)
    {
        this.self = selfUri;
        this.key = key;
    }

    public void addField(final String name, final FieldBean value)
    {
        if (fields == null)
        {
            fields = Maps.newHashMap();
        }

        fields.put(name, value);
    }

    public void addHtml(final String name, final Object value)
    {
        expandedHtml.put(name, value);
    }

    public String getKey()
    {
        return key;
    }

    public void setTransitions(final URI transitions)
    {
        this.transitions = transitions;
    }
}
