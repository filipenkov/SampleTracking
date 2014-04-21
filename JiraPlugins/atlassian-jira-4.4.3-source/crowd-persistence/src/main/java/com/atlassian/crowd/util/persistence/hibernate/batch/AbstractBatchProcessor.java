package com.atlassian.crowd.util.persistence.hibernate.batch;

import com.atlassian.crowd.util.BatchResult;
import com.atlassian.crowd.util.Percentage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Threadsafe batch processor.
 * <p/>
 * This processor is essentially a heavyweight generic DAO for
 * processing batched operations over a collection of entities.
 * <p/>
 * The {@code batchSize}  defaults to {@literal batchSize}  and can
 * be manually set (via Spring, for example) using the appropriate
 * setter method. The {@code batchSize}  should match the
 * {@code hibernate.jdbc.batch_size} property defined in the
 * Hibernate configuration.
 * <p/>
 * Each batch operation is first divided into smaller sets of
 * {@code batchSize}. If there is an error in processing the batch,
 * the batched JDBC call is rolled-back and the batch is
 * processed individually.
 * <p/>
 * This mechanism ensures very fast (JDBC-batched) inserts and
 * updates and follows it up with a fail-over retry for the
 * failing batches.
 *
 * Callback methods are provided to allow you include the session
 * and transaction management you desire. The processing flow is:
 *
 * <code>
 * call beforeProcessCollection()
 * For each item in the collection
 *     Add the item to the batch collection
 *     If the batch collection size >= the batch size
 *         call beforeProcessBatch()
 *         For each item in the batch collection
 *            perform the operation on the item
 *         call afterProcessBatch() or rollbackProcessBatch() if an error occured
 *         clear the batch collection
 * If there are unprocessed items in the Batch Collcetion
 *     call beforeProcessBatch()
 *     For each item in the batch collection
 *        perform the operation on the item
 *     call afterProcessBatch() or rollbackProcessBatch() if an error occured
 * call beforeProcessCollection()
 * </code>
 *
 * If an error occured during the processing of a batch collection then:
 * <code>
 * For each item in the batch collection
 *    call beforeProcessIndividual()
 *    perform the operation on the item
 *    call afterProcessIndividual() or rollbackProcessIndividual() if an error occured
 * </code>
 *
 * <p/>
 * NOTE 1: Do not use this if you're database is not transactional,
 * *stab* MySQL ISAM.
 *
 * @author Shihab Hamid, Matthew Jensen
 */

