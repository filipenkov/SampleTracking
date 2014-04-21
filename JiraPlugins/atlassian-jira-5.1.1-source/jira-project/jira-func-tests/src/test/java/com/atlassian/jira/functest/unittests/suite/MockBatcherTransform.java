package com.atlassian.jira.functest.unittests.suite;

import com.atlassian.jira.functest.framework.suite.BatcherTransform;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.apache.commons.lang.ArrayUtils;
import org.junit.runner.Description;

import javax.annotation.Nullable;

/**
* Extension for batcher transform for tests.
*
* @since v4.4
*/
class MockBatcherTransform extends BatcherTransform
{

    public static MockBatcherTransform forNotSplittableClasses(int batchNumber, int numberOfBatches, Class<?>... nonSplittable)
    {
        return new MockBatcherTransform(batchNumber, numberOfBatches, notSplittableFor(nonSplittable));
    }

    private static Predicate<Description> notSplittableFor(final Class<?>... notSplittableClasses)
     {
         return new Predicate<Description>()
         {
             @Override
             public boolean apply(@Nullable Description description)
             {
                 return !ArrayUtils.contains(notSplittableClasses, description.getTestClass());
             }
         };
     }


    private final int batchNumber;
    private final int numberOfBatches;
    private final Predicate<Description> splittable;

    public MockBatcherTransform(int batchNumber, int numberOfBatches, Predicate<Description> splittable)
    {
        this.batchNumber = batchNumber;
        this.numberOfBatches = numberOfBatches;
        this.splittable = splittable;
    }

    public MockBatcherTransform(int batchNumber, int numberOfBatches)
    {
        this(batchNumber, numberOfBatches, Predicates.<Description>alwaysTrue());
    }

    @Override
    protected int batchNumber()
    {
        return batchNumber;
    }

    @Override
    protected int numberOfBatches()
    {
        return numberOfBatches;
    }

    @Override
    protected boolean isSplittable(Description classTestSuite)
    {
        return splittable.apply(classTestSuite);
    }
}
