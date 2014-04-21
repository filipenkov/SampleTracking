package com.atlassian.jira.workflow.names;

import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.jira.workflow.WorkflowsRepository;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Responsible for holding the unit tests for the {@link DefaultWorkflowCopyNameFactory}.
 *
 * @since v5.1
 */
public class TestWorkflowCopyNameFactory
{
    @Test
    public void theNameOfTheCopyShouldIncludeTheCompleteNameOfTheOriginalWorkflowGivenThatItWouldNotExceedTheMaxNumberOfCharsAllowedForAWorkflowName()
    {
        final String originalWorkflowName="A short name";

        final WorkflowCopyNameFactory workflowCopyNameFactory =
                new DefaultWorkflowCopyNameFactory
                        (
                                mock(WorkflowsRepository.class), getMockI18nBeanFactory()
                        );

        final String actualWorkflowCopyName = workflowCopyNameFactory.createFrom(originalWorkflowName, Locale.ENGLISH);
        assertTrue(actualWorkflowCopyName.contains(originalWorkflowName));
    }

    @Test
    public void shouldShortenTheNameOfTheCopyWhenItWouldExceedTheMaxNumberOfCharsAllowedForAWorkflowName()
    {
        final String originalWorkflowName = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                + "Morbi condimentum ornare eros ut adipiscing. In hac habitasse platea dictumst. "
                + "Cras mattis euismod mi. "
                + "In elit arcu, placerat at placerat lacinia, molestie id mauris. Curabitur eu lacus ac mi metus.";

        final WorkflowCopyNameFactory workflowCopyNameFactory =
                new DefaultWorkflowCopyNameFactory
                        (
                                mock(WorkflowsRepository.class), getMockI18nBeanFactory()
                        );

        final String actualWorkflowCopyName = workflowCopyNameFactory.createFrom(originalWorkflowName, Locale.ENGLISH);
        assertTrue(actualWorkflowCopyName.length() <= 255);
    }

    @Test
    public void shouldAttemptToAppendANumberToTheNameOfTheCopyGivenThereIsAlreadyAWorkflowInTheDbWithTheInitialSuggestedNameForTheCopy()
    {
        final String originalWorkflowName = "A short name";
        final String initialSuggestedName = "Copy of A short name";

        final WorkflowsRepository workflowsRepository = mock(WorkflowsRepository.class);
        when(workflowsRepository.contains(initialSuggestedName)).thenReturn(true);

        final WorkflowCopyNameFactory workflowCopyNameFactory =
                new DefaultWorkflowCopyNameFactory
                        (
                                workflowsRepository, getMockI18nBeanFactory()
                        );

        final String actualWorkflowCopyName = workflowCopyNameFactory.createFrom(originalWorkflowName, Locale.ENGLISH);
        assertTrue(actualWorkflowCopyName.equals("Copy 2 of A short name"));
    }

    @Test
    public void shouldAttemptToAppendANumberAndShortenTheNameOfTheCopyIfItExceedsTheMaxCharsAllowedGivenThereIsAlreadyAWorkflowInTheDbWithTheInitialSuggestedName()
    {
        final String originalWorkflowName = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                + "Morbi condimentum ornare eros ut adipiscing. In hac habitasse platea dictumst. "
                + "Cras mattis euismod mi. "
                + "In elit arcu, placerat at placerat lacinia, molestie id mauris. Curabitur eu lacus ac mi metus.";

        final String initialSuggestedName = "Copy of Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                + "Morbi condimentum ornare eros ut adipiscing. "
                + "In hac habitasse platea dictumst. Cras mattis euismod mi. "
                + "In elit arcu, placerat at placerat lacinia, molestie id mauris. Curabitur eu lacus a...";

        final WorkflowsRepository workflowsRepository = mock(WorkflowsRepository.class);
        when(workflowsRepository.contains(initialSuggestedName)).thenReturn(true);

        final WorkflowCopyNameFactory workflowCopyNameFactory =
                new DefaultWorkflowCopyNameFactory
                        (
                                workflowsRepository, getMockI18nBeanFactory()
                        );

        final String actualWorkflowCopyName = workflowCopyNameFactory.createFrom(originalWorkflowName, Locale.ENGLISH);

        assertTrue(actualWorkflowCopyName.length() <= 255);
        assertTrue(actualWorkflowCopyName.startsWith("Copy 2 of Lorem ipsum"));
        assertTrue(actualWorkflowCopyName.endsWith("..."));
    }

    private I18nHelper.BeanFactory getMockI18nBeanFactory()
    {
        return new MockI18nBean.MockI18nBeanFactory();
    }
}
