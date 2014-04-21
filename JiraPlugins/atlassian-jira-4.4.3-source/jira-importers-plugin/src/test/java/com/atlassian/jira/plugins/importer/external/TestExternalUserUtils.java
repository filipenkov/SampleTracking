/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.external;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableGroup;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.exception.AddException;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestExternalUserUtils {

	private ExternalUserUtils utils;

	@Mock
	private GroupManager groupMock;
	@Mock
	private UserUtil userMock;
	@Mock
	private GlobalPermissionManager globalMock;
	@Mock
	private ProjectRoleService projectRoleService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		utils = new ExternalUserUtils(userMock, groupMock, globalMock, projectRoleService);
	}

	/**
	 * Test case for https://studio.plugins.atlassian.com/browse/JIM-3 / http://jira.atlassian.com/browse/JRA-14864
	 */
	@Test
	public void testCreateUserGroups() throws PermissionException, AddException, CreateException {
		final String name = "pniewiadomski";
		final String email = "pniewiadomski@atlassian.com";
		final String groupName = "groupName";

		final User user = mock(User.class);

		when(userMock.createUserNoNotification(name, null, email, "fullname")).thenReturn(user);

		List<Group> groups = Lists.<Group>newArrayList(new ImmutableGroup("test"), new ImmutableGroup("test-group"));

		when(globalMock.getGroupsWithPermission(Permissions.USE)).thenReturn(groups);

		when(groupMock.groupExists(groupName)).thenReturn(true);

		when(userMock.canActivateNumberOfUsers(1)).thenReturn(true);

		ExternalUser externalUser = new ExternalUser(name, "fullname", email);
		externalUser.setActive(false);
		externalUser.getGroups().add(groupName);
		utils.createUser(externalUser, mock(ImportLogger.class));

		verify(userMock).createUserNoNotification(name, null, email, "fullname");
		verify(groupMock).groupExists(groupName);
		verify(groupMock).getGroupObject(groupName);
		verify(userMock).addUserToGroup(null, user);
	}

	/**
	 * Test case for https://studio.atlassian.com/browse/JIM-407
	 *
	 * When adding users to a role create the role if it doesn't exist
	 */
	@Test
	public void testCreateRoleIfItDoesntExist() throws Exception {
		final User user = mock(User.class);
		final Project project = mock(Project.class);
		final ProjectRole pr = mock(ProjectRole.class);

		when(projectRoleService.createProjectRole(eq(user), any(ProjectRoleImpl.class), any(SimpleErrorCollection.class))).thenReturn(pr);

		utils.addUsersToProjectRole(user, project, "Developers", Lists.<String>newArrayList());
		verify(projectRoleService).getProjectRoleByName(eq(user), eq("Developers"), any(SimpleErrorCollection.class));
		verify(projectRoleService).addActorsToProjectRole(eq(user), any(Collection.class), eq(pr),
				any(Project.class), eq(UserRoleActorFactory.TYPE), any(SimpleErrorCollection.class));
	}

}
