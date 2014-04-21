package com.atlassian.jira.plugins.importer.sample;

import com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean;
import com.atlassian.jira.util.ErrorCollection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public interface SampleDataImporter {

    /**
     * @param json velocity template producing json
     * @param context velocity context to be used when rendering the template
     */
    void createSampleData(@Nonnull String json, @Nonnull Map<String, Object> context, @Nullable Callbacks callbacks,
                          @Nullable AttachmentsProvider attachmentsProvider, @Nonnull ErrorCollection errors);

    /**
     *
     * @param json json encoded model of {@link SampleData}
     * @return
     */
    SampleData parseSampleData(@Nonnull String json);

    void createSampleData(@Nonnull SampleData projects, @Nullable Callbacks callbacks,
                          @Nullable AttachmentsProvider attachmentsProvider, @Nonnull ErrorCollection errors);

    ImportDataBean createDataBean(@Nonnull final SampleData sampleData, @Nullable final Callbacks callbacks,
                                         @Nullable final AttachmentsProvider attachmentsProvider);
}
