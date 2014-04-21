package com.atlassian.upm.rest.resources.install;

import java.net.URI;

import javax.ws.rs.core.Response.Status;

import com.atlassian.upm.PluginDownloadService.Progress;
import com.atlassian.upm.rest.async.TaskStatus;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.rest.MediaTypes.INSTALL_NEXT_TASK_JSON;
import static com.atlassian.upm.rest.MediaTypes.INSTALL_COMPLETE_JSON;
import static com.atlassian.upm.rest.MediaTypes.INSTALL_DOWNLOADING_JSON;
import static com.atlassian.upm.rest.MediaTypes.INSTALL_ERR_JSON;
import static com.atlassian.upm.rest.MediaTypes.INSTALL_INSTALLING_JSON;

public class InstallStatus extends TaskStatus
{
    @JsonProperty private final String source;
    @JsonProperty private final String filename;
    @JsonProperty private final URI nextTaskPostUri;

    private InstallStatus(InstallStatus.State state, String source, URI nextTaskPostUri, int statusCode)
    {
        super(state.isDone(), state.getContentType(), statusCode);
        this.source = source;
        this.nextTaskPostUri = nextTaskPostUri;

        if (source != null)
        {
            int lastSlash = source.lastIndexOf("/") + 1;
            if (source.length() > lastSlash)
            {
                this.filename = source.substring(lastSlash);
            }
            else
            {
                this.filename = null;
            }
        }
        else
        {
            this.filename = null;
        }
    }

    private InstallStatus(InstallStatus.State state, String source)
    {
        this(state, source, null, Status.OK.getStatusCode());
    }
    
    public String getSource()
    {
        return source;
    }

    public String getFilename()
    {
        return filename;
    }

    public URI getNextTaskPostUri()
    {
        return nextTaskPostUri;
    }
    
    public static InstallStatus downloading(URI uri)
    {
        return new DownloadStatus(uri);
    }

    public static InstallStatus downloading(URI uri, Progress progress)
    {
        return new DownloadStatus(uri, progress);
    }

    public static InstallStatus installing(String source)
    {
        return new InstallStatus(State.INSTALLING, source);
    }

    public static InstallStatus installing(URI source)
    {
        return new InstallStatus(State.INSTALLING, source.toASCIIString());
    }

    public static InstallStatus complete(String source)
    {
        return new InstallStatus(State.COMPLETE, source);
    }

    public static InstallStatus complete(URI source)
    {
        return new InstallStatus(State.COMPLETE, source.toASCIIString());
    }

    public static InstallStatus nextTaskPostRequired(String source, URI nextTaskPostUri)
    {
        return new InstallStatus(State.NEXT_TASK, source, nextTaskPostUri, Status.ACCEPTED.getStatusCode());
    }
    
    public static InstallStatus err(String subCode, String fileName)
    {
        return new Err(subCode, fileName);
    }

    public static InstallStatus err(String subCode, URI uri)
    {
        return new Err(subCode, uri.toASCIIString());
    }

    public static final class Err extends InstallStatus
    {
        @JsonProperty private final String subCode;

        @JsonCreator
        public Err(@JsonProperty("subCode") String subCode, @JsonProperty("source") String source)
        {
            super(State.ERR, source);
            this.subCode = subCode;
        }

        public String getSubCode()
        {
            return subCode;
        }
    }

    public static final class DownloadStatus extends InstallStatus
    {
        @JsonProperty private final long amountDownloaded;
        @JsonProperty private final Long totalSize;

        @JsonCreator
        public DownloadStatus(@JsonProperty("source") String source,
            @JsonProperty("amountDownloaded") long amountDownloaded,
            @JsonProperty("totalSize") Long totalSize)
        {
            super(State.DOWNLOADING, source);
            this.amountDownloaded = amountDownloaded;
            this.totalSize = totalSize;
        }

        DownloadStatus(URI uri, Progress progress)
        {
            this(progress.getSource().getOrElse(uri.toASCIIString()), progress.getAmountDownloaded(), progress.getTotalSize());
        }

        DownloadStatus(URI uri)
        {
            this(uri.toASCIIString(), 0, null);
        }

        public Long getTotalSize()
        {
            return totalSize;
        }

        public long getAmountDownloaded()
        {
            return amountDownloaded;
        }
    }

    public enum State
    {
        DOWNLOADING(INSTALL_DOWNLOADING_JSON),
        INSTALLING(INSTALL_INSTALLING_JSON),
        COMPLETE(INSTALL_COMPLETE_JSON),
        NEXT_TASK(INSTALL_NEXT_TASK_JSON),
        ERR(INSTALL_ERR_JSON);

        private final String contentType;

        private State(String contentType)
        {
            this.contentType = contentType;
        }

        public boolean isDone()
        {
            return this == COMPLETE || this == ERR;
        }

        public String getContentType()
        {
            return contentType;
        }
    }
}
