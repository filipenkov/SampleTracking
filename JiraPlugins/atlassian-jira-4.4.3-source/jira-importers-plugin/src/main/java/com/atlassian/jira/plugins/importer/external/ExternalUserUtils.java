/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.external;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.managers.CreateUserHandler;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.SimpleErrorCollection;

import javax.annotation.Nullable;
import java.util.Collection;

public class ExternalUserUtils {

	private final UserUtil userUtil;
	private final GroupManager groupManager;
	private final GlobalPermissionManager globalPermissionManager;
	private final ProjectRoleService projectRoleService;
	private CreateUserHandler createUserHandler;

	public ExternalUserUtils(UserUtil userUtil, GroupManager groupManager,
			GlobalPermissionManager globalPermissionManager,
			ProjectRoleService projectRoleService) {
		this.userUtil = userUtil;
		this.groupManager = groupManager;
		this.globalPermissionManager = globalPermissionManager;
		this.projectRoleService = projectRoleService;
	}

	public void setCreateUserHandler(CreateUserHandler createUserHandler) {
		this.createUserHandler = createUserHandler;
	}

	@Nullable
	public User createUser(final ExternalUser externalUser, ImportLogger log) {
		try {
			final User user;
			if (createUserHandler != null) {
				user = createUserHandler.createUserNoNotification(externalUser.getName(), externalUser.getPassword(),
							externalUser.getEmail(),
							externalUser.getFullname());
			} else {
				user = userUtil.createUserNoNotification(externalUser.getName(), externalUser.getPassword(),
						externalUser.getEmail(),
						externalUser.getFullname());
			}

			for (String groupName : externalUser.getGroups()) {
				final Group group;
				if (groupManager.groupExists(groupName)) {
					group = groupManager.getGroupObject(groupName);
				} else {
					group = groupManager.createGroup(groupName);
				}
				userUtil.addUserToGroup(group, user);
			}
			return user;
		} catch (final Exception e) {
			log.warn(e, "Problems encoutered while creating User " + externalUser);
			return null;
		}
	}

	public void deactivateUser(final User user) throws PermissionException, RemoveException {
		userUtil.removeUserFromGroups(globalPermissionManager.getGroupsWithPermission(Permissions.USE), user);
	}

	public boolean isUserActive(final User user) {
		return globalPermissionManager.hasPermission(Permissions.USE, user);
	}

	public void addUsersToProjectRole(User user, Project jiraProject, String roleName, Collection<String> users)
			throws Exception {
		final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

		ProjectRole developersRole = projectRoleService.getProjectRoleByName(user,
				roleName, errorCollection);

		if (!errorCollection.hasAnyErrors()) {
			if (developersRole == null) {
				developersRole = projectRoleService.createProjectRole(user, new ProjectRoleImpl(roleName, null),
						errorCollection);
			}

			if (developersRole != null && !errorCollection.hasAnyErrors()) {
				projectRoleService.addActorsToProjectRole(user,
					users, developersRole, jiraProject, UserRoleActorFactory.TYPE, errorCollection);
				if (!errorCollection.hasAnyErrors()) {
					return;
				}
			}
		}

		throw new Exception(String.format("Failed to add users to '%s' role for the project '%s': %s", roleName, jiraProject.getKey(),
				errorCollection.toString()));
	}

}
