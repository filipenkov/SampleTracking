package com.atlassian.jira.project;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.util.collect.MapBuilder;

/**
 * @since v4.0.2
 */
public class TestProjectImpl extends ListeningTestCase
{
    @Test
    public void testEquals() throws Exception
    {
        GenericValue gvProject1 = new MockGenericValue("Project", MapBuilder.newBuilder("key", "HSP").add("name", "Homo").toMap());
        GenericValue gvProject2 = new MockGenericValue("Project", MapBuilder.newBuilder("key", "HSP").add("name", "Homo Sapien").toMap());
        ProjectImpl project1 = new ProjectImpl(gvProject1);
        ProjectImpl project2 = new ProjectImpl(gvProject2);

        // GenericValue checks for equality on EVERY field. This can't be helped.
        assertFalse(gvProject1.equals(gvProject2));
        // JRA-20184. ProjectImpl should only care about the KEY field, other fields can legitimately change.
        assertTrue(project1.equals(project2));
        // If the objects are equal, then the hashCodes must be equal
        assertEquals(project1.hashCode(), project2.hashCode());
    }

    @Test
    public void testEquals1NullKey() throws Exception
    {
        GenericValue gvProject1 = new MockGenericValue("Project", MapBuilder.newBuilder("key", "HSP").toMap());
        GenericValue gvProject2 = new MockGenericValue("Project", MapBuilder.newBuilder("key", null).toMap());
        ProjectImpl project1 = new ProjectImpl(gvProject1);
        ProjectImpl project2 = new ProjectImpl(gvProject2);

        // JRA-20184. ProjectImpl should only care about the KEY field, other fields can legitimately change.
        assertFalse(project1.equals(project2));
        assertFalse(project2.equals(project1));
    }

    @Test
    public void testEquals2NullKeys() throws Exception
    {
        GenericValue gvProject1 = new MockGenericValue("Project", MapBuilder.newBuilder("key", null).add("name", "Homo").toMap());
        GenericValue gvProject2 = new MockGenericValue("Project", MapBuilder.newBuilder("key", null).add("name", "Homo Sapien").toMap());
        ProjectImpl project1 = new ProjectImpl(gvProject1);
        ProjectImpl project2 = new ProjectImpl(gvProject2);

        // GenericValue checks for equality on EVERY field. This can't be helped.
        assertFalse(gvProject1.equals(gvProject2));
        // JRA-20184. ProjectImpl should only care about the KEY field, other fields can legitimately change.
        assertTrue(project1.equals(project2));
        // If the objects are equal, then the hashCodes must be equal
        assertEquals(project1.hashCode(), project2.hashCode());
    }
}
