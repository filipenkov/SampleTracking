package com.atlassian.jira.plugin.util;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.mock.plugin.MockOrderableModuleDescriptor;
import com.atlassian.jira.local.ListeningTestCase;

public class TestModuleDescriptorComparator extends ListeningTestCase
{
    @Test
    public void testModuleDescriptorComparator()
    {
        ModuleDescriptorComparator comp = ModuleDescriptorComparator.COMPARATOR;

        //positive order
        MockOrderableModuleDescriptor orderP1 = new MockOrderableModuleDescriptor(0);
        MockOrderableModuleDescriptor orderP2 = new MockOrderableModuleDescriptor(10);

        //negative order
        MockOrderableModuleDescriptor orderN1 = new MockOrderableModuleDescriptor(-10);
        MockOrderableModuleDescriptor orderN2 = new MockOrderableModuleDescriptor(-20);

        //both order is positive (not equal)
        assertTrue(comp.compare(orderP1, orderP2) < 0);
        assertTrue(comp.compare(orderP2, orderP1) > 0);

        //one negative, one positive (negative is greater)
        assertTrue(comp.compare(orderP1, orderN1) > 0);
        assertTrue(comp.compare(orderN1, orderP1) < 0);

        //both order is negative (not equal)
        assertTrue(comp.compare(orderN1, orderN2) > 0);
        assertTrue(comp.compare(orderN2, orderN1) < 0);

        //both order is equal
        assertTrue(comp.compare(orderP1, orderP1) == 0);
        assertTrue(comp.compare(orderN1, orderN1) == 0);
    }
}
