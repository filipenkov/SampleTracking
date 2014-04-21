package com.atlassian.jira.plugins.upgrade.tasks;

import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.testing.stubs.InMemoryPropertySet;
import com.google.common.collect.ImmutableMap;
import com.opensymphony.module.propertyset.PropertySet;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Map;

import static com.atlassian.jira.plugins.upgrade.tasks.WorkflowDesignerUpgradeTask001.LegacyWorkflowLayoutPropertyKeyMigration;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Responsible for holding the tests for {@link LegacyWorkflowLayoutPropertyKeyMigration}
 *
 * @since v5.1
 */
public class TestLegacyWorkflowLayoutPropertyKeyMigration
{
    @Test
    public void allExistingDraftWorkflowLayoutsShouldBeMigratedToTheNewStorageFormat()
    {
        final InMemoryPropertySet existingWorkflowDesignerProperties =
                Stubs.PropertySets.newBuilder().
                        initialiseWith(
                                MapBuilder.<String,Object>newBuilder().
                                        add("jira.jwd.draft.layout:Two Workflow","{two-workflow-layout}").
                                        add("jira.jwd.draft.layout:One Workflow","{one-workflow-layout}").
                                        add("jira.jwd.draft.layout:&lt;b&gt;Four&lt;/b&gt; Workflow","{four-workflow-layout}").
                                        toTreeMap()).
                        build();

        final Map<String, Object> expectedMigratedLayoutValues =
                ImmutableMap.<String, Object>of
                        (
                                "jira.workflow.draft.layout:18f81f60c33773a777272e61442a3df1","{one-workflow-layout}",
                                "jira.workflow.draft.layout:ab8a07526769d02ebdbb48419401f1a0","{two-workflow-layout}",
                                "jira.workflow.draft.layout:e38000f94aa34bcff8917e1f1e82af8e","{four-workflow-layout}"
                        );

        final LegacyWorkflowLayoutPropertyKeyMigration legacyWorkflowLayoutPropertyKeyMigration =
                new LegacyWorkflowLayoutPropertyKeyMigration
                        (
                                Stubs.PropertySets.factory().
                                        setPropertySetInstanceTo(existingWorkflowDesignerProperties).
                                        build()
                        );

        legacyWorkflowLayoutPropertyKeyMigration.perform();

        assertTrue(existingWorkflowDesignerProperties.asMap().equals(expectedMigratedLayoutValues));
    }

    @Test
    public void draftWorkflowLayoutsWhichAlreadyFollowTheNewFormatShouldNotBeModified()
    {
        final InMemoryPropertySet existingWorkflowDesignerProperties =
                Stubs.PropertySets.newBuilder().
                        initialiseWith(
                                MapBuilder.<String,Object>newBuilder().
                                        add("jira.workflow.draft.layout:some-md5-1","{one-workflow-layout}").
                                        add("jira.workflow.draft.layout:some-md5-2","{two-workflow-layout}").
                                        add("jira.workflow.draft.layout:some-md5-3","{four-workflow-layout}").
                                        toTreeMap()).
                        build();

        final Map<String, Object> expectedLayoutValuesAfterPerformingTheMigration =
                ImmutableMap.<String, Object>of
                        (
                                "jira.workflow.draft.layout:some-md5-1","{one-workflow-layout}",
                                "jira.workflow.draft.layout:some-md5-2","{two-workflow-layout}",
                                "jira.workflow.draft.layout:some-md5-3","{four-workflow-layout}"
                        );

        final LegacyWorkflowLayoutPropertyKeyMigration legacyWorkflowLayoutPropertyKeyMigration =
                new LegacyWorkflowLayoutPropertyKeyMigration
                        (
                                Stubs.PropertySets.factory().
                                        setPropertySetInstanceTo(existingWorkflowDesignerProperties).
                                        build()
                        );

        legacyWorkflowLayoutPropertyKeyMigration.perform();

        assertTrue(existingWorkflowDesignerProperties.asMap().equals(expectedLayoutValuesAfterPerformingTheMigration));
    }

    @Test
    public void allExistingLiveWorkflowLayoutsShouldBeMigratedToTheNewStorageFormat()
    {
        final InMemoryPropertySet existingWorkflowDesignerProperties =
                Stubs.PropertySets.newBuilder().
                        initialiseWith(
                                MapBuilder.<String,Object>newBuilder().
                                        add("jira.jwd.layout:Two Workflow","{two-workflow-layout}").
                                        add("jira.jwd.layout:One Workflow","{one-workflow-layout}").
                                        add("jira.jwd.layout:&lt;b&gt;Four&lt;/b&gt; Workflow","{four-workflow-layout}").
                                        toTreeMap()).
                        build();

        final Map<String, Object> expectedMigratedLayoutValues =
                ImmutableMap.<String, Object>of
                        (
                                "jira.workflow.layout:18f81f60c33773a777272e61442a3df1","{one-workflow-layout}",
                                "jira.workflow.layout:ab8a07526769d02ebdbb48419401f1a0","{two-workflow-layout}",
                                "jira.workflow.layout:e38000f94aa34bcff8917e1f1e82af8e","{four-workflow-layout}"
                        );

        final LegacyWorkflowLayoutPropertyKeyMigration legacyWorkflowLayoutPropertyKeyMigration =
                new LegacyWorkflowLayoutPropertyKeyMigration
                        (
                                Stubs.PropertySets.factory().
                                        setPropertySetInstanceTo(existingWorkflowDesignerProperties).
                                        build()
                        );

        legacyWorkflowLayoutPropertyKeyMigration.perform();

        assertTrue(existingWorkflowDesignerProperties.asMap().equals(expectedMigratedLayoutValues));
    }

