package com.atlassian.upm.rest.representations;

import java.net.URI;
import java.util.List;
import java.util.Map;

import com.atlassian.upm.Change;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.spi.Plugin;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.rest.representations.RepresentationLinks.PLUGIN_ICON_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.PLUGIN_LOGO_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.SELF_REL;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

public class ChangesRequiringRestartRepresentation
{
    @JsonProperty private final Map<String, URI> links;
    @JsonProperty private final List<ChangeRepresentation> changes;

    @JsonCreator
    public ChangesRequiringRestartRepresentation(@JsonProperty("links") Map<String, URI> links,
        @JsonProperty("changes") List<ChangeRepresentation> changes)
    {
        this.links = ImmutableMap.copyOf(links);
        this.changes = ImmutableList.copyOf(changes);
    }

    ChangesRequiringRestartRepresentation(Iterable<Change> restartChanges, final UpmUriBuilder uriBuilder,
        final LinkBuilder linkBuilder)
    {
        this.links = linkBuilder.buildLinksFor(uriBuilder.buildChangesRequiringRestartUri(), false).build();

        Function<Change, ChangeRepresentation> toChangeRepresentation = new Function<Change, ChangeRepresentation>()
        {
            public ChangeRepresentation apply(Change change)
            {
                return new ChangeRepresentation(change, uriBuilder);
            }
        };
        changes = ImmutableList.copyOf(filter(transform(restartChanges, toChangeRepresentation), notNull()));
    }

    public URI getSelf()
    {
        return links.get(SELF_REL);
    }

    public List<ChangeRepresentation> getChanges()
    {
        return changes;
    }

    public static final class ChangeRepresentation
    {
        @JsonProperty private final Map<String, URI> links;
        @JsonProperty private final String name;
        @JsonProperty private final String key;
        @JsonProperty private final String action;

        @JsonCreator
        public ChangeRepresentation(@JsonProperty("name") String name,
            @JsonProperty("key") String key,
            @JsonProperty("action") String action,
            @JsonProperty("links") Map<String, URI> links)
        {
            this.name = name;
            this.key = key;
            this.action = action;
            this.links = ImmutableMap.copyOf(links);
        }

        public ChangeRepresentation(Change restartChange, UpmUriBuilder uriBuilder)
        {
            Plugin plugin = restartChange.getPlugin();
            this.name = plugin.getName();
            this.key = plugin.getKey();
            this.action = restartChange.getAction();
            this.links = ImmutableMap.of(SELF_REL, uriBuilder.buildChangeRequiringRestart(plugin.getKey()),
                                         PLUGIN_ICON_REL, uriBuilder.buildPluginIconLocationUri(plugin.getKey()),
                                         PLUGIN_LOGO_REL, uriBuilder.buildPluginLogoLocationUri(plugin.getKey()));
        }

        public URI getSelf()
        {
            return links.get(SELF_REL);
        }

        public String getName()
        {
            return name;
        }

        public String getKey()
        {
            return key;
        }

        public String getAction()
        {
            return action;
        }

        public URI getPluginIconLink()
        {
            return links.get(PLUGIN_ICON_REL);
        }

        public URI getPluginLogoLink()
        {
            return links.get(PLUGIN_LOGO_REL);
        }
    }
}
