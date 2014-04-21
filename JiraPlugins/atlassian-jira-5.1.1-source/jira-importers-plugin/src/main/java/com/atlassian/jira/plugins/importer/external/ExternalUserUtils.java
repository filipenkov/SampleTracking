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
import com.atlassian.jira.plugins.importer.Immutables;
import com.atlassian.jira.plugins.importer.external.beans.ExternalUser;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.managers.CreateUserHandler;
import com.atlassian.jira.plugins.importer.managers.CreateUserHandlerProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.base.Function;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ExternalUserUtils {

	private final UserUtil userUtil;
	private final GroupManager groupManager;
	private final GlobalPermissionManager globalPermissionManager;
	private final ProjectRoleService projectRoleService;
    private final CreateUserHandlerProvider createUserHandlerProvider;

	public ExternalUserUtils(UserUtil userUtil, GroupManager groupManager,
			GlobalPermissionManager globalPermissionManager,
			ProjectRoleService projectRoleService, CreateUserHandlerProvider createUserHandlerProvider) {
		this.userUtil = userUtil;
		this.groupManager = groupManager;
		this.globalPermissionManager = globalPermissionManager;
		this.projectRoleService = projectRoleService;
        this.createUserHandlerProvider = createUserHandlerProvider;
    }

	@Nullable
	public User createUser(final ExternalUser externalUser, ImportLogger log) {
		try {
			final User user;
            final CreateUserHandler createUserHandler = createUserHandlerProvider.getHandler();
            if (createUserHandler != null) { // expected in OnDemand
				user = createUserHandler.createUserNoNotification(externalUser.getName(), externalUser.getPassword(),
							externalUser.getEmail(), externalUser.getFullname(), log);
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
			log.warn(e, "Problems encoutered while creating User %s", externalUser);
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
                final ProjectRoleActors actors = projectRoleService.getProjectRoleActors(user, developersRole, jiraProject, errorCollection);
                if (!errorCollection.hasAnyErrors() && actors != null) {
                    final List<String> usersInRole = Immutables.transformThenCopyToList(actors.getUsers(), new Function<User, String>() {
                        @Override
                        public String apply(@Nullable User input) {
                            return input.getName();
                        }
                    });
                    final Set<String> usersToAdd = Sets.newHashSet(users);
                    usersToAdd.removeAll(usersInRole);

                    if (!usersToAdd.isEmpty()) {
                        projectRoleService.addActorsToProjectRole(user,
                            usersToAdd, developersRole, jiraProject, UserRoleActorFactory.TYPE, errorCollection);
                        if (!errorCollection.hasAnyErrors()) {
                            return;
                        }
                    } else {
                        return;
                    }
                }
			}
		}

		throw new Exception(String.format("Failed to add users to '%s' role for the project '%s': %s", roleName, jiraProject.getKey(),
				errorCollection.toString()));
	}

}
