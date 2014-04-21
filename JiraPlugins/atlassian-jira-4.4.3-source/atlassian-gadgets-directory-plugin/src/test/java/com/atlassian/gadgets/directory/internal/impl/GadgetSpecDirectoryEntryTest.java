package com.atlassian.gadgets.directory.internal.impl;

import java.net.URI;
import java.util.Set;

import com.atlassian.gadgets.directory.Category;
import com.atlassian.gadgets.spec.Feature;
import com.atlassian.gadgets.spec.GadgetSpec;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.atlassian.gadgets.spec.GadgetSpec.gadgetSpec;
import static com.google.common.collect.Iterables.getOnlyElement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GadgetSpecDirectoryEntryTest
{
    private static final URI MONKEY_GADGET_SPEC_URI = URI.create("http://www.example.com/monkey.xml");
    private final Feature featureCategory = mock(Feature.class);
    private GadgetSpec spec = gadgetSpec(MONKEY_GADGET_SPEC_URI)
            .features(ImmutableMap.<String, Feature>builder()
                .put("gadget-directory", featureCategory)
                .build())
            .build();

    @Test
    public void assertThatGetCategoriesReturnsJiraCategory() throws Exception
    {
        when(featureCategory.getParameterValue("categories")).thenReturn("JIRA");
        spec = gadgetSpec(spec).title("JIRA Category Gadget").build();

        GadgetSpecDirectoryEntry gadgetSpecDirectoryEntry = new GadgetSpecDirectoryEntry(spec, true, null);
        Set<Category> categories = gadgetSpecDirectoryEntry.getCategories();
        assertThat(getOnlyElement(categories), is(equalTo(Category.JIRA)));
    }

    @Test
    public void assertThatGetCategoriesReturnsOtherCategoryWhenBlank() throws Exception
    {
        when(featureCategory.getParameterValue("categories")).thenReturn("");
        spec = gadgetSpec(spec).title("Blank Category Gadget").build();

        GadgetSpecDirectoryEntry gadgetSpecDirectoryEntry = new GadgetSpecDirectoryEntry(spec, true, null);
        Set<Category> categories = gadgetSpecDirectoryEntry.getCategories();
        assertThat(getOnlyElement(categories), is(equalTo(Category.OTHER)));
    }

    @Test
    public void assertThatGetCategoriesReturnsOtherCategoryWhenUnincluded() throws Exception
    {
        spec = gadgetSpec(spec).title("No Category Gadget").build();

        GadgetSpecDirectoryEntry gadgetSpecDirectoryEntry = new GadgetSpecDirectoryEntry(spec, true, null);
        Set<Category> categories = gadgetSpecDirectoryEntry.getCategories();
        assertThat(getOnlyElement(categories), is(equalTo(Category.OTHER)));
    }

    @Test
    public void assertThatGetCategoriesReturnsOtherCategoryWhenUnrecognized() throws Exception
    {
        when(featureCategory.getParameterValue("categories")).thenReturn(" unrecognized   ");
        spec = gadgetSpec(spec).title("Unrecognized Category Gadget").build();

        GadgetSpecDirectoryEntry gadgetSpecDirectoryEntry = new GadgetSpecDirectoryEntry(spec, true, null);
        Set<Category> categories = gadgetSpecDirectoryEntry.getCategories();
        assertThat(getOnlyElement(categories), is(equalTo(Category.OTHER)));
    }

    @Test
    public void assertThatGetCategoriesReturnsMultipleCategories() throws Exception
    {
        // intentional mis-capitalization, since any case should be fine
        when(featureCategory.getParameterValue("categories")).thenReturn("   Jira   \n     confluence   ");
        spec = gadgetSpec(spec).title("JIRA and Confluence Gadget").build();

        GadgetSpecDirectoryEntry gadgetSpecDirectoryEntry = new GadgetSpecDirectoryEntry(spec, true, null);
        Set<Category> categories = gadgetSpecDirectoryEntry.getCategories();
        assertTrue(categories.contains(Category.JIRA));
        assertTrue(categories.contains(Category.CONFLUENCE));
    }

    @Test
    public void assertThatGetCategoriesReturnsAliasedCategory() throws Exception
    {
        when(featureCategory.getParameterValue("categories")).thenReturn("chart");
        spec = gadgetSpec(spec).title("Aliased Category Gadget").build();

        GadgetSpecDirectoryEntry gadgetSpecDirectoryEntry = new GadgetSpecDirectoryEntry(spec, true, null);
        Set<Category> categories = gadgetSpecDirectoryEntry.getCategories();
        assertThat(getOnlyElement(categories), is(equalTo(Category.CHARTS)));
    }
}
