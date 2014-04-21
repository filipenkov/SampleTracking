package com.atlassian.jira.plugin.util;

import com.atlassian.jira.plugin.OrderableModuleDescriptor;

import java.util.Comparator;

/**
 * Compares Module Descriptors that implement {@see OrderableModuleDescriptor}
 */
public class ModuleDescriptorComparator implements Comparator<OrderableModuleDescriptor>
{
    public static final ModuleDescriptorComparator COMPARATOR = new ModuleDescriptorComparator();

    public int compare(final OrderableModuleDescriptor descriptor1, final OrderableModuleDescriptor descriptor2)
    {
        final int order1 = descriptor1.getOrder();
        final int order2 = descriptor2.getOrder();

        if (order1 == order2)
        {
            return 0;
        }
        else if (order1 < order2)
        {
            return -1;
        }

        return 1;
    }

    public int newCompare(final Object o1, final Object o2)
    {
        if ((o1 instanceof OrderableModuleDescriptor) && (o2 instanceof OrderableModuleDescriptor))
        {
            final OrderableModuleDescriptor descriptor1 = (OrderableModuleDescriptor) o1;
            final OrderableModuleDescriptor descriptor2 = (OrderableModuleDescriptor) o2;

            final int order1 = descriptor1.getOrder();
            final int order2 = descriptor2.getOrder();

            //treat negative numbers > positive numbers, to put them at the end as follows:
            //0, 1, 2 ... infinity ... -1, -2, ... negative infinity
            if (order1 == order2)
            {
                return 0;
            }
            else if (order1 >= 0) //order1 is positive
            {
                if ((order2 < 0) || (order1 < order2))
                {
                    return -1;
                }
            }
            else if (order2 >= 0) //order1 is negative, order2 is positive
            {
                return 1;
            }
            else
            //both negative
            {
                if (order1 > order2)
                {
                    return -1;
                }
            }

            return 1;
        }
        else
        {
            throw new IllegalArgumentException(
                "This comparator can only compare objects of type '" + OrderableModuleDescriptor.class.getName() + "'." + "  Instead passed " + o1 + " " + o2);
        }
    }

}
