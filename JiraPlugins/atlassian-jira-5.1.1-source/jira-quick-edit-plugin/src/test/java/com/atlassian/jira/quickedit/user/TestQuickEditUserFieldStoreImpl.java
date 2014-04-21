package com.atlassian.jira.quickedit.user;

import com.atlassian.jira.quickedit.rest.api.UserPreferences;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.opensymphony.module.propertyset.PropertySet;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TestQuickEditUserFieldStoreImpl
{
    private final MockUser admin = new MockUser("admin");
    private PropertySet mockPS;
    private UserPreferencesStoreImpl store;

    @Before
    public void setUp() throws Exception
    {
        final UserPropertyManager mockUserPropertyManager = mock(UserPropertyManager.class);
        mockPS = mock(PropertySet.class);
        store = new UserPreferencesStoreImpl(mockUserPropertyManager);

        when(mockUserPropertyManager.getPropertySet(eq(admin))).thenReturn(mockPS);
    }

    @Test
    public void testGetEditFields() throws Exception
    {
        when(mockPS.getText(eq("jira.quick.edit.fields"))).thenReturn("summary,assignee");

        UserPreferences userPreferences = store.getEditUserPreferences(admin);
        assertEquals(CollectionBuilder.list("summary", "assignee"), userPreferences.getFields());
    }

    @Test
    public void testGetEditFieldsEmpty() throws Exception
    {
        when(mockPS.getText(eq("jira.quick.edit.fields"))).thenReturn(null);

        UserPreferences userPreferences = store.getEditUserPreferences(admin);
        assertEquals(CollectionBuilder.list("fixVersions", "assignee", "labels", "components", "priority", "comment"), userPreferences.getFields());
    }

    @Test
    public void testGetCreateFields() throws Exception
    {
        when(mockPS.getText(eq("jira.quick.create.fields"))).thenReturn("description");

        UserPreferences userPreferences = store.getCreateUserPreferences(admin);
        assertEquals(CollectionBuilder.list("description"), userPreferences.getFields());
    }

    @Test
    public void testStoreEditFields() throws Exception
    {
        UserPreferences.Builder builder = new UserPreferences.Builder();
        builder.fields(CollectionBuilder.list("fixVersion", "assignee", "reporter"));
        store.storeEditUserPreferences(admin, builder.build());
        verify(mockPS).setText(eq("jira.quick.edit.fields"), eq("fixVersion,assignee,reporter"));

        builder = new UserPreferences.Builder();
        store.storeEditUserPreferences(admin, builder.build());
        verify(mockPS).setText(eq("jira.quick.edit.fields"), eq(""));
    }

    @Test
    public void testStoreCreateFields() throws Exception
    {
        UserPreferences.Builder builder = new UserPreferences.Builder();
        builder.fields(CollectionBuilder.list("fixVersion", "assignee", "reporter"));
        store.storeCreateUserPreferences(admin, builder.build());
        verify(mockPS).setText(eq("jira.quick.create.fields"), eq("fixVersion,assignee,reporter"));

        builder = new UserPreferences.Builder();
        store.storeCreateUserPreferences(admin, builder.build());
        verify(mockPS).setText(eq("jira.quick.create.fields"), eq(""));
    }
}
