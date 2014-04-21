package com.atlassian.upm;

import java.io.File;
import java.net.URI;

import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.upm.api.util.Option;

/**
 * Service for downloading plugins
 */
public interface PluginDownloadService
{
    /**
     * Download the plugin at the given URI
     *
     * @param uri The uri to download from
     * @param username The username to use to download
     * @param password The password to use to download
     * @param progressTracker
     * @return The downloaded plugin
     * @throws com.atlassian.sal.api.net.ResponseException If an error occurred
     * @throws AccessDeniedException if authorization is needed but not supplied
     * @throws UnsupportedProtocolException if URI scheme is not supported
     * @throws RelativeURIException if URI is not absolute
     */
    File downloadPlugin(final URI uri, final String username, final String password, ProgressTracker progressTracker)
        throws ResponseException;

    /**
     * A callback that can be used by clients to track the progress of downloads.
     */
    interface ProgressTracker
    {
        void notify(Progress progress);

        void redirectedTo(URI newUri);
    }

    ProgressTracker NULL_TRACKER = new ProgressTracker()
    {
        public void notify(Progress p)
        {
        }

        public void redirectedTo(URI newUri)
        {
        }
    };

    final class Progress
    {
        private final long amountDownloaded;
        private final Long totalSize;
        private final Option<String> source;

        public Progress(long amountDownloaded, Long totalSize, Option<String> source)
        {
            this.amountDownloaded = amountDownloaded;
            this.totalSize = totalSize;
            this.source = source;
        }

        public long getAmountDownloaded()
        {
            return amountDownloaded;
        }

        public Long getTotalSize()
        {
            return totalSize;
        }

        public Option<String> getSource()
        {
            return source;
        }
        
        @Override
        public String toString()
        {
            if (totalSize != null)
            {
                return amountDownloaded + "/" + totalSize + "(" + Math.round((double) amountDownloaded / totalSize * 10) * 10 + "%)";
            }
            else
            {
                return Long.toString(amountDownloaded);
            }
        }
    }
}
