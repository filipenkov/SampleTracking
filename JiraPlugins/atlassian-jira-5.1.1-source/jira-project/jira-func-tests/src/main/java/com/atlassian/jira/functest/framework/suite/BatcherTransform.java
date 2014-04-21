package com.atlassian.jira.functest.framework.suite;

import com.atlassian.jira.functest.framework.log.FuncTestOut;
import com.atlassian.jira.functest.framework.util.junit.AnnotatedDescription;
import com.atlassian.jira.functest.framework.util.junit.DescriptionWalker;
import com.atlassian.jira.functest.framework.util.junit.JUnitPredicates;
import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.NotNull;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.hamcrest.StringDescription;
import org.junit.Ignore;
import org.junit.runner.Description;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.atlassian.jira.functest.framework.suite.TransformingParentRunner.applyTransforms;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.not;

/**
 * <p/>
 * A {@link com.atlassian.jira.functest.framework.suite.SuiteTransform} for batching tests.
 *
 * <p/>
 * NOTE: this class is stateful, as it has to drill down the description tree to find single test cases to batch. Use
 * single instance of this class (or any subclass) per transforming runner. It also needs to know about any preceding
 * transforms that modify the list of descriptions to work correctly.
 *
 * @since v4.4
 */
public abstract class BatcherTransform implements SuiteTransform
{
    static final Predicate<Description> IS_RUN_FIRST = new Predicate<Description>()
    {
        @Override
        public boolean apply(@Nullable Description input)
        {
            return new AnnotatedDescription(input).hasAnnotation(RunFirst.class);
        }
    };

    // previous transforms are necessary because this transform computes children in advance
    protected final Iterable<SuiteTransform> precedingTransforms;

    protected BatcherTransform()
    {
        this(Collections.<SuiteTransform>emptyList());
    }

    protected BatcherTransform(Iterable<SuiteTransform> previousTransforms)
    {
        this.precedingTransforms = ImmutableList.copyOf(previousTransforms);
    }

    private boolean firstRun = true;
    private Iterable<TestWithParents> batch;

    @Override
    public Iterable<Description> apply(@Nullable Iterable<Description> input)
    {
        if (batch != null)
        {
            return descriptionsMatchingBatch(input);
        }
        BatchValidator validator = new BatchValidator(batchNumber(), numberOfBatches());
        if (!validator.shouldBatch())
        {
            if (firstRun)
            {
                FuncTestOut.log("***** Running in non-batched mode *****");
                firstRun = false;
            }
            return input;
        }
        Batcher batcher = new Batcher(batchNumber(), numberOfBatches(), input);
        batch = batcher.batch();
        if (firstRun)
        {
            FuncTestOut.log(String.format("***** Running batch %d of %d *****", batcher.batchNumber, batcher.numberOfBatches));
            firstRun = false;
        }
        return descriptionsMatchingBatch(input);
    }

    private Iterable<Description> descriptionsMatchingBatch(Iterable<Description> input)
    {
        ImmutableSet.Builder<Description> builder = ImmutableSet.builder();
        // we need to preserve order of batch!
        for (TestWithParents batched : batch)
        {
            for (Description inputDescription : input)
            {
                if (batched.matches(inputDescription))
                {
                    builder.add(inputDescription);
                    break;
                }
            }
        }
        return ImmutableList.copyOf(builder.build());
    }


    /**
     * Batch number to execute.
     *
     * @return batch number
     */
    protected abstract int batchNumber();

    /**
     * <p>
     * Total number of batches.
     *
     * <p>
     * 0 or less means no batching
     *
     * @return total number of batches
     */
    protected abstract int numberOfBatches();


    /**
     * Checks if given description representing class test suite is splittable.
     *
     * @param classTestSuite suite to check
     * @return <code>true</code>, if the suite is splittable
     */
    protected abstract boolean isSplittable(Description classTestSuite);



    private static abstract class BatchManipulator
    {
        protected final int batchNumber;
        protected final int numberOfBatches;

        public BatchManipulator(int batchNumber, int numberOfBatches)
        {
            this.batchNumber = batchNumber;
            this.numberOfBatches = numberOfBatches;
        }
    }

    private static class BatchValidator extends BatchManipulator
    {

        public BatchValidator(int batchNumber, int numberOfBatches)
        {
            super(batchNumber, numberOfBatches);
        }

        boolean shouldBatch()
        {
            if (numberOfBatches <= 0)
            {
                return false;
            }
            checkState(batchNumber > 0, "Batch number <" + batchNumber + "> must be greater than 0");
            checkState(batchNumber <= numberOfBatches, "Batch number <" + batchNumber + "> must be greater than 0");
            return true;
        }
    }


    private class Batcher extends BatchManipulator
    {
        private final Iterable<Description> input;
        private final List<TestWithParents> singleTests;
        private final List<TestWithParents> runFirstTests;
        private int currentBatchSize;
        private int currentBatchNumber = 0;
        private int currentTestIndex;
        private int currentRunFirstIndex;
        private int batchesLeft;
        private List<TestWithParents> currentBatch;
        private int testsInBatchLeft;



        public Batcher(int batchNumber, int numberOfBatches, Iterable<Description> input)
        {
            super(batchNumber, numberOfBatches);
            int runFirstCount = countRunFirst(input);
            checkState(runFirstCount <= numberOfBatches, "Too many RUN_FIRST tests <" + runFirstCount
                    + ">, can only accomodate <" + numberOfBatches + ">");
            this.input = input;
            this.singleTests = getSingleTests();
            this.runFirstTests = getRunFirstTests();
            this.currentTestIndex = 0;
        }

