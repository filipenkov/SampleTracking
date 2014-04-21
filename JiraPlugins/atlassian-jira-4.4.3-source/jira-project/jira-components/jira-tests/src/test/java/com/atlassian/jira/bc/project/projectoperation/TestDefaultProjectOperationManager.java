package com.atlassian.jira.bc.project.projectoperation;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.plugin.projectoperation.PluggableProjectOperation;
import com.atlassian.jira.plugin.projectoperation.ProjectOperationModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @since v3.12
 */
public class TestDefaultProjectOperationManager extends ListeningTestCase
{

    @Test
    public void testGetNoViewableProjectOperationDescriptors()
    {
        Mock mockPluginAccessor = new Mock(PluginAccessor.class);
        mockPluginAccessor
                .expectAndReturn("getEnabledModuleDescriptorsByClass", new Constraint[] { P.eq(ProjectOperationModuleDescriptor.class) }, Collections.EMPTY_LIST);

        DefaultProjectOperationManager vp = new DefaultProjectOperationManager((PluginAccessor) mockPluginAccessor
                .proxy());

        Collection operationDescriptors = vp.getVisibleProjectOperations(null, null);
        assertNotNull(operationDescriptors);
        assertEquals(0, operationDescriptors.size());
    }

    @Test
    public void testGetViewableProjectOperationDescriptorsWithNoPermission()
    {
        Mock mockPluginAccessor = new Mock(PluginAccessor.class);

        Mock mockPluggableProjectOperation = new Mock(PluggableProjectOperation.class);
        mockPluggableProjectOperation.expectAndReturn("showOperation", P.ANY_ARGS, Boolean.FALSE);

        ProjectOperationModuleDescriptor mockProjectOperationModuleDescriptor = getMockProjectOperationModuleDescriptor((PluggableProjectOperation) mockPluggableProjectOperation
                .proxy(), 0);

        List descriptors = new ArrayList();
        descriptors.add(mockProjectOperationModuleDescriptor);

        mockPluginAccessor
                .expectAndReturn("getEnabledModuleDescriptorsByClass", new Constraint[] { P.eq(ProjectOperationModuleDescriptor.class) }, descriptors);

        DefaultProjectOperationManager vp = new DefaultProjectOperationManager((PluginAccessor) mockPluginAccessor
                .proxy());

        Collection operationDescriptors = vp.getVisibleProjectOperations(null, null);
        assertNotNull(operationDescriptors);
        assertEquals(0, operationDescriptors.size());
    }

    @Test
    public void testGetViewableProjectOperationDescriptorsWithPermission()
    {
        Mock mockPluginAccessor = new Mock(PluginAccessor.class);

        Mock mockPluggableProjectOperation = new Mock(PluggableProjectOperation.class);
        mockPluggableProjectOperation.expectAndReturn("showOperation", P.ANY_ARGS, Boolean.TRUE);
        PluggableProjectOperation projectOperation = (PluggableProjectOperation) mockPluggableProjectOperation.proxy();

        Mock mockPluggableProjectOperation1 = new Mock(PluggableProjectOperation.class);
        mockPluggableProjectOperation1.expectAndReturn("showOperation", P.ANY_ARGS, Boolean.TRUE);
        PluggableProjectOperation projectOperation1 = (PluggableProjectOperation) mockPluggableProjectOperation1.proxy();

        Mock mockPluggableProjectOperation2 = new Mock(PluggableProjectOperation.class);
        mockPluggableProjectOperation2.expectAndReturn("showOperation", P.ANY_ARGS, Boolean.TRUE);
        PluggableProjectOperation projectOperation2 = (PluggableProjectOperation) mockPluggableProjectOperation2.proxy();

        ProjectOperationModuleDescriptor mockProjectOperationModuleDescriptor = getMockProjectOperationModuleDescriptor(projectOperation, 0);
        ProjectOperationModuleDescriptor mockProjectOperationModuleDescriptor1 = getMockProjectOperationModuleDescriptor(projectOperation1, 1);
        ProjectOperationModuleDescriptor mockProjectOperationModuleDescriptor2 = getMockProjectOperationModuleDescriptor(projectOperation2, 2);

        List descriptors = new ArrayList();
        descriptors.add(mockProjectOperationModuleDescriptor1);
        descriptors.add(mockProjectOperationModuleDescriptor2);
        descriptors.add(mockProjectOperationModuleDescriptor);

        mockPluginAccessor.expectAndReturn("getEnabledModuleDescriptorsByClass", new Constraint[] { P.eq(ProjectOperationModuleDescriptor.class) }, descriptors);

        DefaultProjectOperationManager vp = new DefaultProjectOperationManager((PluginAccessor) mockPluginAccessor.proxy());

        List operationDescriptors = vp.getVisibleProjectOperations(null, null);
        assertNotNull(operationDescriptors);
        assertEquals(3, operationDescriptors.size());
        //assert the right order is returned.
        assertEquals(mockPluggableProjectOperation, operationDescriptors.get(0));
        assertEquals(mockPluggableProjectOperation1, operationDescriptors.get(1));
        assertEquals(mockPluggableProjectOperation2, operationDescriptors.get(2));
    }

