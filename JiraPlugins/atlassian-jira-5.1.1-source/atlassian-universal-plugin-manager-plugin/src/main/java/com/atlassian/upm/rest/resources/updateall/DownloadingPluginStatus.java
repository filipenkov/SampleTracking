package com.atlassian.upm.rest.resources.updateall;

import java.net.URI;
import java.net.URISyntaxException;

import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.upm.PluginDownloadService.Progress;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

final class DownloadingPluginStatus extends UpdateStatus
{
    @JsonProperty private final String name;
    @JsonProperty private final String version;
    @JsonProperty private final URI uri;
    @JsonProperty private final long amountDownloaded;
    @JsonProperty private final Long totalSize;
    @JsonProperty private final Integer numberComplete;
    @JsonProperty private final Integer totalUpdates;

    @JsonCreator
    public DownloadingPluginStatus(@JsonProperty("name") String name,
        @JsonProperty("version") String version,
        @JsonProperty("uri") URI uri,
        @JsonProperty("amountDownloaded") long amountDownloaded,
        @JsonProperty("totalSize") Long totalSize,
        @JsonProperty("numberComplete") Integer numberComplete,
        @JsonProperty("totalUpdates") Integer totalUpdates)
    {
        super(State.DOWNLOADING);
        this.name = name;
        this.version = version;
        this.uri = uri;
        this.amountDownloaded = amountDownloaded;
        this.totalSize = totalSize;
        this.numberComplete = numberComplete;
        this.totalUpdates = totalUpdates;
    }

    DownloadingPluginStatus(PluginVersion pluginVersion, int numberComplete, int totalUpdates) throws URISyntaxException
    {
        this(pluginVersion.getPlugin().getName(), pluginVersion.getVersion(), new URI(pluginVersion.getBinaryUrl().trim()), 0, null, numberComplete, totalUpdates);
    }

    DownloadingPluginStatus(PluginVersion pluginVersion, URI redirectedUri, int numberComplete, int totalUpdates)
    {
        this(pluginVersion.getPlugin().getName(), pluginVersion.getVersion(), redirectedUri, 0, null, numberComplete, totalUpdates);
    }

    DownloadingPluginStatus(PluginVersion pluginVersion, Progress progress, int numberComplete, int totalUpdates) throws URISyntaxException
    {
        this(
            pluginVersion.getPlugin().getName(),
            pluginVersion.getVersion(),
            new URI(pluginVersion.getBinaryUrl().trim()),
            progress.getAmountDownloaded(),
            progress.getTotalSize(),
            numberComplete,
            totalUpdates
        );
    }

    public String getVersion()
    {
        return version;
    }

    public String getName()
    {
        return name;
    }

    public URI getUri()
    {
        return uri;
    }

    public long getAmountDownloaded()
    {
        return amountDownloaded;
    }

    public Long getTotalSize()
    {
        return totalSize;
    }

    public int getNumberComplete()
    {
        return numberComplete;
    }

    public int getTotalUpdates()
    {
        return totalUpdates;
    }
}