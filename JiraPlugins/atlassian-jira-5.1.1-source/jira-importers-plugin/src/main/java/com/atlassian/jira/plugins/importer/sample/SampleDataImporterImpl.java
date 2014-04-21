package com.atlassian.jira.plugins.importer.sample;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.plugins.importer.external.beans.*;
import com.atlassian.jira.plugins.importer.imports.importer.ImportDataBean;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporter;
import com.atlassian.jira.plugins.importer.imports.importer.JiraDataImporterFactory;
import com.atlassian.jira.plugins.importer.imports.importer.impl.ImporterCallable;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.module.SimpleModule;
import org.joda.time.DateTime;
import org.joda.time.Period;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class SampleDataImporterImpl implements SampleDataImporter {

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final JiraDataImporterFactory jiraDataImporterFactory;
    private final ConstantsManager constantsManager;
    private final TemplateRenderer templateRenderer;

    public SampleDataImporterImpl(JiraAuthenticationContext jiraAuthenticationContext, JiraDataImporterFactory jiraDataImporterFactory,
                                  ConstantsManager constantsManager, TemplateRenderer templateRenderer) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.jiraDataImporterFactory = jiraDataImporterFactory;
        this.constantsManager = constantsManager;
        this.templateRenderer = templateRenderer;
    }

    public static ObjectMapper getObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        SimpleModule module = new SimpleModule("Sample Data Module", Version.unknownVersion())
                .addDeserializer(DateTime.class, new PeriodAwareDateDeserializer())
                .addDeserializer(Period.class, new PeriodDeserializer())
                .addSerializer(new PeriodSerializer());
        om.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        om.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        om.registerModule(module);
        return om;
    }

    public SampleData parseSampleData(@Nonnull String json) {
        ObjectMapper om = getObjectMapper();
        try {
            return om.readValue(json, SampleData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createSampleData(@Nonnull String json, Map<String, Object> context, @Nullable Callbacks callbacks, @Nullable AttachmentsProvider attachmentsProvider, @Nonnull ErrorCollection errors) {
        createSampleData(parseSampleData(templateRenderer.renderFragment(json, context)), callbacks, attachmentsProvider, errors);
    }

    @Override
    public void createSampleData(@Nonnull SampleData sampleData, @Nullable Callbacks callbacks, @Nullable AttachmentsProvider attachmentsProvider, @Nonnull ErrorCollection errors) {
        final JiraDataImporter importer = jiraDataImporterFactory.create();
        importer.setRunning();
        importer.initializeLog();

        try {
            final ImportDataBean dataBean = createDataBean(sampleData, callbacks, attachmentsProvider);
            importer.setDataBean(dataBean);

            new ImporterCallable(importer, jiraAuthenticationContext.getLoggedInUser()).call();

            if (importer.getStats() != null && !importer.getStats().getFailures().isEmpty()) {
                errors.addErrorMessages(importer.getStats().getFailures());
            }
        } catch(Exception e) {
            ImportLogger log = importer.getLog();
            if(log != null) {
                log.fail(e, "Failed to start import");
            }
        }
    }

    public ImportDataBean createDataBean(@Nonnull final SampleData sampleData, @Nullable final Callbacks callbacks,
                                          @Nullable final AttachmentsProvider attachmentsProvider) {
        return new ImportDataBean() {

            @Override
            public Set<ExternalUser> getRequiredUsers(Collection<ExternalProject> projects, ImportLogger importLogger) {
                return this.getAllUsers(importLogger);
            }

            @Override
            public Set<ExternalUser> getAllUsers(ImportLogger log) {
                return sampleData.getUsers();
            }

            @Override
            public Set<ExternalProject> getAllProjects(ImportLogger log) {
                return sampleData.getProjects();
            }

            @Override
            public Set<ExternalProject> getSelectedProjects(ImportLogger log) {
                return this.getAllProjects(log);
            }

            @Override
            public Collection<ExternalVersion> getVersions(ExternalProject externalProject, ImportLogger importLogger) {
                Set<ExternalVersion> versions = externalProject.getVersions();
                return versions != null ? versions : Collections.<ExternalVersion>emptySet();
            }

            @Override
            public Collection<ExternalComponent> getComponents(ExternalProject externalProject, ImportLogger importLogger) {
                Set<ExternalComponent> components = externalProject.getComponents();
                return components != null ? components : Collections.<ExternalComponent>emptySet();
            }

            @Override
            public Iterator<ExternalIssue> getIssuesIterator(ExternalProject externalProject, ImportLogger importLogger) {
                List<ExternalIssue> issues = externalProject.getIssues();
                return Iterables.transform((issues != null ? issues : Collections.<ExternalIssue>emptyList()), new Function<ExternalIssue, ExternalIssue>() {
                    @Override
                    public ExternalIssue apply(@Nullable ExternalIssue input) {
                        ExternalIssue ei = new ExternalIssue(input);
                        if (ei.getStatus() != null) {
                            ei.setStatus(constantsManager.getIssueConstantByName(ConstantsManager.STATUS_CONSTANT_TYPE, ei.getStatus()).getId());
                        }
                        return ei;
                    }
                }).iterator();
            }

            @Override
            public Collection<ExternalAttachment> getAttachmentsForIssue(ExternalIssue externalIssue, ImportLogger log) {
                if (attachmentsProvider != null) {
                    for(ExternalAttachment attachment : externalIssue.getAttachments()) {
                        attachment.setAttachment(attachmentsProvider.getAttachmentForIssue(externalIssue, attachment.getName()));
                    }
                    return externalIssue.getAttachments();
                }
                return Collections.emptyList();
            }

            @Override
            public void cleanUp() {
            }

            @Override
            public String getIssueKeyRegex() {
                return null;
            }

            @Override
            public Collection<ExternalLink> getLinks(ImportLogger log) {
                return sampleData.getLinks();
            }

            @Override
            public long getTotalIssues(Set<ExternalProject> selectedProjects, ImportLogger log) {
                long count = 0;
                for(ExternalProject project : selectedProjects) {
                    List<ExternalIssue> issues = project.getIssues();
                    if (issues != null) {
                        count += issues.size();
                    }
                }
                return count;
            }

            @Nonnull
            @Override
            public Callbacks getCallbacks() {
                return callbacks != null ? callbacks : super.getCallbacks();
            }

            @Override
            public String getUnusedUsersGroup() {
                return null;
            }

            @Override
            public String getReturnLinks() {
                return null;
            }
        };
    }

}
