package com.atlassian.jira.jql.values;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.issue.label.LabelManager;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.opensymphony.user.User;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class TestLabelsClauseValuesGenerator extends ListeningTestCase
{
    private MockProviderAccessor mpa = new MockProviderAccessor();
    private User user = new User("admin", mpa, new MockCrowdService());

    @Test
    public void testGetPossibleValues()
    {
        final LabelManager mockLabelManager = createMock(LabelManager.class);
        final SearchHandlerManager mockSearchHandlerManager = createMock(SearchHandlerManager.class);
        expect(mockSearchHandlerManager.getFieldIds(user, "labels")).andReturn(CollectionBuilder.list("labels"));
        expect(mockLabelManager.getSuggestedLabels(user, null, "to")).andReturn(CollectionBuilder.newBuilder("token", "toxic").asSortedSet());

        replay(mockLabelManager, mockSearchHandlerManager);
        final LabelsClauseValuesGenerator generator = new LabelsClauseValuesGenerator(mockLabelManager)
        {
            @Override
            SearchHandlerManager getSearchHandlerManager()
            {
                return mockSearchHandlerManager;
            }
        };
        final ClauseValuesGenerator.Results results = generator.getPossibleValues(user, "labels", "to", 10);
        assertEquals(CollectionBuilder.list(new ClauseValuesGenerator.Result("token"), new ClauseValuesGenerator.Result("toxic")), results.getResults());

        verify(mockLabelManager, mockSearchHandlerManager);
    }

    @Test
    public void testGetPossibleValuesMultipleFields()
    {
        final LabelManager mockLabelManager = createMock(LabelManager.class);
        final SearchHandlerManager mockSearchHandlerManager = createMock(SearchHandlerManager.class);
        expect(mockSearchHandlerManager.getFieldIds(user, "epic")).andReturn(CollectionBuilder.list("customfield_10000", "customfield_10001"));
        expect(mockLabelManager.getSuggestedLabels(user, null, 10000L, "to")).andReturn(CollectionBuilder.newBuilder("token", "toxic").asSortedSet());
        expect(mockLabelManager.getSuggestedLabels(user, null, 10001L, "to")).andReturn(CollectionBuilder.newBuilder("tobar", "tozzle").asSortedSet());

        replay(mockLabelManager, mockSearchHandlerManager);
        final LabelsClauseValuesGenerator generator = new LabelsClauseValuesGenerator(mockLabelManager)
        {
            @Override
            SearchHandlerManager getSearchHandlerManager()
            {
                return mockSearchHandlerManager;
            }
        };
        final ClauseValuesGenerator.Results results = generator.getPossibleValues(user, "epic", "to", 10);
        assertEquals(CollectionBuilder.list(
                new ClauseValuesGenerator.Result("tobar"),
                new ClauseValuesGenerator.Result("token"),
                new ClauseValuesGenerator.Result("toxic"),
                new ClauseValuesGenerator.Result("tozzle")),
                results.getResults());

        verify(mockLabelManager, mockSearchHandlerManager);
    }
    
    @Test
    public void testGetPossibleValuesMaxResults()
    {
        final LabelManager mockLabelManager = createMock(LabelManager.class);
        final SearchHandlerManager mockSearchHandlerManager = createMock(SearchHandlerManager.class);
        expect(mockSearchHandlerManager.getFieldIds(user, "epic")).andReturn(CollectionBuilder.list("customfield_10000", "customfield_10001"));
        expect(mockLabelManager.getSuggestedLabels(user, null, 10000L, "to")).andReturn(CollectionBuilder.newBuilder("token", "toxic").asSortedSet());
        expect(mockLabelManager.getSuggestedLabels(user, null, 10001L, "to")).andReturn(CollectionBuilder.newBuilder("tobar", "tozzle").asSortedSet());

        replay(mockLabelManager, mockSearchHandlerManager);
        final LabelsClauseValuesGenerator generator = new LabelsClauseValuesGenerator(mockLabelManager)
        {
            @Override
            SearchHandlerManager getSearchHandlerManager()
            {
                return mockSearchHandlerManager;
            }
        };
        final ClauseValuesGenerator.Results results = generator.getPossibleValues(user, "epic", "to", 2);
        assertEquals(CollectionBuilder.list(
                new ClauseValuesGenerator.Result("tobar"),
                new ClauseValuesGenerator.Result("token")),
                results.getResults());

        verify(mockLabelManager, mockSearchHandlerManager);
    }

}
