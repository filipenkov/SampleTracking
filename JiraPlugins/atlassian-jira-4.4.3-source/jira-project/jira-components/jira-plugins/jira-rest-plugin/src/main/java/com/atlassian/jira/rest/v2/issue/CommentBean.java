package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.rest.bind.DateTimeAdapter;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

enum VisibilityType
{
    GROUP,
    ROLE
}

@SuppressWarnings ( { "UnusedDeclaration", "FieldCanBeLocal" })
@XmlRootElement( name="visibility")
class VisibilityBean
{
    public VisibilityType type;
    public String value;

    public VisibilityBean() {}

    public VisibilityBean(final VisibilityType type, final String value)
    {
        this.type = type;
        this.value = value;
    }
}

/**
* @since v4.2
*/
@SuppressWarnings ({ "UnusedDeclaration" })
@XmlRootElement (name="comment")
public class CommentBean
{
    @XmlElement
    private URI self;

    @XmlElement
    private UserBean author;

    @XmlElement
    private String body;

    @XmlElement
    private UserBean updateAuthor;

    @XmlJavaTypeAdapter (DateTimeAdapter.class)
    private Date created;

    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    private Date updated;

    @XmlElement
    private VisibilityBean visibility;

    public CommentBean() {}
    public CommentBean(final Comment comment, final UriInfo uriInfo)
    {
        this.self = uriInfo.getBaseUriBuilder().path(CommentResource.class).path(comment.getId().toString()).build();
        this.author = new UserBeanBuilder().user(comment.getAuthorObject()).context(uriInfo).buildShort();
        this.body = comment.getBody();
        this.created = comment.getCreated();
        this.updated = comment.getUpdated();
        this.updateAuthor = new UserBeanBuilder().user(comment.getUpdateAuthorObject()).context(uriInfo).buildShort();

        final String groupLevel = comment.getGroupLevel();
        if (groupLevel != null)
        {
            this.visibility = new VisibilityBean(VisibilityType.GROUP, groupLevel);
        }
        else
        {
            final ProjectRoleManager projectRoleManager = getProjectRoleManager();
            final Long roleId = comment.getRoleLevelId();
            if (roleId != null)
            {
                final String roleName = projectRoleManager.getProjectRole(roleId).getName();
                this.visibility = new VisibilityBean(VisibilityType.ROLE, roleName);
            }
        }
    }

    public String getBody()
    {
        return body;
    }

    public VisibilityBean getVisibility()
    {
        return visibility;
    }

    protected ProjectRoleManager getProjectRoleManager()
    {
        return ComponentManager.getComponentInstanceOfType(ProjectRoleManager.class);
    }

    public static List<CommentBean> asBeans(final List<Comment> comments, final UriInfo uriInfo)
    {
        return CollectionUtil.transform(comments, new Function<Comment, CommentBean>()
        {
            public CommentBean get(final Comment input)
            {
                return new CommentBean(input, uriInfo);
            }
        });
    }

    static final CommentBean DOC_EXAMPLE = new CommentBean();
    static {
        try {
            DOC_EXAMPLE.self = new URI("http://example.com:8080/jira/rest/api/2.0/comment/10000");
            DOC_EXAMPLE.author = UserBean.SHORT_DOC_EXAMPLE;
            DOC_EXAMPLE.updateAuthor = UserBean.SHORT_DOC_EXAMPLE;
            DOC_EXAMPLE.body = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque eget venenatis elit. Duis eu justo eget augue iaculis fermentum. Sed semper quam laoreet nisi egestas at posuere augue semper.";
            DOC_EXAMPLE.created = new Date();
            DOC_EXAMPLE.updated = new Date();
            DOC_EXAMPLE.visibility = new VisibilityBean(VisibilityType.ROLE, "Administrators");
        }
        catch (URISyntaxException e)
        {
            // can't happen
        }
    }

    static final CommentBean DOC_COMMENT_LINK_ISSUE_EXAMPLE = new CommentBean();
    static {
        DOC_COMMENT_LINK_ISSUE_EXAMPLE.body = "Linked related issue!";
        DOC_COMMENT_LINK_ISSUE_EXAMPLE.visibility = new VisibilityBean(VisibilityType.GROUP, "jira-users");
    }

}