public abstract class AbstractBatchProcessor implements BatchProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(AbstractBatchProcessor.class);

    private int batchSize = 20;

    public AbstractBatchProcessor()
    {
    }

    /**
     * The set is first divided into smaller sets of <code>batchSize</code>.
     * Each batchSet is added via a batched JDBC call (using Hibernate's
     * batching mechanism). If there is an error in processing the batch,
     * the batched JDBC call is rolled-back and the batchSet is processed
     * individually.
     * <p/>
     * This mechanism ensures very fast (JDBC-batched) inserts and
     * updates and follows it up with a fail-over retry for the
     * failing batches.
     * <p/>
     * NOTE: do not use this if you're database is not transactional,
     * *stab* MySQL ISAM.
     *
     * @param op      Hibernate operation to perform (eg. replicate, saveOrUpdate).
     * @param objects set of <code>DirectoryEntity</code> objectst to
     *                batch add.
     * @return batch result.
     */
    public final <E extends Serializable> BatchResult<E> execute(HibernateOperation op, Collection<E> objects)
    {
        int numberOfObjects = objects.size();
        int numberOfBatches = (numberOfObjects / batchSize) + (numberOfObjects % batchSize > 0 ? 1 : 0);

        logger.debug("processing [ {} ] objects in [ {} ] batches of [ {} ] with [ {} ]",
                new Object[]{numberOfObjects, numberOfBatches, batchSize, op.getClass().getName()});

        BatchResult<E> result = new BatchResult<E>(objects.size());

        beforeProcessCollection();
        int currentBatch = 0;
        try
        {

            List<E> batch = new ArrayList<E>(batchSize);
            for (E object : objects)
            {
                batch.add(object);
                if (batch.size() == batchSize)
                {
                    currentBatch++;
                    processBatch(result, op, batch);
                    logger.info("processed batch [ {} ] of [ {} ] [ {}% ]",
                            new Object[]{currentBatch, numberOfBatches, Percentage.get(currentBatch, numberOfBatches)});
                    batch.clear();
                }
            }

            // process last batch (might be less than batchSize)
            if (!batch.isEmpty())
            {
                currentBatch++;
                processBatch(result, op, batch);
                batch.clear();
                logger.info("processed batch [ {} ] of [ {} ] [ {} % ]",
                        new Object[]{currentBatch, numberOfBatches, Percentage.get(currentBatch, numberOfBatches)});
            }
        }
        finally
        {
            afterProcessCollection();
        }

        return result;
    }

    // returns collection of failures
    private <E extends Serializable> void processBatch(BatchResult<E> result, HibernateOperation op, List<E> objects)
    {
        int count = 0;
        int numberOfObjects = objects.size();

        try
        {
            beforeProcessBatch();
            for (Object object : objects)
            {
                performOperation(op, object);
                logger.trace("processed [ {} ] [ {}% ]", new Object[]{object.toString(), Percentage.get(count, numberOfObjects)});
            }
            afterProcessBatch();
            result.addSuccesses(objects);
        }
        catch (RuntimeException e)
        {
            logger.warn("batch failed falling back to individual processing", e);
            rollbackProcessBatch();
            // attempt to insert one by one
            processIndividual(result, op, objects);
        }
    }

    private <E extends Serializable> void processIndividual(BatchResult<E> result, HibernateOperation op, Collection<E> objects)
    {
        int numberOfObjects = objects.size();
        logger.debug("processing [ {} ] individually", numberOfObjects);

        int count = 0;
        for (E object : objects)
        {
            count++;
            try
            {
                beforeProcessIndividual();
                performOperation(op, object);
                logger.debug("processed [ {} ] [ {}% ]",
                        new Object[]{object.toString(), Percentage.get(count, numberOfObjects)});
                afterProcessIndividual();
                result.addSuccess(object);
            }
            catch (RuntimeException e)
            {
                rollbackProcessIndividual();
                result.addFailure(object);
                logger.error("Could not process " + object.getClass() + ": " + object.toString(), e);
            }
        }
    }

    private void performOperation(HibernateOperation op, Object object)
    {
        if (object instanceof TransactionGroup)
        {
            TransactionGroup transactionGroup = (TransactionGroup) object;
            op.performOperation(transactionGroup.getPrimaryObject());
            for (Object dep : transactionGroup.getDependantObjects())
            {
                op.performOperation(dep);
            }
        }
        else
        {
            op.performOperation(object);
        }
    }

    /**
     * The <code>batchSize</code> value should be the same
     * as the <code>hibernate.jdbc.batch_size</code>
     * Hibernate property.
     *
     * @param batchSize batch size used to group batches.
     */
    public void setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
    }

    /**
     * Called before the collection is processed into a batch.  Can be used to start session or transaction that will
     * cover the execution of the entire collection which could involve multiple batches.
     */
    protected abstract void beforeProcessCollection();

    /**
     * Called when processing the collection has completed successfully.
     */
    protected abstract void afterProcessCollection();

    /**
     * Called before processing each batch.  Can be used to start session or transaction that will
     * cover this particular batch.
     */
    protected abstract void beforeProcessBatch();

    /**
     * Called after successfully processing each batch.
     */
    protected abstract void afterProcessBatch();

    /**
     * Called after processing each batch where an exception was encountered.
     */
    protected abstract void rollbackProcessBatch();

    /**
     * Called before processing an individual item.  This method will be called if the batch failed and
     * the processor falls back to processing each item seperately.
     */
    protected abstract void beforeProcessIndividual();

    /**
     * Called after successully processing an item individually.
     */
    protected abstract void afterProcessIndividual();

    /**
     * Called after processing an individual item where an exception was encountered.
     */
    protected abstract void rollbackProcessIndividual();

}
