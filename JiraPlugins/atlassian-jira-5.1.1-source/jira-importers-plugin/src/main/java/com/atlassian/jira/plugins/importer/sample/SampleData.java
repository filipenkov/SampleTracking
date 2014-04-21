package com.atlassian.jira.plugins.importer.sample;

import com.atlassian.jira.plugins.importer.external.beans.ExternalLink;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public class SampleData {

    private final Set<ExternalUser> users;
    private final Set<ExternalProject> projects;
    private final Set<ExternalLink> links;

    @JsonCreator
    public SampleData(@JsonProperty(value = "links") @Nullable Set<ExternalLink> links, @JsonProperty(value = "projects") @Nullable Set<ExternalProject> projects,
                      @JsonProperty(value = "users") @Nullable Set<ExternalUser> users) {
        this.links = links;
        this.projects = projects;
        this.users = users;
    }

    @Nonnull
    public Set<ExternalUser> getUsers() {
        return users == null ? Collections.<ExternalUser>emptySet() : users;
    }

    @Nonnull
    public Set<ExternalProject> getProjects() {
        return projects == null ? Collections.<ExternalProject>emptySet() : projects;
    }

    @Nonnull
    public Set<ExternalLink> getLinks() {
        return links == null ? Collections.<ExternalLink>emptySet() : links;
    }
}
