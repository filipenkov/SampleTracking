package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import java.util.List;
import java.util.Map;

/**
 * Client class for the Project resource.
 *
 * @since v4.3
 */
public class ProjectClient extends RestApiClient<ProjectClient>
{
    /**
     * Constructs a new ProjectClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public ProjectClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * GETs the project having the given key.
     *
     * @param projectKey a String containing the project key
     * @return a Project
     * @throws com.sun.jersey.api.client.UniformInterfaceException if there is a problem
     */
    public Project get(String projectKey) throws UniformInterfaceException
    {
        return projectWithKey(projectKey).get(Project.class);
    }

    /**
     * GETs a list of versions, which are visible to the current user.
     *
     * @return a map of projects.
     */
    public List<Project> getProjects()
    {
        return projects().get(Project.PROJECTS_TYPE);
    }

    /**
     * GETs a list of versions, associated with the passed project.
     *
     * @param key the key of the project to query.
     * @return a list of versions.
     */
    public List<Version> getVersions(String key)
    {
        return projectVersionWithKey(key).get(Version.VERSIONS_TYPE);
    }

    /**
     * GETs a map of avatars, associated with the passed project.
     *
     * @param key the key of the project to query.
     * @return a map of avatars, system and custom
     */
    public Map<String, List<Avatar>> getAvatars(String key)
    {
        return projectWithKey(key).path("avatars").get(Avatar.ALLAVATARS_TYPE);
    }

    /**
     * GETs a single avatar, associated with the passed project
     *
     * @param key the key of the project to query
     * @param id the database id of the avatar
     * @return avatar
     */
    public Avatar getAvatar(String key, Long id)
    {
        return projectWithKey(key).path("avatar").path(id.toString())
                .get(Avatar.AVATAR_TYPE);
    }

    /**
     * GETs the project having the given key, and returns a Response.
     *
     * @param projectKey a String containing the project key
     * @return a Response
     */
    public Response getResponse(final String projectKey)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return projectWithKey(projectKey).get(ClientResponse.class);
            }
        });
    }

    /**
     * GETs the versions associated with the passed project.
     *
     * @param projectKey a String containing the project key
     * @return a Response
     */
    public Response getVersionsResponse(final String projectKey)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return projectWithKey(projectKey).get(ClientResponse.class);
            }
        });
    }

    /**
     * GETs a list of components, associated with the passed project.
     *
     * @param key the key of the project to query.
     * @return a list of components.
     */
    public List<Component> getComponents(String key)
    {
        return projectComponentWithKey(key).get(Component.COMPONENTS_TYPE);
    }

    /**
     * Returns a WebResource for the project having the given key.
     *
     * @param projectKey a String containing the project key
     * @return a Response
     */
    protected WebResource projectWithKey(String projectKey)
    {
        return createResource().path("project").path(projectKey);
    }

    /**
     * Returns a WebResource for the versions gi
     *
     * @param projectKey a String containing the project key
     * @return a Response
     */
    protected WebResource projectVersionWithKey(String projectKey)
    {
        return createResource().path("project").path(projectKey).path("versions");
    }

    /**
     * Returns a WebResource for the versions gi
     *
     * @param projectKey a String containing the project key
     * @return a Response
     */
    protected WebResource projectComponentWithKey(String projectKey)
    {
        return createResource().path("project").path(projectKey).path("components");
    }

    /**
     * Returns a WebResource for the projects visible to the current user.
     *
     * @return a Response
     */
    protected WebResource projects()
    {
        return createResource().path("project");
    }
}
