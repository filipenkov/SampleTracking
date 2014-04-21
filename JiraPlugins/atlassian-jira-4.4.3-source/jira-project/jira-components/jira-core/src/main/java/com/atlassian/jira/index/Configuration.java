package com.atlassian.jira.index;

import com.atlassian.jira.config.util.IndexWriterConfiguration;
import com.atlassian.jira.util.NotNull;
import net.jcip.annotations.Immutable;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;

/**
 * The configuration for a particular index and how it should be written.
 * 
 * @since v4.0
 */
@Immutable
public interface Configuration
{
    @NotNull
    Directory getDirectory();

    @NotNull
    Analyzer getAnalyzer();

    @NotNull
    IndexWriterConfiguration.WriterSettings getWriterSettings(Index.UpdateMode mode);
}
