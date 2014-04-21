package com.atlassian.jira.functest.framework.suite;

import com.atlassian.jira.functest.framework.log.FuncTestOut;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.junit.runner.Description;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Batcher transform that reads current batch and number of batches from a system property.
 *
 * @see BatcherTransform
 * @since v4.4
 */
public class SystemPropertyBasedBatcherTransform extends BatcherTransform
{

    private static final String BATCH_NUMBER_PROP = "atlassian.test.suite.batch";
    private static final String NUMBER_OF_BATCHES_PROP = "atlassian.test.suite.numbatches";

    private final Predicate<Description> splittable;

    private final int numberOfBatches;
    private final int batchNumber;


    public SystemPropertyBasedBatcherTransform()
    {
        this(Predicates.<Description>alwaysTrue());
    }

    public SystemPropertyBasedBatcherTransform(Predicate<Description> splittable)
    {
        this.splittable = checkNotNull(splittable);
        this.batchNumber = getProperty(BATCH_NUMBER_PROP);
        this.numberOfBatches = getProperty(NUMBER_OF_BATCHES_PROP);
    }

    public SystemPropertyBasedBatcherTransform(Iterable<SuiteTransform> precedingTransforms, Predicate<Description> splittable)
    {
        super(precedingTransforms);
        this.splittable = checkNotNull(splittable);
        this.batchNumber = getProperty(BATCH_NUMBER_PROP);
        this.numberOfBatches = getProperty(NUMBER_OF_BATCHES_PROP);
    }

    public SystemPropertyBasedBatcherTransform(Iterable<SuiteTransform> precedingTransforms)
    {
        this(precedingTransforms, Predicates.<Description>alwaysTrue());
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

    private int getProperty(String key)
    {
        Integer val = Integer.getInteger(key);
        if (val == null)
        {
            FuncTestOut.log("'" + key + "' is not specified or invalid");
            return -1;
        }
        return val;
    }


    /**
     * Uses provided predicated to check if given suite is splittable.
     *
     * @param classTestSuite suite to check
     * @return <code>true</code> by default
     */
    @Override
    protected boolean isSplittable(Description classTestSuite)
    {
        return splittable.apply(classTestSuite);
    }
}
