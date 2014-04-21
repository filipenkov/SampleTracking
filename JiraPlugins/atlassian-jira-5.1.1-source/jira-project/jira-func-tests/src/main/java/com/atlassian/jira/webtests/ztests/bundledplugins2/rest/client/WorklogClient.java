package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * Client for the work log resource.
 *
 * @since v4.3
 */
public class WorklogClient extends RestApiClient<WorklogClient>
{
    /**
     * Constructs a new WorklogClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public WorklogClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * GETs the work log with the given id, returning a Response object.
     *
     * @return a Response
     */
    public WorklogWithPaginationBean getAll(final String issueKey)
    {
        return createResource().path("issue").path(issueKey).path("worklog").get(WorklogWithPaginationBean.class);
    }

    /**
     * GETs the work log with the given id.
     *
     * @param worklogID a String containing the work log id
     * @return a Worklog
     * @throws UniformInterfaceException if there is a problem getting the work log
     */
    public Worklog get(String issueKey, String worklogID) throws UniformInterfaceException
    {
        return worklogWithID(issueKey, worklogID).get(Worklog.class);
    }

    /**
     * GETs the work log with the given id, returning a Response object.
     *
     * @param worklogID a String containing the work log id
     * @return a Response
     */
    public Response getResponse(final String issueKey, final String worklogID)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return worklogWithID(issueKey, worklogID).get(ClientResponse.class);
            }
        });
    }

    /**
     * Returns a WebResource for the work log with the given id.
     *
     * @param issueKey worklog is associated with
     * @param worklogID a String containing the work log id
     * @return a WebResource
     */
    protected WebResource worklogWithID(String issueKey, String worklogID)
    {
        return createResource().path("issue").path(issueKey).path("worklog").path(worklogID);
    }

    public Response<Worklog> put(final String issueKey, final Worklog worklog)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource().path("issue").path(issueKey).path("worklog").path(worklog.id).type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, worklog);
            }
        }, Worklog.class);

    }

    public Response<Worklog> put(final String issueKey, final Worklog worklog, final Map<String, String> queryParams)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                WebResource path = createResource().path("issue").path(issueKey).path("worklog").path(worklog.id);
                for (Map.Entry<String, String> entry : queryParams.entrySet())
                {
                    path = path.queryParam(entry.getKey(), entry.getValue());
                }
                return path.type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, worklog);
            }
        }, Worklog.class);
    }

    public Response<Worklog> post(final String issueKey, final Worklog worklog)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource().path("issue").path(issueKey).path("worklog").type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, worklog);
            }
        }, Worklog.class);
    }

    public Response<Worklog> post(final String issueKey, final Worklog worklog, final Map<String, String> queryParams)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                    WebResource path = createResource().path("issue").path(issueKey).path("worklog");
                    for (Map.Entry<String, String> entry : queryParams.entrySet())
                    {
                       path = path.queryParam(entry.getKey(), entry.getValue());
                    }
                   return path.type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, worklog);
            }
        }, Worklog.class);
    }


    public Response delete(final String issueKey, final Worklog worklog)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource().path("issue").path(issueKey).path("worklog").path(worklog.id).type(MediaType.APPLICATION_JSON_TYPE).delete(ClientResponse.class);
            }
        });
    }

    public Response delete(final String issueKey, final Worklog worklog, final Map<String, String> queryParams)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                WebResource path = createResource().path("issue").path(issueKey).path("worklog").path(worklog.id);
                for (Map.Entry<String, String> entry : queryParams.entrySet())
                {
                    path = path.queryParam(entry.getKey(), entry.getValue());
                }
                return path.type(MediaType.APPLICATION_JSON_TYPE).delete(ClientResponse.class);
            }
        });
    }
}
