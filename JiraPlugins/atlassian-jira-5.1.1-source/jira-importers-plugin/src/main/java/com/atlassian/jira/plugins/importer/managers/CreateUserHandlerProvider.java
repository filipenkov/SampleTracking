package com.atlassian.jira.plugins.importer.managers;

import javax.annotation.Nullable;

public interface CreateUserHandlerProvider {
	@Nullable
    CreateUserHandler getHandler();
}
