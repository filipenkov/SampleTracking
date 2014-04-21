package com.atlassian.jira.plugin.ext.bamboo.service;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.project.Project;
import com.atlassian.sal.api.net.ResponseException;

import java.io.IOException;
import java.util.Map;

/**
 * Gateway to access various parts of Bamboo
 */
public interface BambooServerAccessor
{
    String getHtmlFromAction(String bambooAction, Project project, Map extraParams) throws IOException, ResponseException, CredentialsRequiredException;
    String getHtmlFromAction(String bambooAction, Project project, Iterable<String> issueKeys, Map extraParams) throws IOException, ResponseException, CredentialsRequiredException;
}
