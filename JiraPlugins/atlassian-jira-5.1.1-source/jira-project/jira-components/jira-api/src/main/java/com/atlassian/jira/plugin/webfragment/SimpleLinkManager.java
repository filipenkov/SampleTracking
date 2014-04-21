package com.atlassian.jira.plugin.webfragment;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.util.NotNull;

import java.util.List;

/**
 * A manager that looks after generating lists of {@link com.atlassian.jira.plugin.webfragment.model.SimpleLink} and
 * {@link com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection}.
 *
 * @since v4.0
 */
public interface SimpleLinkManager
{
    /**
     * This determines whether a location should be loaded lazily if possible.
     *
     * @param location   The location to check for
     * @param remoteUser The user that we are generating the list for
     * @param jiraHelper The context under which the list is being generated
     * @return true if the loaction should be loaded lazily if possible, false otherwise
     */
    boolean shouldLocationBeLazy(String location, User remoteUser, JiraHelper jiraHelper);

    /**
     * This determines whether an individual section should be loaded lazily if possible.
     *
     * @param section The section to check for
     * @return true if the section should be loaded lazily if possible, false otherwise
     */
    boolean shouldSectionBeLazy(String section);

    /**
     * Gets a list of {@link com.atlassian.jira.plugin.webfragment.model.SimpleLink} for the given section.
     *
     * @param section    The section to generate the list for
     * @param remoteUser The user that we are generating the list for
     * @param jiraHelper The context under which the list is being generated
     * @return The list of links for the given section
     */
    @NotNull
    List<SimpleLink> getLinksForSection(@NotNull String section, User remoteUser, @NotNull JiraHelper jiraHelper);


    /**
     * Gets a list of {@link com.atlassian.jira.plugin.webfragment.model.SimpleLink} for the given section, without
     * filtering links using the conditions specified for the links.  This will effectively return a list of links
     * without running any security checks.
     *
     * @param section    The section to generate the list for
     * @param remoteUser The user that we are generating the list for
     * @param jiraHelper The context under which the list is being generated
     * @return The list of links for the given section
     */
    @NotNull
    List<SimpleLink> getLinksForSectionIgnoreConditions(@NotNull String section, User remoteUser, @NotNull JiraHelper jiraHelper);

    /**
     * Gets a list of {@link com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection} for the given location.
     *
     * @param location   The location to generate the list for
     * @param remoteUser The user that we are generating the list for
     * @param jiraHelper The context under which the list is being generated
     * @return The list of sections for the give location
     */
    @NotNull
    List<SimpleLinkSection> getSectionsForLocation(@NotNull String location, User remoteUser, @NotNull JiraHelper jiraHelper);

    /**
     * Recursively search through our sections within sections within sections within ...
     * To find a section that contains a link that matches this URL
     *
     * @param URL           The URL for the action e.g. https://jdog.atlassian.com/secure/project/ViewProjects.jspa. We check if this URL contains a web-item's path which is usually something shorter like /secure/project/ViewProjects.jspa
     * @param topLevelSection The top level section from where to start searching!
     * @param remoteUser The user that we are generating the list for
     * @param jiraHelper The context under which the list is being generated
     * @return              Returns null if no section found
     */
    SimpleLinkSection getSectionForURL (@NotNull String topLevelSection, @NotNull String URL, User remoteUser, JiraHelper jiraHelper);

    /**
     * Gets a list of {@link com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection} for the given location, that have either web-items or
     * other web-sections within them.
     *
     * @param location   The location to generate the list for
     * @param remoteUser The user that we are generating the list for
     * @param jiraHelper The context under which the list is being generated
     * @return The list of sections for the give location
     */
    @NotNull
    List<SimpleLinkSection> getNotEmptySectionsForLocation(@NotNull String location, User remoteUser, @NotNull JiraHelper jiraHelper);
}
