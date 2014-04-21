package com.atlassian.jira.dev.backdoor;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.dev.backdoor.util.Errors;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.dev.backdoor.util.CacheControl.never;
import static org.apache.commons.lang.StringUtils.trimToNull;

/**
 * @since v5.0.1
 */
@Path ("issueType")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class IssueTypeBackdoorResource
{
    private final static String SUBTASK = SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE; 
    private final static String TASK = ""; 
    
    private final ConstantsManager constantsManager;
    private final IssueTypeManager issueTypeManager;
    
    public IssueTypeBackdoorResource(ConstantsManager constantsManager, IssueTypeManager issueTypeManager)
    {
        this.constantsManager = constantsManager;
        this.issueTypeManager = issueTypeManager;
    }

    @GET
    public Response getAllTypes()
    {
        final Collection<IssueType> issueTypes = constantsManager.getAllIssueTypeObjects();
        final List<IssueTypeBean> issueTypeBeans = Lists.newArrayList();
        for (IssueType issueType : issueTypes)
        {
            issueTypeBeans.add(new IssueTypeBean(issueType));
        }
        return Response.ok(issueTypeBeans).cacheControl(never()).build();
    }
    
    @POST
    public Response createIssueType(IssueTypeBean bean)
    {
        final String style = bean.subtask ? SUBTASK : TASK;        
        SimpleErrorCollection collections = new SimpleErrorCollection();
        constantsManager.validateCreateIssueType(bean.name, style, bean.description, bean.iconUrl, collections, "name");

        if (collections.hasAnyErrors())
        {
            return Response.status(Response.Status.BAD_REQUEST).cacheControl(never()).entity(Errors.of(collections)).build();
        }
        else
        {
            IssueType type;
            if (bean.subtask)
            {
                type = issueTypeManager.createSubTaskIssueType(bean.name, bean.description, bean.iconUrl);
            }
            else
            {
                type = issueTypeManager.createIssueType(bean.name, bean.description, bean.iconUrl);
            }
            return Response.ok(new IssueTypeBean(type)).cacheControl(never()).build();
        }
    }

    @DELETE
    @Path("{id}")
    public Response deleteIssueType(@PathParam("id") long id)
    {
        issueTypeManager.removeIssueType(String.valueOf(id), null);
        return Response.ok().cacheControl(never()).build();
    }

    public static class IssueTypeBean
    {
        @JsonProperty
        private String id;

        @JsonProperty
        private String description;

        @JsonProperty
        private String iconUrl;

        @JsonProperty
        private String name;

        @JsonProperty
        private boolean subtask;

        public IssueTypeBean()
        {
        }

        public IssueTypeBean(IssueType type)
        {
            id = trimToNull(type.getId());
            name = trimToNull(type.getName());
            iconUrl = trimToNull(type.getIconUrl());
            description = trimToNull(type.getDescription());
            subtask = type.isSubTask();
        }
    }
}
