package com.atlassian.jira.bc.project.component;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.project.AssigneeTypes;
import com.mockobjects.dynamic.Mock;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class TestCachingProjectComponentStore extends TestOfBizProjectComponentStore
{
    protected ProjectComponentStore createStore(MockOfBizDelegator ofBizDelegator)
    {
        return new CachingProjectComponentStore(new OfBizProjectComponentStore(ofBizDelegator));
    }

    public void testUpdate()
    {
        ProjectComponentStore store = createStore(new MockOfBizDelegator(null, null));

        MutableProjectComponent component1 = new MutableProjectComponent(null, "name1", "desc - 1", null, 0, ProjectComponentStoreTester.PROJECT_ID_1);
        MutableProjectComponent component2 = new MutableProjectComponent(null, "name2", "desc - 2", null, 0, ProjectComponentStoreTester.PROJECT_ID_1);

        // test that the cached store does not allow duplicate component name per project in insert operation
        try
        {
            component1 = store.store(component1);
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
        catch (IllegalArgumentException e)
        {
            fail();
        }
        // insert valid second component
        try
        {
            component2 = store.store(component2);
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
        catch (IllegalArgumentException e)
        {
            fail();
        }

        // test that the cached store does not allow duplicate component name per project in update operation
        try
        {
            component1.setName("name2");
            store.store(component1);
            fail();
        }
        catch (EntityNotFoundException e)
        {
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // duplicate name per project not alowed
        }
    }

    public void testContainsNameIgnoreCase()
    {
        List names = EasyList.build("frank", "harry", "moon unit");
        assertFalse(CachingProjectComponentStore.containsNameIgnoreCase(names, "Zappa"));
        assertTrue(CachingProjectComponentStore.containsNameIgnoreCase(names, "Frank"));
        assertTrue(CachingProjectComponentStore.containsNameIgnoreCase(names, "Moon Unit"));
    }

    // Ensure that once cache is populated - it returns the correct detauls (this simluates an XML import or JIRA restart)
    public void testCachingDetails() throws EntityNotFoundException
    {
        MutableProjectComponent component1 = new MutableProjectComponent(new Long(1), "component 1", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent component2 = new MutableProjectComponent(new Long(2), "component 2", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent component3 = new MutableProjectComponent(new Long(3), "component 3", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent component4 = new MutableProjectComponent(new Long(4), "component 1", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1001));
        MutableProjectComponent sameName1 = new MutableProjectComponent(new Long(5), "sameName", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent sameName2 = new MutableProjectComponent(new Long(6), "SameName", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1001));
        Collection project1Components = EasyList.build(component1, component2, component3, sameName1);
        Collection project2Components = EasyList.build(component4, sameName2);
        Collection sameNameComponents = EasyList.build(sameName1, sameName2);
        Collection allComponents = EasyList.build(component1, component2, component3, component4, sameName1, sameName2);
        Mock mockOfBizStore = new Mock(ProjectComponentStore.class);

        // Mock out the initCache() call to the persistence store
        mockOfBizStore.expectAndReturn("findAll", allComponents);
        CachingProjectComponentStore cachingProjectComponentStore = new CachingProjectComponentStore((ProjectComponentStore) mockOfBizStore.proxy());

        // Retrieve all the components with name sameName
        assertTrue(cachingProjectComponentStore.findByComponentNameCaseInSensitive("sameName").containsAll(sameNameComponents));
        assertEquals(2, cachingProjectComponentStore.findByComponentNameCaseInSensitive("sameName").size());

        // Retrieve all the components for the project1
        assertTrue(cachingProjectComponentStore.findAllForProject(new Long(1000)).containsAll(project1Components));
        assertEquals(4, cachingProjectComponentStore.findAllForProject(new Long(1000)).size());

        // Retrieve all the components for the project2
        assertTrue(cachingProjectComponentStore.findAllForProject(new Long(1001)).containsAll(project2Components));
        assertEquals(2, cachingProjectComponentStore.findAllForProject(new Long(1001)).size());

        // Retrieve the component1
        assertEquals(component1, cachingProjectComponentStore.find(new Long(1)));

        // Retrieve the component4
        assertEquals(component4, cachingProjectComponentStore.find(new Long(4)));

        // Retrieve all the components for the project1
        assertTrue(cachingProjectComponentStore.findAll().containsAll(project1Components));
        assertEquals(4, cachingProjectComponentStore.findAllForProject(new Long(1000)).size());

        // Retrieve all the components for the project2
        assertTrue(cachingProjectComponentStore.findAllForProject(new Long(1001)).containsAll(project2Components));
        assertEquals(2, cachingProjectComponentStore.findAllForProject(new Long(1001)).size());

        // Inspect cache of component details
        assertEquals(component1, cachingProjectComponentStore.findByComponentName(new Long(1000), "component 1"));
        assertEquals(new Long(1000), cachingProjectComponentStore.findProjectIdForComponent(new Long(1)));
        assertEquals(component4, cachingProjectComponentStore.findByComponentName(new Long(1001), "component 1"));
        assertEquals(new Long(1001), cachingProjectComponentStore.findProjectIdForComponent(new Long(4)));
    }

    // Change the order of the find and findAllForProject (this simluates an XML import or JIRA restart)
    public void testCachingDetails2() throws EntityNotFoundException
    {

        MutableProjectComponent component1 = new MutableProjectComponent(new Long(1), "component 1", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent component2 = new MutableProjectComponent(new Long(2), "component 2", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent component3 = new MutableProjectComponent(new Long(3), "component 3", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent component4 = new MutableProjectComponent(new Long(4), "component 1", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1001));
        Collection project1Components = EasyList.build(component1, component2, component3);
        Collection allComponents = EasyList.build(component1, component2, component3, component4);
        Mock mockOfBizStore = new Mock(ProjectComponentStore.class);

        // Mock out the initCache() call to the persistence store
        mockOfBizStore.expectAndReturn("findAll", allComponents);
        CachingProjectComponentStore cachingProjectComponentStore = new CachingProjectComponentStore((ProjectComponentStore) mockOfBizStore.proxy());

        // Retrieve specific components
        assertEquals(component1, cachingProjectComponentStore.find(new Long(1)));
        assertEquals(component4, cachingProjectComponentStore.find(new Long(4)));

        // Retrieve all the components for the project1
        assertTrue(cachingProjectComponentStore.findAll().containsAll(project1Components));
        assertEquals(3, cachingProjectComponentStore.findAllForProject(new Long(1000)).size());

        // Retrieve all the components for the project2
        assertTrue(cachingProjectComponentStore.findAll().containsAll(project1Components));
        assertEquals(1, cachingProjectComponentStore.findAllForProject(new Long(1001)).size());

        mockOfBizStore.verify();
    }

    public void testComponentIDProjectComponentCache() throws EntityNotFoundException
    {
        MutableProjectComponent component1 = new MutableProjectComponent(new Long(1), "component 1", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent component2 = new MutableProjectComponent(new Long(2), "component 2", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent component3 = new MutableProjectComponent(new Long(3), "component 3", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent component4 = new MutableProjectComponent(new Long(4), "component 1", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1001));
        Collection allComponents = EasyList.build(component1, component2, component3, component4);
        Mock mockOfBizStore = new Mock(ProjectComponentStore.class);

        // Mock out the initCache() call to the persistence store
        mockOfBizStore.expectAndReturn("findAll", allComponents);
        mockOfBizStore.expectVoid("delete", new Long(1));

        CachingProjectComponentStore cachingProjectComponentStore = new CachingProjectComponentStore((ProjectComponentStore) mockOfBizStore.proxy());

        // Retrieve specific components
        assertEquals(component1, cachingProjectComponentStore.find(new Long(1)));
        assertEquals(component4, cachingProjectComponentStore.find(new Long(4)));
        cachingProjectComponentStore.delete(new Long(1));

        final Collection components = cachingProjectComponentStore.findAll();
        assertTrue(components.containsAll(EasyList.build(component2, component3, component4)));

        mockOfBizStore.verify();
    }

    public void testUpdateProjectIDComponentNameCache() throws EntityNotFoundException
    {
        MutableProjectComponent component1 = new MutableProjectComponent(new Long(1), "component 1", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent component2 = new MutableProjectComponent(new Long(2), "component 2", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent component3 = new MutableProjectComponent(new Long(3), "component 3", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent component4 = new MutableProjectComponent(new Long(4), "component 1", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1001));
        Collection allComponents = EasyList.build(component1, component2, component3, component4);
        Mock mockOfBizStore = new Mock(ProjectComponentStore.class);

        // Mock out the initCache() call to the persistence store
        mockOfBizStore.expectAndReturn("findAll", allComponents);

        final MutableProjectComponent updatedProjectComponent = new MutableProjectComponent(new Long(3), "component 4", "Updated this component", null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        mockOfBizStore.expectAndReturn("store", updatedProjectComponent, updatedProjectComponent);

        CachingProjectComponentStore cachingProjectComponentStore = new CachingProjectComponentStore((ProjectComponentStore) mockOfBizStore.proxy());

        // Retrieve specific components

        cachingProjectComponentStore.store(updatedProjectComponent);
        assertEquals("component 4", cachingProjectComponentStore.find(new Long(3)).getName());

        final MutableProjectComponent newProjectComponent = new MutableProjectComponent(new Long(4), "component 3", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        mockOfBizStore.expectAndReturn("store", newProjectComponent, newProjectComponent);

        cachingProjectComponentStore.store(newProjectComponent);
        assertEquals("component 3", cachingProjectComponentStore.find(new Long(4)).getName());

        final Collection components = cachingProjectComponentStore.findAll();
        assertTrue(components.containsAll(EasyList.build(component2, component3, component4)));

        mockOfBizStore.verify();
    }

    public void testProjectIDComponentObjectCache() throws EntityNotFoundException
    {
        MutableProjectComponent component1 = new MutableProjectComponent(new Long(1), "Zcomponent 1", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent component2 = new MutableProjectComponent(new Long(2), "Dcomponent 2", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent component3 = new MutableProjectComponent(new Long(3), "ACcomponent 3", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent component4 = new MutableProjectComponent(new Long(4), "Bcomponent 1", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1001));
        Collection allComponents = EasyList.build(component1, component2, component3, component4);
        Mock mockOfBizStore = new Mock(ProjectComponentStore.class);

        // Mock out the initCache() call to the persistence store
        mockOfBizStore.expectAndReturn("findAll", allComponents);

        CachingProjectComponentStore cachingProjectComponentStore = new CachingProjectComponentStore((ProjectComponentStore) mockOfBizStore.proxy());

        final Long projectID1 = new Long(1001);
        final List componentList1 = EasyList.build(component4);

        assertEquals(componentList1, cachingProjectComponentStore.findAllForProject(projectID1));

        final Long projectID2 = new Long(1000);
        final List componentList2 = EasyList.build(component1, component2, component3);

        final Collection sortedComponents = cachingProjectComponentStore.findAllForProject(projectID2);
        assertTrue(componentList2.containsAll(sortedComponents));

        Collections.sort(componentList2, ProjectComponentComparator.INSTANCE);

        assertEquals(sortedComponents, componentList2);

        mockOfBizStore.verify();
    }

    public void testComponentIDProjectIDCache() throws EntityNotFoundException
    {
        MutableProjectComponent component1 = new MutableProjectComponent(new Long(1), "Acomponent 1", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent component2 = new MutableProjectComponent(new Long(2), "Bcomponent 2", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent component3 = new MutableProjectComponent(new Long(3), "Ccomponent 3", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1000));
        MutableProjectComponent component4 = new MutableProjectComponent(new Long(4), "Dcomponent 1", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1001));
        Collection allComponents = EasyList.build(component1, component2, component3, component4);
        Mock mockOfBizStore = new Mock(ProjectComponentStore.class);

        // Mock out the initCache() call to the persistence store
        mockOfBizStore.expectAndReturn("findAll", allComponents);

        CachingProjectComponentStore cachingProjectComponentStore = new CachingProjectComponentStore((ProjectComponentStore) mockOfBizStore.proxy());

        final Long componentID1 = new Long(1);
        final Long componentID2 = new Long(4);

        assertEquals(new Long(1000), cachingProjectComponentStore.findProjectIdForComponent(componentID1));
        assertEquals(new Long(1001), cachingProjectComponentStore.findProjectIdForComponent(componentID2));

        mockOfBizStore.verify();
    }

    public void testDeleteComponents() throws EntityNotFoundException
    {
        final Long componentID1 = new Long(1);
        final Long projectID1 = new Long(1000);
        MutableProjectComponent component1 = new MutableProjectComponent(componentID1, "Acomponent 1", null, null, AssigneeTypes.PROJECT_DEFAULT, projectID1);
        MutableProjectComponent component2 = new MutableProjectComponent(new Long(2), "Bcomponent 2", null, null, AssigneeTypes.PROJECT_DEFAULT, projectID1);
        MutableProjectComponent component3 = new MutableProjectComponent(new Long(3), "Ccomponent 3", null, null, AssigneeTypes.PROJECT_DEFAULT, projectID1);
        MutableProjectComponent component4 = new MutableProjectComponent(new Long(4), "Dcomponent 1", null, null, AssigneeTypes.PROJECT_DEFAULT, new Long(1001));
        Collection allComponents = EasyList.build(component1, component2, component3, component4);
        Mock mockOfBizStore = new Mock(ProjectComponentStore.class);

        // Mock out the initCache() call to the persistence store
        mockOfBizStore.expectAndReturn("findAll", allComponents);

        mockOfBizStore.expectVoid("delete", componentID1);

        CachingProjectComponentStore cachingProjectComponentStore = new CachingProjectComponentStore((ProjectComponentStore) mockOfBizStore.proxy());

        cachingProjectComponentStore.delete(componentID1);

        try
        {
            cachingProjectComponentStore.find(componentID1);
            fail("The component with the id '" + componentID1 + "' should not be in the store!");
        }
        catch (EntityNotFoundException expected)
        {
            assertEquals("The component with id '1' does not exist.", expected.getMessage());
        }

        try
        {
            cachingProjectComponentStore.find(componentID1);
            fail("The component with the id '" + componentID1 + "' should not be in the store!");
        }
        catch (EntityNotFoundException expected)
        {
            assertEquals("The component with id '1' does not exist.", expected.getMessage());
        }

        try
        {
            cachingProjectComponentStore.find(componentID1);
            fail("The component with the id '" + componentID1 + "' should not be in the store!");
        }
        catch (EntityNotFoundException expected)
        {
            assertEquals("The component with id '1' does not exist.", expected.getMessage());
        }

        assertEquals(false, cachingProjectComponentStore.containsName("Acomponent 1", projectID1));

        Collection components = EasyList.build(component2, component3);
        assertEquals(components, cachingProjectComponentStore.findAllForProject(projectID1));
        try
        {
            cachingProjectComponentStore.findProjectIdForComponent(componentID1);
            fail("The component with the id '" + componentID1 + "' should not be in the store!");

        }
        catch (EntityNotFoundException expected)
        {
            assertEquals("The component with the id '1' does not exist.", expected.getMessage());
        }

    }

}