    @Test
    public void liveWorkflowLayoutsWhichAlreadyFollowTheNewFormatShouldNotBeModified()
    {
        final InMemoryPropertySet existingWorkflowDesignerProperties =
                Stubs.PropertySets.newBuilder().
                        initialiseWith(
                                MapBuilder.<String,Object>newBuilder().
                                        add("jira.workflow.layout:some-md5-1","{one-workflow-layout}").
                                        add("jira.workflow.layout:some-md5-2","{two-workflow-layout}").
                                        add("jira.workflow.layout:some-md5-3","{four-workflow-layout}").
                                        toTreeMap()).
                        build();

        final Map<String, Object> expectedLayoutValuesAfterPerformingTheMigration =
                ImmutableMap.<String, Object>of
                        (
                                "jira.workflow.layout:some-md5-1","{one-workflow-layout}",
                                "jira.workflow.layout:some-md5-2","{two-workflow-layout}",
                                "jira.workflow.layout:some-md5-3","{four-workflow-layout}"
                        );

        final LegacyWorkflowLayoutPropertyKeyMigration legacyWorkflowLayoutPropertyKeyMigration =
                new LegacyWorkflowLayoutPropertyKeyMigration
                        (
                                Stubs.PropertySets.factory().
                                        setPropertySetInstanceTo(existingWorkflowDesignerProperties).
                                        build()
                        );

        legacyWorkflowLayoutPropertyKeyMigration.perform();

        assertTrue
                (
                        existingWorkflowDesignerProperties.asMap().equals(expectedLayoutValuesAfterPerformingTheMigration)
                );
    }

    @Test
    public void nonLayoutWorkflowDesignerEntriesShouldNotBeModified()
    {
        final InMemoryPropertySet existingWorkflowDesignerProperties =
                Stubs.PropertySets.newBuilder().
                        initialiseWith(
                                MapBuilder.<String,Object>newBuilder().
                                        add("jira.jwd.annotation:test workflow 1","value 1").
                                        add("jira.jwd.annotation:test workflow 2","value 2").
                                        add("jira.jwd.annotation:another workflow","another value").
                                        add("another.unrelated.property","unrelated value").
                                        toTreeMap()).
                        build();

        final Map<String, Object> expectedLayoutValuesAfterPerformingTheMigration =
                ImmutableMap.<String, Object>of
                        (
                                "jira.jwd.annotation:test workflow 1","value 1",
                                "jira.jwd.annotation:test workflow 2","value 2",
                                "jira.jwd.annotation:another workflow","another value",
                                "another.unrelated.property","unrelated value"
                        );

        final LegacyWorkflowLayoutPropertyKeyMigration legacyWorkflowLayoutPropertyKeyMigration =
                new LegacyWorkflowLayoutPropertyKeyMigration
                        (
                                Stubs.PropertySets.factory().
                                        setPropertySetInstanceTo(existingWorkflowDesignerProperties).
                                        build()
                        );

        legacyWorkflowLayoutPropertyKeyMigration.perform();

        assertTrue
                (
                        existingWorkflowDesignerProperties.asMap().
                                equals(expectedLayoutValuesAfterPerformingTheMigration)
                );
    }

    @Test
    public void migrationIsSuccesfulWhenThereAreNoWorkflowDesignerProperties()
    {
        final InMemoryPropertySet existingWorkflowDesignerLayoutValues =
                Stubs.PropertySets.newBuilder().
                        initialiseWith(Collections.<String, Object>emptyMap()).
                        build();

        final LegacyWorkflowLayoutPropertyKeyMigration legacyWorkflowLayoutPropertyKeyMigration =
                new LegacyWorkflowLayoutPropertyKeyMigration
                        (
                                Stubs.PropertySets.factory().
                                        setPropertySetInstanceTo(existingWorkflowDesignerLayoutValues).
                                        build()
                        );

        legacyWorkflowLayoutPropertyKeyMigration.perform();

        assertTrue(existingWorkflowDesignerLayoutValues.asMap().equals(Collections.emptyMap()));

    }
    private static class Stubs
    {
        private static class PropertySets
        {
            static PropertySetBuilder newBuilder()
            {
                return new PropertySetBuilder();
            }

            private static class PropertySetBuilder
            {
                private Map<String, Object> properties;

                PropertySetBuilder initialiseWith(final Map<String, Object> properties)
                {
                    this.properties = properties;
                    return this;
                }

                InMemoryPropertySet build()
                {
                    return new InMemoryPropertySet(properties);
                }
            }
            static JiraPropertySetFactoryBuilder factory()
            {
                return new JiraPropertySetFactoryBuilder();
            }

            private static class JiraPropertySetFactoryBuilder
            {
                private PropertySet propertySetInstance;

                JiraPropertySetFactoryBuilder setPropertySetInstanceTo(final PropertySet propertySet)
                {
                    this.propertySetInstance = propertySet;
                    return this;
                }

                JiraPropertySetFactory build()
                {
                    final JiraPropertySetFactory instance = Mockito.mock(JiraPropertySetFactory.class);
                    when(instance.buildNoncachingPropertySet(anyString())).thenReturn(propertySetInstance);
                    return instance;
                }
            }
        }
    }
}
