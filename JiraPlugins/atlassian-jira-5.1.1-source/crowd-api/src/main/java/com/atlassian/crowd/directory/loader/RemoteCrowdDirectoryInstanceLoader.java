package com.atlassian.crowd.directory.loader;

/**
 * Marker interface for the RemoteCrowdDirectory instance loader.
 *
 * Required because the Pico container in JIRA only allows one implementation of
 * an interface to exist.
 */
public interface RemoteCrowdDirectoryInstanceLoader extends DirectoryInstanceLoader
{
}
