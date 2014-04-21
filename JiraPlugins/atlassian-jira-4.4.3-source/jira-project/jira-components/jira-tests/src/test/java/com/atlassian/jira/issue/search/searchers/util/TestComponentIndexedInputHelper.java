package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.MockProjectComponent;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.query.operand.SingleValueOperand;

/**
 * @since v4.0
 */
public class TestComponentIndexedInputHelper extends MockControllerTestCase
{
    private NameResolver<ProjectComponent> componentResolver;
    private ComponentIndexedInputHelper helper;

    @Before
    public void setUp() throws Exception
    {
        componentResolver = getMock(NameResolver.class);
    }

    @Test
    public void testCreateSingleValueOperandFromIdIsntNumber() throws Exception
    {
        helper = instantiate(ComponentIndexedInputHelper.class);

        assertEquals(new SingleValueOperand("test"), helper.createSingleValueOperandFromId("test"));
    }

    @Test
    public void testCreateSingleValueOperandFromIdIsNotAComponent() throws Exception
    {
        expect(componentResolver.get(123l))
                .andReturn(null);

        helper = instantiate(ComponentIndexedInputHelper.class);

        assertEquals(new SingleValueOperand(123l), helper.createSingleValueOperandFromId("123"));
    }

    @Test
    public void testCreateSingleValueOperandFromIdIsAVersion() throws Exception
    {
        expect(componentResolver.get(123l))
                .andReturn(new MockProjectComponent(123l,"Component 1"));

        helper = instantiate(ComponentIndexedInputHelper.class);

        assertEquals(new SingleValueOperand("Component 1"), helper.createSingleValueOperandFromId("123"));
    }

}
