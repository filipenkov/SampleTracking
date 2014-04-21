package com.atlassian.crowd.directory.loader;

/**
 * Loader for directories that work by caching/mirroring some remote directory in the internal repository.
 *
 * Required because the Pico container in JIRA only allows one implementation of
 * an interface to exist.
 */
public interface InternalHybridDirectoryInstanceLoader extends DirectoryInstanceLoader
{
}