    /**
     * Test that moduels that throw exceptions dring load are ignored
     */
    @Test
    public void testLoadProjectOperationDescriptorsWithExceptions()
    {
        Mock mockPluginAccessor = new Mock(PluginAccessor.class);

        Mock mockPluggableProjectOperation = new Mock(PluggableProjectOperation.class);
        mockPluggableProjectOperation.expectAndReturn("showOperation", P.ANY_ARGS, Boolean.TRUE);
        PluggableProjectOperation projectOperation = (PluggableProjectOperation) mockPluggableProjectOperation.proxy();

        Mock mockPluggableProjectOperation1 = new Mock(PluggableProjectOperation.class);
        mockPluggableProjectOperation1.expectAndReturn("showOperation", P.ANY_ARGS, Boolean.TRUE);
        PluggableProjectOperation projectOperation1 = (PluggableProjectOperation) mockPluggableProjectOperation1.proxy();

        Mock mockPluggableProjectOperation2 = new Mock(PluggableProjectOperation.class);
        mockPluggableProjectOperation2.expectAndReturn("showOperation", P.ANY_ARGS, Boolean.TRUE);
        PluggableProjectOperation projectOperation2 = (PluggableProjectOperation) mockPluggableProjectOperation2.proxy();

        ProjectOperationModuleDescriptor mockProjectOperationModuleDescriptor = getMockProjectOperationModuleDescriptor(projectOperation, 0);
        ProjectOperationModuleDescriptor mockProjectOperationModuleDescriptorWithException = getMockProjectOperationModuleDescriptor(projectOperation1, 1, true);
        ProjectOperationModuleDescriptor mockProjectOperationModuleDescriptor2 = getMockProjectOperationModuleDescriptor(projectOperation2, 2);

        List descriptors = new ArrayList();
        descriptors.add(mockProjectOperationModuleDescriptorWithException);
        descriptors.add(mockProjectOperationModuleDescriptor2);
        descriptors.add(mockProjectOperationModuleDescriptor);

        mockPluginAccessor.expectAndReturn("getEnabledModuleDescriptorsByClass", new Constraint[] { P.eq(ProjectOperationModuleDescriptor.class) }, descriptors);

        DefaultProjectOperationManager vp = new DefaultProjectOperationManager((PluginAccessor) mockPluginAccessor.proxy());

        List operationDescriptors = vp.getVisibleProjectOperations(null, null);
        assertNotNull(operationDescriptors);
        assertEquals(2, operationDescriptors.size());
        //assert the right order is returned. and the exception throwing module is rejected
        assertEquals(mockPluggableProjectOperation, operationDescriptors.get(0));
        assertEquals(mockPluggableProjectOperation2, operationDescriptors.get(1));
    }

    private ProjectOperationModuleDescriptor getMockProjectOperationModuleDescriptor(PluggableProjectOperation pluggableProjectOperation, int order)
    {
        return getMockProjectOperationModuleDescriptor(pluggableProjectOperation, order, false);
    }

    private ProjectOperationModuleDescriptor getMockProjectOperationModuleDescriptor(PluggableProjectOperation pluggableProjectOperation, int order, boolean throwExceptionOnGetModule)
    {
        MockControl mockProjectOperationModuleDescriptorControl = MockClassControl.createControl(ProjectOperationModuleDescriptor.class);
        ProjectOperationModuleDescriptor mockProjectOperationModuleDescriptor = (ProjectOperationModuleDescriptor) mockProjectOperationModuleDescriptorControl.getMock();
        if (throwExceptionOnGetModule)
        {
            mockProjectOperationModuleDescriptor.getOrder();
            mockProjectOperationModuleDescriptorControl.setDefaultReturnValue(order);

            mockProjectOperationModuleDescriptor.getModule();
            //noinspection ThrowableInstanceNeverThrown
            mockProjectOperationModuleDescriptorControl.setDefaultThrowable(new RuntimeException("You asked for it!"));

            mockProjectOperationModuleDescriptor.getCompleteKey();
            mockProjectOperationModuleDescriptorControl.setDefaultReturnValue("Bad news bear!");
        }
        else
        {
            mockProjectOperationModuleDescriptor.getModule();
            mockProjectOperationModuleDescriptorControl.setReturnValue(pluggableProjectOperation);
            mockProjectOperationModuleDescriptor.getOrder();
            mockProjectOperationModuleDescriptorControl.setDefaultReturnValue(order);
        }
        mockProjectOperationModuleDescriptorControl.replay();

        return mockProjectOperationModuleDescriptor;
    }
}
