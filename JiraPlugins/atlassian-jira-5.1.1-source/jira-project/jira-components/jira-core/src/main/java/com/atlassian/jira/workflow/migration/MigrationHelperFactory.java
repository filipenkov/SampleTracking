package com.atlassian.jira.workflow.migration;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * Provides a way to create instances of {@link WorkflowMigrationHelper}.
 * @since v5.1
 */
public interface MigrationHelperFactory
{
    /**
     * Create an instacne of a {@link WorkflowMigrationHelper}.
     *
     * @param project the project to migration. Can't be null.
     * @param scheme the scheme to migrate to. Can't be null.
     * @return the created helper.
     * @throws GenericEntityException if DB errors.
     */
    WorkflowMigrationHelper createMigrationHelper(GenericValue project, GenericValue scheme)
            throws GenericEntityException;
}
