/*
 * Copyright (C) 2002-2010 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.external;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.plugins.importer.external.beans.ExternalAttachment;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.AttachmentException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.ofbiz.core.entity.GenericEntityException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestExternalUtils {

	@Mock
	private ImportLogger log;
	@Mock
	private UserProvider userProvider;

	@Before
	public void initMocks() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 *
	 */
	@Test
	public void testAttachmentDescriptionCreatesComment()
			throws ExternalException, IOException, AttachmentException, GenericEntityException {
		JiraAuthenticationContext mockAuthentication = mock(JiraAuthenticationContext.class);
		AttachmentManager mockAttachment = mock(AttachmentManager.class);
		I18nHelper mockHelper = mock(I18nHelper.class);
		PermissionManager mockPermission = mock(PermissionManager.class);
		CommentManager mockComment = mock(CommentManager.class);
		ProjectManager mockProject = mock(ProjectManager.class);

		ExternalUtils utils = new ExternalUtilsBuilder()
				.setAuthenticationContext(mockAuthentication)
				.setAttachmentManager(mockAttachment)
				.setPermissionManager(mockPermission)
				.setCommentManager(mockComment)
				.setProjectManager(mockProject)
				.createExternalUtils();

		final String name = "pniewiadomski";
		final User user = new ImmutableUser(1, name, null, null, true);
		final User attacher = new ImmutableUser(1, "admin", null, null, true);
		final Project project = mock(Project.class);

		Date date = new Date();
		File tempFile = File.createTempFile("test", ".txt");
		tempFile.deleteOnExit();
		ExternalAttachment attachment = new ExternalAttachment("test.txt", tempFile, date);
		attachment.setAttacher(attacher.getName());
		attachment.setDescription("Tis a dezcipton.");

		MutableIssue issue = mock(MutableIssue.class);

		when(mockAuthentication.getLoggedInUser()).thenReturn(user);
		when(userProvider.getUser(user.getName())).thenReturn(user);
		when(userProvider.getUser(attacher.getName())).thenReturn(attacher);
		when(mockAuthentication.getI18nHelper()).thenReturn(mockHelper);
		when(mockPermission.hasPermission(Permissions.COMMENT_ISSUE, issue, attacher)).thenReturn(true);
		when(mockProject.getProjectObj(0l)).thenReturn(project);

		utils.attachFile(userProvider, attachment, issue, log);

		verify(mockAttachment).createAttachment(attachment.getAttachment(), attachment.getName(),
				ExternalUtils.GENERIC_CONTENT_TYPE, attacher, issue.getGenericValue(), Collections.EMPTY_MAP,
				attachment.getCreated().toDate());

		verify(mockHelper).getText("jira-importer-plugins.external.utils.attachment.description",
				attachment.getName(), attachment.getDescription());

		verify(mockComment).create(issue, attacher.getName(), attacher.getName(), null, null, null, date, date, false, true);
	}

}
