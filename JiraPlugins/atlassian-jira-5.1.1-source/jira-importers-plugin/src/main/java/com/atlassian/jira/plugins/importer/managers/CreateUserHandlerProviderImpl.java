package com.atlassian.jira.plugins.importer.managers;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Set;

public class CreateUserHandlerProviderImpl implements CreateUserHandlerProvider {
    private final Set<CreateUserHandler> createUserHandlers;

    public CreateUserHandlerProviderImpl(Set<CreateUserHandler> createUserHandlers) {
        this.createUserHandlers = createUserHandlers;
    }

    @Nullable
	@Override
    public CreateUserHandler getHandler() {
        final Iterator<CreateUserHandler> iterator = createUserHandlers.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }
}
