package com.atlassian.jira.portal;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import org.apache.commons.collections.map.ListOrderedMap;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestProjectAndProjectCategoryValuesGenerator extends ListeningTestCase
{
    @Test
    public void testInvertAndSortNullMap()
    {
        assertNull(ProjectAndProjectCategoryValuesGenerator.invertAndSort(null));
    }

    @Test
    public void testInvertAndSort()
    {
        Map map = EasyMap.build("1", "Z", "2", "A", "3", "Y");
        Map retMap = ProjectAndProjectCategoryValuesGenerator.invertAndSort(map);
        assertEquals(3, retMap.size());
        Iterator iterator = retMap.entrySet().iterator();
        assertMapEntryEquals("A", "2", (Map.Entry) iterator.next());
        assertMapEntryEquals("Y", "3", (Map.Entry) iterator.next());
        assertMapEntryEquals("Z", "1", (Map.Entry) iterator.next());
    }

    @Test
    public void testGetCategoriesForProjectsNull()
    {
        ProjectAndProjectCategoryValuesGenerator generator = getGenerator(null);
        Map retMap = generator.getCategoriesForProjects(null);
        assertNotNull(retMap);
        assertTrue((retMap.isEmpty()));
    }

    @Test
    public void testGetCategoriesForProjectsNoProjectIds()
    {
        ProjectAndProjectCategoryValuesGenerator generator = getGenerator(null);
        Map retMap = generator.getCategoriesForProjects(Collections.EMPTY_SET);
        assertNotNull(retMap);
        assertTrue((retMap.isEmpty()));
    }

    @Test
    public void testGetCategoriesForProjects()
    {
        final Set projectIds = new HashSet(EasyList.build("1", "2"));
        ProjectAndProjectCategoryValuesGenerator generator = new ProjectAndProjectCategoryValuesGenerator()
        {
            void addCategory(Map /* <Long, String> */ categories, String projectId)
            {
                assertNotNull(categories);
                assertTrue(projectIds.contains(projectId));
                categories.put(projectId, "cat" + projectId);
            }
        };


        Map retMap = generator.getCategoriesForProjects(projectIds);
        assertEquals(2, retMap.size());
        assertTrue(retMap.containsValue("cat1"));
        assertTrue(retMap.containsValue("cat2"));
    }

    @Test
    public void testAddCategory()
    {
        GenericValue category1 = new MockGenericValue("ProjectCategory", EasyMap.build("id", new Long(1), "name", "cat1"));

        MockProject project = new MockProject();
        project.setId(new Long(123));
        project.setProjectCategoryGV(category1);

        final MockProjectManager projManager = new MockProjectManager();
        projManager.addProject(project);

        Map categories = new HashMap();

        ProjectAndProjectCategoryValuesGenerator generator = new ProjectAndProjectCategoryValuesGenerator()
        {

            ProjectManager getProjectManager()
            {
                return projManager;
            }
        };

        generator.addCategory(categories, "123");
        assertEquals(1, categories.size());
        assertTrue(categories.containsKey(new Long(1)));
        assertTrue(categories.containsValue("cat1"));

        // project does not have a category
        generator.addCategory(categories, "45");
        // no change - same as above
        assertEquals(1, categories.size());
        assertTrue(categories.containsKey(new Long(1)));
        assertTrue(categories.containsValue("cat1"));

    }

    private void assertMapEntryEquals(String expectedKey, String expectedValue, Map.Entry entry)
    {
        assertEquals(expectedKey, entry.getKey());
        assertEquals(expectedValue, entry.getValue());
    }

    @Test
    public void testNoProjects()
    {
        ProjectAndProjectCategoryValuesGenerator generator = getGenerator(EasyMap.build());
        Map values = generator.getValues(EasyMap.build());
        assertEquals(1, values.size());
        assertEquals("gadget.projects.display.name.all", values.get(ProjectAndProjectCategoryValuesGenerator.Values.ALL_PROJECTS));

    }

    @Test
    public void testWithProjectsOnly()
    {
        ProjectAndProjectCategoryValuesGenerator generator = getGenerator(EasyMap.build("1", "proj1", "2", "proj2"));
        Map values = generator.getValues(EasyMap.build());
        assertEquals(3, values.size());
        assertEquals("gadget.projects.display.name.all", values.get(ProjectAndProjectCategoryValuesGenerator.Values.ALL_PROJECTS));
        assertEquals("proj1", values.get("1"));
        assertEquals("proj2", values.get("2"));
    }

    @Test
    public void testNullProjects()
    {
        ProjectAndProjectCategoryValuesGenerator generator = getGenerator(null);
        Map values = generator.getValues(EasyMap.build());
        assertEquals(1, values.size());
        assertEquals("gadget.projects.display.name.all", values.get(ProjectAndProjectCategoryValuesGenerator.Values.ALL_PROJECTS));
    }

    @Test
    public void testProjectsAndCategories()
    {
        ProjectAndProjectCategoryValuesGenerator generator = getGenerator(
                EasyMap.build("1", "proj1", "2", "proj2"),
                EasyMap.build("1", "cat1", "999", "last category"));

        Map values = generator.getValues(EasyMap.build());

        assertEquals(7, values.size());

        ListOrderedMap orderedMap  = (ListOrderedMap)values;
        // headers
        assertEquals("common.concepts.projects", values.get("-1"));
        assertEquals("admin.menu.projects.project.categories", values.get("-2"));

        // projects
        assertEquals("- gadget.projects.display.name.all", values.get(ProjectAndProjectCategoryValuesGenerator.Values.ALL_PROJECTS));
        assertEquals("- proj1", values.get("1"));
        assertEquals("- proj2", values.get("2"));

        // categories
        assertEquals("- cat1", values.get("cat1"));
        assertEquals("- last category", values.get("cat999"));

        // make sure all is in order
        assertEquals(0, orderedMap.indexOf("-1"));
        assertEquals(1, orderedMap.indexOf(ProjectAndProjectCategoryValuesGenerator.Values.ALL_PROJECTS));
        assertEquals(2, orderedMap.indexOf("1"));
        assertEquals(3, orderedMap.indexOf("2"));
        assertEquals(4, orderedMap.indexOf("-2"));
        assertEquals(5, orderedMap.indexOf("cat1"));
        assertEquals(6, orderedMap.indexOf("cat999"));
    }

    @Test
    public void testProjectIdFilterNoProjects()
    {
        List origList = EasyList.build();
        Set projIds = ProjectAndProjectCategoryValuesGenerator.filterProjectIds(origList);

        assertNotNull(projIds);
        assertEquals(0, projIds.size());
    }

    @Test
    public void testProjectIdFilterProjectsOnly()
    {
        List origList = EasyList.build("123", "789");
        Set projIds = ProjectAndProjectCategoryValuesGenerator.filterProjectIds(origList);

        assertNotNull(projIds);
        assertEquals(2, projIds.size());
        projIds.contains(new Long(123));
        projIds.contains(new Long(789));
    }

    @Test
    public void testProjectIdFilterInvalidProjects()
    {
        List origList = EasyList.build("123", "789asd");
        Set projIds = ProjectAndProjectCategoryValuesGenerator.filterProjectIds(origList);

        assertNotNull(projIds);
        assertEquals(1, projIds.size());
        projIds.contains(new Long(123));
    }

    @Test
    public void testProjectIdFilterProjectsAndCategories()
    {
        List origList = EasyList.build("123", "cat789", "456");
        Set projIds = ProjectAndProjectCategoryValuesGenerator.filterProjectIds(origList);

        assertNotNull(projIds);
        assertEquals(2, projIds.size());
        projIds.contains(new Long(123));
        projIds.contains(new Long(456));
    }

    @Test
    public void testCategoryIdFilterNoCategories()
    {
        List origList = EasyList.build();
        Set categoryIds = ProjectAndProjectCategoryValuesGenerator.filterProjectCategoryIds(origList);

        assertNotNull(categoryIds);
        assertEquals(0, categoryIds.size());
    }

    @Test
    public void testCategoryIdFilterCategoriesOnly()
    {
        List origList = EasyList.build("cat123", "cat789");
        Set categoryIds = ProjectAndProjectCategoryValuesGenerator.filterProjectCategoryIds(origList);

        assertNotNull(categoryIds);
        assertEquals(2, categoryIds.size());
        categoryIds.contains(new Long(123));
        categoryIds.contains(new Long(789));
    }

    @Test
    public void testCategoryIdFilterInvalidCategories()
    {
        List origList = EasyList.build("cat123", "cat789asd");
        Set categoryIds = ProjectAndProjectCategoryValuesGenerator.filterProjectCategoryIds(origList);

        assertNotNull(categoryIds);
        assertEquals(1, categoryIds.size());
        categoryIds.contains(new Long(123));
    }

    @Test
    public void testCategoryIdFilterProjectsAndCategories()
    {
        List origList = EasyList.build("123", "cat789", "456", "cat555");
        Set categoryIds = ProjectAndProjectCategoryValuesGenerator.filterProjectCategoryIds(origList);

        assertNotNull(categoryIds);
        assertEquals(2, categoryIds.size());
        categoryIds.contains(new Long(789));
        categoryIds.contains(new Long(555));
    }

    private ProjectAndProjectCategoryValuesGenerator getGenerator(final Map projects)
    {
        return getGenerator(projects, Collections.EMPTY_MAP);
    }

    private ProjectAndProjectCategoryValuesGenerator getGenerator(final Map projects, final Map categories)
    {
        return new ProjectAndProjectCategoryValuesGenerator()
        {

            Map getProjects(Map params)
            {
                return projects;
            }

            ProjectManager getProjectManager()
            {
                return new MockProjectManager();
            }

            String getText(String key)
            {
                return key;
            }

            Map getCategoriesForProjects(Set projectIds)
            {
                return categories;
            }
        };
    }


}
