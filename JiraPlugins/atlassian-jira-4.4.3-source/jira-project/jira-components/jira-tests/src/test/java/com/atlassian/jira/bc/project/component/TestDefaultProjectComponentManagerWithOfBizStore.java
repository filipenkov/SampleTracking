package com.atlassian.jira.bc.project.component;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;

public class TestDefaultProjectComponentManagerWithOfBizStore extends LegacyJiraMockTestCase
{

    protected ProjectComponentManagerTester tester;
    GenericValue mockComponent = createMockGenericValue(1000, "test 1", "just a test", null, new Long(1), AssigneeTypes.PROJECT_DEFAULT);
    // Init delegator that will have one component stored on test completion
    protected MockOfBizDelegator singleComponentOfBizDelegator = new MockOfBizDelegator(null, EasyList.build(mockComponent));


    protected void tearDown() throws Exception
    {
        tester = null;
    }

    public void testCreateSuccess()
    {
        MockGenericValue v1 = createMockGenericValue(1001, ProjectComponentManagerTester.UNIQUE_COMPONENT_NAME, null, null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        // Delegator is expected to return a component
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator(null, EasyList.build(mockComponent, v1));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator));
        tester.testCreateSuccess();
        ofBizDelegator.verifyAll();
    }

    public void testFindAllForProject()
    {
        MockGenericValue v1 = createMockGenericValue(1001, "pc1", "ptest1", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v2 = createMockGenericValue(1002, "pc2", "ptest2", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v3 = createMockGenericValue(1003, "pc3", "ptest3", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator(null, EasyList.build(mockComponent, v1, v2, v3));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator));
        tester.testFindAllForProject1();
        ofBizDelegator.verifyAll();

        ofBizDelegator = new MockOfBizDelegator(null, EasyList.build(mockComponent, v1, v2, v3));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator));
        tester.testFindAllForProject2();
        ofBizDelegator.verifyAll();

        v1 = createMockGenericValue(1001, "pc1", "ptest1", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        v2 = createMockGenericValue(1002, "pc2", "ptest2", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        v3 = createMockGenericValue(1003, "pc3", "ptest3", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my1 = createMockGenericValue(1004, "my1", "ptest1", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my2 = createMockGenericValue(1005, "my2", "ptest2", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my3 = createMockGenericValue(1006, "my3", "ptest3", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        ofBizDelegator = new MockOfBizDelegator(null, EasyList.build(mockComponent, v1, v3, my1, my2, my3));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator));
        tester.testFindAllForProject3();
        ofBizDelegator.verifyAll();

        ofBizDelegator = new MockOfBizDelegator(null, EasyList.build(my1, my2, my3));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator));
        tester.testFindAllForProject4();
        ofBizDelegator.verifyAll();

        ofBizDelegator = new MockOfBizDelegator(null, null);
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator));
        tester.testFindAllForProject5();
        ofBizDelegator.verifyAll();

    }

    public void testFindAll()
    {
        MockGenericValue v1 = createMockGenericValue(1001, "pc1", "ptest1", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v2 = createMockGenericValue(1002, "pc2", "ptest2", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v3 = createMockGenericValue(1003, "pc3", "ptest3", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my1 = createMockGenericValue(1004, "my1", "ptest1", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my2 = createMockGenericValue(1005, "my2", "ptest2", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my3 = createMockGenericValue(1006, "my3", "ptest3", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator(null, EasyList.build(mockComponent, v1, v2, v3, my1, my2, my3));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator));
        tester.testFindAll();     
    }

    public void testCreateAndDelete()
    {
        MockGenericValue v1 = createMockGenericValue(1007, "pc1", "ptest1", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v2 = createMockGenericValue(1008, "pc2", "ptest2", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v3 = createMockGenericValue(1009, "pc3", "ptest3", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my1 = createMockGenericValue(1010, "my1", "test1", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my2 = createMockGenericValue(1011, "my2", "test2", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my3 = createMockGenericValue(1012, "my3", "test3", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator(null, EasyList.build(v1, v2, v3, my1, my2, my3));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator));
        tester.testCreateAndDelete();
        ofBizDelegator.verifyAll();
    }

    public void testFindByComponentName() throws Exception
    {
        MockGenericValue v1 = createMockGenericValue(1001, "name", "ptest1", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v2 = createMockGenericValue(1002, "pc2", "ptest2", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v3 = createMockGenericValue(1003, "pc3", "ptest3", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my1 = createMockGenericValue(1004, "name", "ptest1", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my2 = createMockGenericValue(1005, "my2", "ptest2", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue my3 = createMockGenericValue(1006, "my3", "ptest3", null, ProjectComponentManagerTester.MY_PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator(null, EasyList.build(mockComponent, v1, v2, v3, my1, my2, my3));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator));
        tester.testFindByComponentName();
    }

    public void testUpdate()
    {
        MockGenericValue v1 = createMockGenericValue(1000, "test 2", "another test", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator(null, EasyList.build(v1));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator));
        tester.testUpdateSingleComponent();
        ofBizDelegator.verifyAll();

        tester = new ProjectComponentManagerTester(createStore(singleComponentOfBizDelegator));
        tester.testUpdateNonPersisted();
        singleComponentOfBizDelegator.verifyAll();

        v1 = createMockGenericValue(1001, "c1", "test1", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v2 = createMockGenericValue(1002, "c2", "test2", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        MockGenericValue v3 = createMockGenericValue(1003, "noname", "test3", null, ProjectComponentManagerTester.PROJECT_ID_STORED, AssigneeTypes.PROJECT_DEFAULT);
        ofBizDelegator = new MockOfBizDelegator(null, EasyList.build(mockComponent, v1, v2, v3));
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator));
        tester.testUpdateIsConsistent();
        ofBizDelegator.verifyAll();
    }

    public void testDelete()
    {
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator(null, null);
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator));
        tester.testDelete();
        ofBizDelegator.verifyAll();
    }

    public void testDeleteAndUpdate()
    {
        MockOfBizDelegator ofBizDelegator = new MockOfBizDelegator(null, null);
        tester = new ProjectComponentManagerTester(createStore(ofBizDelegator));
        tester.testDeleteAndUpdate();
        ofBizDelegator.verifyAll();
    }

    private MockGenericValue createMockGenericValue(int id, String name, String description, String lead, Long projectId, long assigneeType)
    {
        return new MockGenericValue("Component", EasyMap.build("id", new Long(id), "name", name, "description", description, "lead", lead, "project", projectId, "assigneetype", new Long(assigneeType)));
    }

    protected ProjectComponentStore createStore(MockOfBizDelegator ofBizDelegator)
    {
        return new OfBizProjectComponentStore(ofBizDelegator);
    }

}
