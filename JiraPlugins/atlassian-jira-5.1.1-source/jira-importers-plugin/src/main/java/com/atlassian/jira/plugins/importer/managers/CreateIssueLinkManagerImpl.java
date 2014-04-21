/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.importer.managers;

import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkCreator;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.google.common.collect.Maps;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Map;

public class CreateIssueLinkManagerImpl implements CreateIssueLinkManager {
	private final IssueLinkManager issueLinkManager;
	private final IssueIndexManager issueIndexManager;
	private final IssueLinkCreator issueLinkCreator;
	private final OfBizDelegator delegator;

	private static final String LINK_TYPE_ID_FIELD_NAME = "linktype";
	private static final String SOURCE_ID_FIELD_NAME = "source";
	private static final String DESTINATION_ID_LINK_NAME = "destination";
	private static final String SEQUENCE_FIELD_NAME = "sequence";

	public CreateIssueLinkManagerImpl(IssueLinkManager issueLinkManager, IssueIndexManager issueIndexManager,
			IssueLinkCreator issueLinkCreator, OfBizDelegator delegator) {
		this.issueLinkManager = issueLinkManager;
		this.issueIndexManager = issueIndexManager;
		this.issueLinkCreator = issueLinkCreator;
		this.delegator = delegator;
	}

	public void createIssueLink(Long sourceId, Long destinationId, Long issueLinkTypeId, @Nullable Long sequence)
			throws CreateException {
		//if the link is already created, then don't do anything
		if (issueLinkManager.getIssueLink(sourceId, destinationId, issueLinkTypeId) != null)
			return;

		IssueLink issueLink = null;
		try {
			issueLink = storeIssueLink(sourceId, destinationId, issueLinkTypeId, sequence);
		}
		finally {
			// Clear the cache before we reindex - Plugin developers may add link info to the index,
			// and we don't want to serve them stale cache data. See JRA-16199.
			issueLinkManager.clearCache();
			// We always need to reindex linked Issues - the updated date of both issues is updated. see JRA-7156
			if (issueLink != null) {
				reindexLinkedIssues(issueLink);
			}
		}
	}

	protected IssueLink storeIssueLink(Long sourceId, Long destinationId, Long issueLinkTypeId, Long sequence) {
		// create the outward link from issue -> destination
		try {
			Map<String, Object> map = Maps.newHashMap();
			map.put(SOURCE_ID_FIELD_NAME, sourceId);
			map.put(DESTINATION_ID_LINK_NAME, destinationId);
			map.put(LINK_TYPE_ID_FIELD_NAME, issueLinkTypeId);
			map.put(SEQUENCE_FIELD_NAME, sequence);
			return issueLinkCreator.createIssueLink(delegator.createValue(OfBizDelegator.ISSUE_LINK, map));
		}
		finally {
			issueLinkManager.clearCache();
		}
	}

	protected void reindexLinkedIssues(IssueLink issueLink) {
		try {
			issueIndexManager.reIndex(issueLink.getSourceObject());
			issueIndexManager.reIndex(issueLink.getDestinationObject());
		}
		catch (IndexException e) {
			throw new NestableRuntimeException(e);
		}
	}
}