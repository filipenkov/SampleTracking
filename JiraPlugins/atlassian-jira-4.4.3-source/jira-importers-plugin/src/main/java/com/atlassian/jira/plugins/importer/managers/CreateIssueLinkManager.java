/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.managers;

import com.atlassian.jira.exception.CreateException;

import javax.annotation.Nullable;

public interface CreateIssueLinkManager {

    void createIssueLink(Long sourceId, Long destinationId, Long issueLinkTypeId, @Nullable Long sequence) throws CreateException;

}
