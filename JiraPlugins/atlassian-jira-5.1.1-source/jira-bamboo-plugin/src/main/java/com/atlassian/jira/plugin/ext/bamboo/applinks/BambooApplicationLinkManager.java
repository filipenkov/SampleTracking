package com.atlassian.jira.plugin.ext.bamboo.applinks;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jira.project.Project;

/**
 * Manages links between your JIRA and Bamboo instances.
 * Serves as a proxy between {@link com.atlassian.applinks.api.ApplicationLinkService} and the action classes.
 */
public interface BambooApplicationLinkManager
{
    /**
     * Returns true if at least one Bamboo application link exists, false if not
     *
     * @return true if at least one Bamboo application link exists, false if not
     */
    boolean hasApplicationLinks();

    /**
     * Returns all Bamboo {@link ApplicationLink}s.
     *
     * @return all Bamboo {@link ApplicationLink}s.
     */
    Iterable<ApplicationLink> getApplicationLinks();

    /**
     * Returns the number of Bamboo {@link ApplicationLink}s.
     *
     * @return the number of Bamboo {@link ApplicationLink}s.
     */
    int getApplicationLinkCount();

    /**
     * Returns the Bamboo {@link ApplicationLink} associated with the given {@link Project}.
     *
     * @param projectKey the project key
     * @return the Bamboo {@link ApplicationLink} associated with the given {@link Project}.
     */
    ApplicationLink getApplicationLink(String projectKey);

    /**
     * Returns the Bamboo {@link ApplicationLink} associated with the given {@link ApplicationId}.
     *
     * @param applicationId the id
     * @return the Bamboo {@link ApplicationLink} associated with the given {@link ApplicationId}.
     */
    ApplicationLink getBambooApplicationLink(String applicationId);

    /**
     * Returns the {@link Project} keys associated with the given {@link ApplicationId}.
     *
     * @param applicationId the applicationId
     * @return the {@link Project} keys associated with the given {@link ApplicationId}.
     */
    Iterable<String> getProjects(String applicationId);

    /**
     * Returns true if at least one Bamboo project is associated with this {@link ApplicationId}, false if not
     *
     * @param applicationId the application id
     * @return true if at least one Bamboo project is associated with this {@link ApplicationId}, false if not
     */
    boolean hasAssociatedProjects(String applicationId);

    /**
     * Returns true if an {@link ApplicationLink} is associated with this {@link Project}, false if not
     *
     * @param projectKey the project key
     * @return true if an {@link ApplicationLink} is associated with this {@link Project}, fal
     */
    boolean hasAssociatedApplicationLink(String projectKey);

    /**
     * Returns true if associates, false if not.
     *
     * @param projectKey    the project key
     * @param applicationId the application id
     * @return true if associates, false if not.
     */
    boolean isAssociated(String projectKey, ApplicationId applicationId);

    /**
     * Associates a {@link Project} with an {@link ApplicationLink}.
     *
     * @param projectKey    the project key
     * @param applicationId the application id
     */
    void associate(String projectKey, ApplicationId applicationId);

    /**
     * Unassociates a {@link ApplicationLink} from all {@link Project}s.
     *
     * @param applicationId the application id
     */
    void unassociateAll(ApplicationId applicationId);
}
