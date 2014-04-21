package com.atlassian.jira.issue.customfields.searchers.information;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.index.indexers.impl.IssueTypeIndexer;
import com.atlassian.jira.issue.search.searchers.SearcherGroupType;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @since v4.0
 */
public class TestCustomFieldSearcherInformation extends MockControllerTestCase
{
    @Test
    public void testBadConstructorArgs() throws Exception
    {
        final String searcherId = "searcherId";
        final String nameKey = "nameKey";
        final JiraAuthenticationContext context = mockController.getMock(JiraAuthenticationContext.class);

        final AtomicReference<CustomField> fieldReference = new AtomicReference<CustomField>(null);

        mockController.replay();

        try
        {
            new CustomFieldSearcherInformation(searcherId, nameKey, null, fieldReference);
            fail("Expected exception for null indexers");
        }
        catch (IllegalArgumentException expected) {}

        try
        {
            new CustomFieldSearcherInformation(searcherId, nameKey, Collections.<FieldIndexer>emptyList(), fieldReference);
            fail("Expected exception for empty indexers");
        }
        catch (IllegalStateException expected) {}
    }

    @Test
    public void testGetRelatedIndexersNullCustomField() throws Exception
    {
        final String searcherId = "searcherId";
        final String nameKey = "nameKey";
        final JiraAuthenticationContext context = mockController.getMock(JiraAuthenticationContext.class);

        final AtomicReference<CustomField> fieldReference = new AtomicReference<CustomField>(null);

        final CustomFieldSearcherInformation info = new CustomFieldSearcherInformation(searcherId, nameKey, Collections.<FieldIndexer>singletonList(new IssueTypeIndexer(null)), fieldReference);

        mockController.replay();

        try
        {
            info.getRelatedIndexers();
            fail("Expected exception for empty indexers");
        }
        catch (IllegalStateException expected) {}

        mockController.verify();
    }

    @Test
    public void testGetRelatedIndexersFromCustomFieldType() throws Exception
    {
        final String searcherId = "searcherId";
        final String nameKey = "nameKey";
        final JiraAuthenticationContext context = mockController.getMock(JiraAuthenticationContext.class);

        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        final CustomField customField = mockController.getMock(CustomField.class);

        final List<FieldIndexer> expectedIndexers = Collections.singletonList(mockController.getMock(FieldIndexer.class));

        customField.getCustomFieldType();
        mockController.setReturnValue(customFieldType);
        customFieldType.getRelatedIndexers(customField);
        mockController.setReturnValue(expectedIndexers);

        final AtomicReference<CustomField> fieldReference = new AtomicReference<CustomField>(customField);

        final CustomFieldSearcherInformation info = new CustomFieldSearcherInformation(searcherId, nameKey, Collections.<FieldIndexer>singletonList(new IssueTypeIndexer(null)), fieldReference);

        mockController.replay();
        
        assertEquals(expectedIndexers, info.getRelatedIndexers());

        mockController.verify();
    }

    @Test
    public void testGetRelatedIndexersFromProvided() throws Exception
    {
        final String searcherId = "searcherId";
        final String nameKey = "nameKey";
        final JiraAuthenticationContext context = mockController.getMock(JiraAuthenticationContext.class);

        final CustomFieldType customFieldType = mockController.getMock(CustomFieldType.class);
        final CustomField customField = mockController.getMock(CustomField.class);

        final List<FieldIndexer> expectedIndexers = Collections.singletonList(mockController.getMock(FieldIndexer.class));

        customField.getCustomFieldType();
        mockController.setReturnValue(customFieldType);
        customFieldType.getRelatedIndexers(customField);
        mockController.setReturnValue(null);

        final AtomicReference<CustomField> fieldReference = new AtomicReference<CustomField>(customField);

        final CustomFieldSearcherInformation info = new CustomFieldSearcherInformation(searcherId, nameKey, expectedIndexers, fieldReference);

        mockController.replay();

        assertEquals(expectedIndexers, info.getRelatedIndexers());

        mockController.verify();
    }

    @Test
    public void testGetSearcherGroupType() throws Exception
    {
        final String searcherId = "searcherId";
        final String nameKey = "nameKey";

        final JiraAuthenticationContext context = mockController.getMock(JiraAuthenticationContext.class);
        final AtomicReference<CustomField> fieldReference = new AtomicReference<CustomField>(null);
        final CustomFieldSearcherInformation info = new CustomFieldSearcherInformation(searcherId, nameKey, Collections.<FieldIndexer>singletonList(new IssueTypeIndexer(null)), fieldReference);
        mockController.replay();

        assertEquals(SearcherGroupType.CUSTOM, info.getSearcherGroupType());

        mockController.verify();
    }
}
