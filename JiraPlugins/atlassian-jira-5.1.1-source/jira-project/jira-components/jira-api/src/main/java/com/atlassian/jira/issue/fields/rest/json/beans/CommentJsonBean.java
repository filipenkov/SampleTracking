package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.rest.Dates;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.NotNull;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;
import java.util.Date;

import static com.google.common.collect.Collections2.transform;

/**
* @since v5.0
*/
@JsonIgnoreProperties (ignoreUnknown = true)
public class CommentJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String id;

    @JsonProperty
    private UserJsonBean author;

    @JsonProperty
    private String body;

    @JsonProperty
    private UserJsonBean updateAuthor;

    @JsonProperty
    private String created;

    @JsonProperty
    private String updated;

    @JsonProperty
    private VisibilityJsonBean visibility;

    @JsonIgnore
    private boolean isVisibilitySet = false;

    @JsonIgnore
    private boolean isBodySet = false;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getSelf()
    {
        return self;
    }

    public void setSelf(String self)
    {
        this.self = self;
    }

    public UserJsonBean getAuthor()
    {
        return author;
    }

    public void setAuthor(UserJsonBean author)
    {
        this.author = author;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
        this.isBodySet = true;
    }

    public UserJsonBean getUpdateAuthor()
    {
        return updateAuthor;
    }

    public void setUpdateAuthor(UserJsonBean updateAuthor)
    {
        this.updateAuthor = updateAuthor;
    }

    public Date getCreated()
    {
        return Dates.fromTimeString(created);
    }

    public void setCreated(Date created)
    {
        this.created = Dates.asTimeString(created);
    }

    public Date getUpdated()
    {
        return Dates.fromTimeString(updated);
    }

    public void setUpdated(Date updated)
    {
        this.updated = Dates.asTimeString(updated);
    }

    @JsonIgnore
    public boolean isBodySet()
    {
        return isBodySet;
    }

    @JsonIgnore
    public boolean isVisibilitySet()
    {
        return isVisibilitySet;
    }

    @JsonProperty
    public VisibilityJsonBean getVisibility()
    {
        return visibility;
    }

    @JsonProperty
    public void setVisibility(VisibilityJsonBean visibility)
    {
        this.visibility = visibility;
        this.isVisibilitySet = true;
    }

    public static Collection<CommentJsonBean> shortBeans(final Collection<Comment> comments, final JiraBaseUrls urls,
            final ProjectRoleManager projectRoleManager)
    {
        return transform(comments, new com.google.common.base.Function<Comment, CommentJsonBean>()
        {
            @Override
            public CommentJsonBean apply(Comment from)
            {
                return shortBean(from, urls, projectRoleManager);
            }
        });
    }

    /**
     *
     * @return null if the input is null
     */
    public static CommentJsonBean shortBean(final Comment comment,  final JiraBaseUrls urls,
            final ProjectRoleManager projectRoleManager)
    {
        if (comment == null)
        {
            return null;
        }
        final CommentJsonBean bean = new CommentJsonBean();
        addNonRenderableStuff(bean, comment, urls, projectRoleManager);
        bean.body = comment.getBody();
        bean.setCreated(comment.getCreated());
        bean.setUpdated(comment.getUpdated());
        return bean;
    }

    public static Collection<CommentJsonBean> renderedShortBeans(final Collection<Comment> comments, final JiraBaseUrls urls,
            final ProjectRoleManager projectRoleManager, final DateTimeFormatterFactory dateTimeFormatterFactory, final RendererManager rendererManager, final String rendererType, final IssueRenderContext renderContext)
    {
        return Lists.newArrayList(transform(comments, new com.google.common.base.Function<Comment, CommentJsonBean>()
        {
            @Override
            public CommentJsonBean apply(Comment from)
            {
                return renderedShortBean(from, urls, projectRoleManager, dateTimeFormatterFactory, rendererManager, rendererType, renderContext);
            }
        }));
    }

    /**
     * @return null if the input is null
     */
    public static CommentJsonBean renderedShortBean(Comment comment, JiraBaseUrls urls,
            ProjectRoleManager projectRoleManager, DateTimeFormatterFactory dateTimeFormatterFactory,
            RendererManager rendererManager, String rendererType, IssueRenderContext renderContext)
    {
        if (comment == null)
        {
            return null;
        }

        final CommentJsonBean bean = new CommentJsonBean();
        addNonRenderableStuff(bean, comment, urls, projectRoleManager);
        if (StringUtils.isNotBlank(rendererType))
        {
            bean.body = rendererManager.getRenderedContent(rendererType, comment.getBody(), renderContext);
        }
        else
        {
            bean.body = comment.getBody();
        }
        bean.created = comment.getCreated() == null ? "" : dateTimeFormatterFactory.formatter().forLoggedInUser().format(comment.getCreated());
        bean.updated = comment.getUpdated() == null ? "" : dateTimeFormatterFactory.formatter().forLoggedInUser().format(comment.getUpdated());
        return bean;
    }

    private static void addNonRenderableStuff(CommentJsonBean bean, @NotNull final Comment comment, final JiraBaseUrls urls,
            final ProjectRoleManager projectRoleManager)
    {
        bean.self = urls.restApi2BaseUrl() + "issue/" + comment.getIssue().getId() + "/comment/" + JiraUrlCodec.encode(comment.getId().toString());
        bean.id = comment.getId().toString();
        bean.author = UserJsonBean.shortBean(comment.getAuthorUser(), urls);
        bean.updateAuthor = UserJsonBean.shortBean(comment.getUpdateAuthorUser(), urls);
        bean.visibility = getVisibilityBean(comment, projectRoleManager);
    }

    private static VisibilityJsonBean getVisibilityBean(Comment comment, ProjectRoleManager projectRoleManager)
    {
        VisibilityJsonBean visibilityBean = null;
        final String groupLevel = comment.getGroupLevel();
        if (groupLevel != null)
        {
            visibilityBean = new VisibilityJsonBean(VisibilityJsonBean.VisibilityType.group, groupLevel);
        }
        else
        {
            final Long roleId = comment.getRoleLevelId();
            if (roleId != null)
            {
                final String roleName = projectRoleManager.getProjectRole(roleId).getName();
                visibilityBean = new VisibilityJsonBean(VisibilityJsonBean.VisibilityType.role, roleName);
            }
        }
        return visibilityBean;
    }

    public static final CommentJsonBean DOC_EXAMPLE = new CommentJsonBean();
    public static final CommentJsonBean DOC_UPDATE_EXAMPLE = new CommentJsonBean();
    static
    {
        DOC_EXAMPLE.setId("10000");
        DOC_EXAMPLE.setSelf("http://www.example.com/jira/rest/api/2.0/issue/10010/comment/10000");
        DOC_EXAMPLE.setAuthor(UserJsonBean.USER_SHORT_DOC_EXAMPLE);
        DOC_EXAMPLE.setUpdateAuthor(UserJsonBean.USER_SHORT_DOC_EXAMPLE);
        DOC_EXAMPLE.setBody("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque eget venenatis elit. Duis eu justo eget augue iaculis fermentum. Sed semper quam laoreet nisi egestas at posuere augue semper.");
        DOC_EXAMPLE.setCreated(new Date());
        DOC_EXAMPLE.setUpdated(new Date());
        DOC_EXAMPLE.setVisibility(new VisibilityJsonBean(VisibilityJsonBean.VisibilityType.role, "Administrators"));
        DOC_UPDATE_EXAMPLE.setBody("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque eget venenatis elit. Duis eu justo eget augue iaculis fermentum. Sed semper quam laoreet nisi egestas at posuere augue semper.");
        DOC_UPDATE_EXAMPLE.setVisibility(new VisibilityJsonBean(VisibilityJsonBean.VisibilityType.role, "Administrators"));
    }

}
