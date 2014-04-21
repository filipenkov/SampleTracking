package com.atlassian.jira.issue.search;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.atlassian.jira.util.collect.CollectionBuilder;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Collections;
import java.util.Set;

/**
 * @since v4.0
 */
public class TestClauseNames extends ListeningTestCase
{
    @Test
    public void testClauseNamesConstructorBad() throws Exception
    {
        Set<String> goodNames = CollectionBuilder.newBuilder("jack").asSet();
        Set<String> nullNames = Collections.singleton(null);
        String[] nullNamesInArray = new String[] {null};
        Set<String> emptyName = CollectionBuilder.newBuilder("jill", "").asSet();

        try
        {
            new ClauseNames(null);
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}

        try
        {
            new ClauseNames("  ");
            fail("Expected exception");
        }
        catch (IllegalArgumentException expected) {}

        try
        {
            new ClauseNames("dude", (Set<String>) null);
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            new ClauseNames("dude", (String[]) null);
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            new ClauseNames("dude", nullNames);
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            new ClauseNames("dude", nullNamesInArray);
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            new ClauseNames("dude", emptyName);
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }

        try
        {
            new ClauseNames(null, goodNames);
            fail("Should not accept these arguments.");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    @Test
    public void testClauseNamesConstructorHappyPath() throws Exception
    {
        Set<String> goodNames = CollectionBuilder.newBuilder("jack").asSet();

        final ClauseNames names = new ClauseNames("dude", goodNames);

        assertTrue(names.contains("dude"));
        assertTrue(names.contains("jack"));
        
        final ClauseNames names2 = new ClauseNames("dude", "jack");
        assertEquals(names, names2);
    }

    @Test
    public void testForCustomField() throws Exception
    {
        final long customFieldId = 10L;
        final String customFieldName = "NotSystemField";

        final CustomField mock = createMock(CustomField.class);
        expect(mock.getName()).andReturn(customFieldName).anyTimes();
        expect(mock.getIdAsLong()).andReturn(customFieldId).anyTimes();
        replay(mock);

        final ClauseNames names = ClauseNames.forCustomField(mock);
        ClauseNames expectedNames = new ClauseNames(JqlCustomFieldId.toString(customFieldId), customFieldName);

        assertEquals(expectedNames, names);

        verify(mock);
    }

    @Test
    public void testForCustomFieldWithSystemName() throws Exception
    {
        final long customFieldId = 10L;
        final String customFieldName = "pRoJeCT";

        final CustomField mock = createMock(CustomField.class);
        expect(mock.getName()).andReturn(customFieldName).anyTimes();
        expect(mock.getIdAsLong()).andReturn(customFieldId).anyTimes();
        replay(mock);

        final ClauseNames names = ClauseNames.forCustomField(mock);
        ClauseNames expectedNames = new ClauseNames(JqlCustomFieldId.toString(customFieldId));

        assertEquals(expectedNames, names);

        verify(mock);
    }

    @Test
    public void testForCustomFieldNameWithId() throws Exception
    {
        final long customFieldId = 10L;
        final String customFieldName = "cf [ 2889292 ]  ";

        final CustomField mock = createMock(CustomField.class);
        expect(mock.getName()).andReturn(customFieldName).anyTimes();
        expect(mock.getIdAsLong()).andReturn(customFieldId).anyTimes();
        replay(mock);

        final ClauseNames names = ClauseNames.forCustomField(mock);
        ClauseNames expectedNames = new ClauseNames(JqlCustomFieldId.toString(customFieldId));

        assertEquals(expectedNames, names);

        verify(mock);
    }
}
