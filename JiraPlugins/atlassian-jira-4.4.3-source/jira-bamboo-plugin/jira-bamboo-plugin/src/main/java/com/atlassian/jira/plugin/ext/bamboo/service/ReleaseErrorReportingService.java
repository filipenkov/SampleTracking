package com.atlassian.jira.plugin.ext.bamboo.service;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ReleaseErrorReportingService
{
    /**
     * Records a release error message against the version.
     * @param projectKey, the version is part of.
     * @param versionId, the version to store the error against
     * @param error string.
     */
    void recordError(@NotNull String projectKey, long versionId, @NotNull String error);

    /**
     * Records a release error message against the version.
     * @param projectKey, the version is part of.
     * @param versionId, the version to store the error against
     * @param errors string.
     */
    void recordErrors(@NotNull String projectKey, long versionId, @NotNull List<String> errors);

    /**
     * Retrieves any release errors recorded against the version
     * @param projectKey the version belongs to
     * @param versionId errors are recorded against
     * @return a list of any errors stored against the version.
     */
    @NotNull
    List<String> getErrors(@NotNull String projectKey, long versionId);

    /**
     * Removes all errors recorded against the version
     * @param projectKey the version belongs to
     * @param versionId of the version to clear release errors from
     */
    void clearErrors(@NotNull String projectKey, long versionId);

}
