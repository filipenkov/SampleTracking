package com.atlassian.jira.plugins.importer.sample;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.jira.plugins.importer.external.beans.ExternalIssue;

import javax.annotation.Nonnull;
import java.io.File;

@ExperimentalApi
public interface AttachmentsProvider {

    @Nonnull
    public File getAttachmentForIssue(ExternalIssue externalIssue, String name);

}