        private int countRunFirst(Iterable<Description> input)
        {
            // not very intention revealing, but what, do we need a Holder<T>??? ;)
            final AtomicInteger count = new AtomicInteger();
            DescriptionWalker.walk(new Consumer<Description>()
            {
                @Override
                public void consume(@NotNull Description element)
                {
                    if (IS_RUN_FIRST.apply(element))
                    {
                        count.incrementAndGet();
                    }
                }
            }, JUnitPredicates.isTest(), Iterables.toArray(input, Description.class));
            return count.get();
        }

        private List<TestWithParents> getSingleTests()
        {
            return getMatchingTests(not(IS_RUN_FIRST));
        }

        private List<TestWithParents> getRunFirstTests()
        {
            return getMatchingTests(IS_RUN_FIRST);
        }

        private List<TestWithParents> getMatchingTests(Predicate<Description> matcher)
        {
            final List<TestWithParents> answer = Lists.newArrayList();
            for (Description description : applyPreviousTransforms(input))
            {
                answer.addAll(getTestsWithParents(description, Lists.<Description>newArrayList(), matcher));
            }
            return answer;
        }

        private List<TestWithParents> getTestsWithParents(Description test, List<Description> parents,
                Predicate<Description> included)
        {
            if (isSingleTestNotMatching(test, included) || isIgnored(test))
            {
                return Collections.emptyList();
            }
            if (test.isTest())
            {
                return ImmutableList.of(new TestWithParents(test, ImmutableList.copyOf(parents)));
            }
            else
            {
                List<TestWithParents> answer = Lists.newArrayList();
                List<Description> newParents = ImmutableList.<Description>builder().addAll(parents).add(test).build();
                for (Description child : applyPreviousTransforms(test.getChildren()))
                {
                    answer.addAll(getTestsWithParents(child, newParents, included));
                }
                return answer;
            }
        }

        private Iterable<Description> applyPreviousTransforms(Iterable<Description> descriptions)
        {
            return applyTransforms(descriptions, precedingTransforms);
        }

        private boolean isSingleTestNotMatching(Description test, Predicate<Description> included)
        {
            return test.isTest() && !included.apply(test);
        }

        private boolean isIgnored(Description test)
        {
            return test.getAnnotation(Ignore.class) != null;
        }

        public List<TestWithParents> batch()
        {
            do
            {
                startBatch();
                while (isSpaceInBatch() && hasMoreMainTests())
                {
                    addNext();
                }
            } while (currentBatchNumber < batchNumber);
            return currentBatch;
        }

        private void startBatch()
        {
            batchesLeft = numberOfBatches - currentBatchNumber;
            currentBatchNumber++;
            currentBatchSize = computeBatchSize();
            testsInBatchLeft = currentBatchSize;
            currentBatch = Lists.newArrayList();
            if (isSpaceInBatch() && hasMoreRunFirstTests())
            {
                addNextRunFirst();
            }

        }

        private boolean isSpaceInBatch()
        {
            return testsInBatchLeft > 0;
        }

        private boolean hasMoreTests()
        {
            return hasMoreMainTests() || hasMoreRunFirstTests();
        }

        private boolean hasMoreMainTests()
        {
            return currentTestIndex < singleTests.size();
        }

        private boolean hasMoreRunFirstTests()
        {
            return currentRunFirstIndex < runFirstTests.size();
        }

        private int testsLeft()
        {
            return mainTestsLeft() + runFirstTestsLeft();
        }

        private int mainTestsLeft()
        {
            return singleTests.size() - currentTestIndex;
        }

        private int runFirstTestsLeft()
        {
            return runFirstTests.size() - currentRunFirstIndex;
        }

        private TestWithParents current()
        {
            return singleTests.get(currentTestIndex);
        }

        private TestWithParents currentRunFirst()
        {
            return runFirstTests.get(currentRunFirstIndex);
        }

        private int computeBatchSize()
        {
            if (!hasMoreTests())
            {
                return 0;
            }
            int size = testsLeft() / batchesLeft;
            if (testsLeft() % batchesLeft > 0)
            {
                size++;
            }
            return size;
        }

        private void addNext()
        {
            if (!isSplittable(current().directParent()))
            {
                addNextClassSuite();
            }
            else
            {
                addNextSingleTest();
            }
        }

        private void addNextRunFirst()
        {
            currentBatch.add(currentRunFirst());
            testsInBatchLeft--;
            currentRunFirstIndex++;
        }

        private void addNextClassSuite()
        {
            Description parent = current().directParent();
            while (hasMoreTests() && current().matches(parent))
            {
                addNextSingleTest();
            }
        }

        private void addNextSingleTest()
        {
            currentBatch.add(current());
            testsInBatchLeft--;
            currentTestIndex++;
        }
    }


    /**
     * Contains description of a single test and all his parents in the test suite tree.
     *
     */
    private static class TestWithParents
    {
        public final Description test;
        public final List<Description> parents;

        TestWithParents(Description test, List<Description> parents)
        {
            this.test = test;
            this.parents = ImmutableList.copyOf(parents);
        }

        boolean matches(Description test)
        {
            return this.test.equals(test) || parents.contains(test);
        }

        public Description directParent()
        {
            if (parents.size() > 0)
            {
                return parents.get(parents.size()-1);
            }
            else
            {
                return null;
            }

        }

        @Override
        public String toString()
        {
            return new StringDescription().appendValue(test).appendText("\nParents:").appendValue(parents).toString();
        }
    }
}
