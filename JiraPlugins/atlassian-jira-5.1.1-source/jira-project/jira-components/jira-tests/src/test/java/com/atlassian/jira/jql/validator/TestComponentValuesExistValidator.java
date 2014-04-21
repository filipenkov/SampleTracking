package com.atlassian.jira.jql.validator;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.resolver.ComponentIndexInfoResolver;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestComponentValuesExistValidator extends MockControllerTestCase
{
    private JqlOperandResolver operandResolver;
    private ComponentIndexInfoResolver indexInfoResolver;
    private PermissionManager permissionManager;
    private ProjectComponentManager componentManager;
    private ProjectManager projectManager;
    private I18nHelper.BeanFactory beanFactory;

    @Before
    public void setUp()
    {
        operandResolver = mockController.getMock(JqlOperandResolver.class);
        indexInfoResolver = mockController.getMock(ComponentIndexInfoResolver.class);
        permissionManager = mockController.getMock(PermissionManager.class);
        componentManager = mockController.getMock(ProjectComponentManager.class);
        projectManager = mockController.getMock(ProjectManager.class);
        beanFactory = mockController.getMock(I18nHelper.BeanFactory.class);
    }

    @Test
    public void testNoComponentsExists() throws Exception
    {
        String name = "blah";

        indexInfoResolver.getIndexedValues(name);
        mockController.setReturnValue(CollectionBuilder.newBuilder().asList());

        ComponentValuesExistValidator validator = mockController.instantiate(ComponentValuesExistValidator.class);
        assertFalse(validator.stringValueExists(null, name));

        mockController.verify();
    }

    @Test
    public void testComponentExistsAndHasPermission() throws Exception
    {
        String name = "blah";
        Long id = 10L;
        Long pid = 20L;
        MockProject project = new MockProject(pid);
        MockComponent component = new MockComponent(id, name, pid);

        indexInfoResolver.getIndexedValues(name);
        mockController.setReturnValue(CollectionBuilder.newBuilder(id.toString()).asList());

        componentManager.find(id);
        mockController.setReturnValue(component);

        projectManager.getProjectObj(pid);
        mockController.setReturnValue(project);

        permissionManager.hasPermission(Permissions.BROWSE, project, (User) null);
        mockController.setReturnValue(true);

        ComponentValuesExistValidator validator = mockController.instantiate(ComponentValuesExistValidator.class);
        assertTrue(validator.stringValueExists(null, name));

        mockController.verify();
    }

    @Test
    public void testComponentExistsAndNoHasPermission() throws Exception
    {
        String name = "blah";
        Long id = 10L;
        Long pid = 20L;
        MockProject project = new MockProject(pid);
        MockComponent component = new MockComponent(id, name, pid);

        indexInfoResolver.getIndexedValues(name);
        mockController.setReturnValue(CollectionBuilder.newBuilder(id.toString()).asList());

        componentManager.find(id);
        mockController.setReturnValue(component);

        projectManager.getProjectObj(pid);
        mockController.setReturnValue(project);

        permissionManager.hasPermission(Permissions.BROWSE, project, (User) null);
        mockController.setReturnValue(false);

        ComponentValuesExistValidator validator = mockController.instantiate(ComponentValuesExistValidator.class);
        assertFalse(validator.stringValueExists(null, name));

        mockController.verify();
    }

    @Test
    public void testTwoComponentsExistsAndOneHasPermission() throws Exception
    {
        String name = "blah";
        Long id1 = 10L;
        Long id2 = 10L;
        Long pid1 = 20L;
        Long pid2 = 20L;
        MockProject project1 = new MockProject(pid1);
        MockProject project2 = new MockProject(pid2);
        MockComponent component1 = new MockComponent(id1, name, pid1);
        MockComponent component2 = new MockComponent(id2, name, pid2);

        indexInfoResolver.getIndexedValues(name);
        mockController.setReturnValue(CollectionBuilder.newBuilder(id1.toString(), id2.toString()).asList());

        componentManager.find(id1);
        mockController.setReturnValue(component1);

        projectManager.getProjectObj(pid1);
        mockController.setReturnValue(project1);

        permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null);
        mockController.setReturnValue(false);

        componentManager.find(id2);
        mockController.setReturnValue(component2);

        projectManager.getProjectObj(pid2);
        mockController.setReturnValue(project2);

        permissionManager.hasPermission(Permissions.BROWSE, project2, (User) null);
        mockController.setReturnValue(true);

        ComponentValuesExistValidator validator = mockController.instantiate(ComponentValuesExistValidator.class);
        assertTrue(validator.stringValueExists(null, name));

        mockController.verify();
    }

    @Test
    public void testLongValueExist() throws Exception
    {
        Long id = 10L;
        indexInfoResolver.getIndexedValues(id);
        mockController.setReturnValue(Collections.emptyList());
        indexInfoResolver.getIndexedValues(id);
        mockController.setReturnValue(Collections.emptyList());
        mockController.replay();

        final AtomicInteger called = new AtomicInteger(0);
        ComponentValuesExistValidator validator = new ComponentValuesExistValidator(operandResolver, indexInfoResolver, permissionManager, componentManager, projectManager, beanFactory)
        {
            @Override
            boolean componentExists(final User searcher, final List<String> ids)
            {
                return called.incrementAndGet() == 1;
            }
        };

        assertTrue(validator.longValueExist(null, id));
        assertFalse(validator.longValueExist(null, id));

        assertEquals(2, called.get());
        mockController.verify();
    }

    static class MockComponent implements ProjectComponent
    {
        private final Long id;
        private final String name;
        private final Long projectId;

        MockComponent(Long id, String name, Long projectId)
        {
            this.id = id;
            this.name = name;
            this.projectId = projectId;
        }

        public String getName()
        {
            return name;
        }

        public Long getId()
        {
            return id;
        }

        public Long getProjectId()
        {
            return projectId;
        }

        public String getDescription()
        {
            return null;
        }

        public String getLead()
        {
            return null;
        }

        public long getAssigneeType()
        {
            return 0;
        }

        public GenericValue getGenericValue()
        {
            return null;
        }
    }
}
