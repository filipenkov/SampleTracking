package com.atlassian.jira.functest.unittests.suite;

import com.atlassian.jira.functest.unittests.suite.MockJUnit4TestClasses.FullyIgnoredTest;
import com.atlassian.jira.functest.unittests.suite.MockJUnit4TestClasses.PartiallyIgnoredTest;
import com.atlassian.jira.functest.unittests.suite.MockJUnit4TestClasses.TestFive;
import com.atlassian.jira.functest.unittests.suite.MockJUnit4TestClasses.TestFour;
import com.atlassian.jira.functest.unittests.suite.MockJUnit4TestClasses.TestOne;
import com.atlassian.jira.functest.unittests.suite.MockJUnit4TestClasses.TestThree;
import com.atlassian.jira.functest.unittests.suite.MockJUnit4TestClasses.TestTwo;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.JUnit4;
import org.junit.runners.model.InitializationError;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;

import static com.atlassian.jira.functest.unittests.suite.MockBatcherTransform.forNotSplittableClasses;
import static org.junit.Assert.assertEquals;

/**
 * Test for {@link com.atlassian.jira.functest.framework.suite.BatcherTransform}.
 *
 * @since 4.4
 */
public class TestBatcherTransform
{

    @Test
    public void shouldNotApplyBatchingGivenNumberOfBatchesLessThanOne() throws Exception
    {
        MockBatcherTransform tested = new MockBatcherTransform(0, 0);
        Iterable<Description> input = suitesOf(TestOne.class, TestTwo.class);
        assertEquals(input, tested.apply(input));
        tested = new MockBatcherTransform(-1, -1);
        assertEquals(input, tested.apply(input));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldRaiseExceptionGivenValidNumberOfBatchesButInvalidBatchNumber() throws Exception
    {
        MockBatcherTransform tested = new MockBatcherTransform(-1, 10);
        Iterable<Description> input = suitesOf(TestOne.class, TestTwo.class);
        tested.apply(input);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldRaiseExceptionGivenValidNumberOfBatchesButZeroBatchNumber() throws Exception
    {
        MockBatcherTransform tested = new MockBatcherTransform(0, 10);
        Iterable<Description> input = suitesOf(TestOne.class, TestTwo.class);
        tested.apply(input);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldRaiseExceptionGivenBatchNumberGreaterThanNumberOfBatches() throws Exception
    {
        MockBatcherTransform tested = new MockBatcherTransform(11, 10);
        Iterable<Description> input = suitesOf(TestOne.class, TestTwo.class);
        tested.apply(input);
    }

    @Test
    public void shouldProcessEmptyTestCollectionInNonBatchingMode()
    {
        MockBatcherTransform tested = new MockBatcherTransform(-1, -1);
        assertEquals(0, Iterables.size(tested.apply(Collections.<Description>emptyList())));
    }

    @Test
    public void shouldProcessEmptyTestCollectionInBatchingMode()
    {
        MockBatcherTransform tested = new MockBatcherTransform(1, 5);
        assertEquals(0, Iterables.size(tested.apply(Collections.<Description>emptyList())));
    }

    @Test
    public void shouldCreateOneBatchWithAllTests()
    {
        MockBatcherTransform tested = new MockBatcherTransform(1, 1);
        Iterable<Description> input = suitesOf(TestOne.class, TestTwo.class);
        assertEquals(Lists.newArrayList(input), Lists.newArrayList(tested.apply(input)));
    }

    @Test
    public void shouldCreateFirstBatchContainingOneTest()
    {
        MockBatcherTransform tested = new MockBatcherTransform(1, 7);
        Iterable<Description> oneTest = suitesOf(TestOne.class);
        assertEquals(suitesOf(TestOne.class), createBatch(tested, oneTest));
    }

    @Test
    public void shouldCreateSecondBatchContainingOneTest()
    {
        MockBatcherTransform tested = new MockBatcherTransform(2, 7);
        Iterable<Description> twoTestSuites = suitesOf(TestOne.class, TestTwo.class);
        assertEquals(suitesOf(TestTwo.class), createBatch(tested, twoTestSuites));
    }

    @Test
    public void shouldCreateThirdBatchContainingOneTest()
    {
        MockBatcherTransform tested = new MockBatcherTransform(3, 7);
        Iterable<Description> twoTestSuites = suitesOf(TestOne.class, TestTwo.class);
        assertEquals(suitesOf(TestTwo.class), createBatch(tested, twoTestSuites));
    }

    @Test
    public void shouldCreateBatchContainingZeroTestsGivenAllTestsAddedToPreviousBatches()
    {
        MockBatcherTransform tested = new MockBatcherTransform(4, 7);
        // 3 tests altogether
        Iterable<Description> oneTest = suitesOf(TestOne.class, TestTwo.class);
        assertEquals(0, Iterables.size(tested.apply(oneTest)));
    }

    @Test
    public void shouldSplitTestsAmongBatches()
    {
        MockBatcherTransform tested = new MockBatcherTransform(1, 5);
        // 15 tests - should end up as 3 per batch
        Iterable<Description> full = fullSuite();
        assertEquals(suitesOf(TestOne.class, TestTwo.class), createBatch(tested, full));
        tested = new MockBatcherTransform(2, 5);
        assertEquals(suitesOf(TestThree.class), createBatch(tested, full));
        tested = new MockBatcherTransform(3, 5);
        assertEquals(suitesOf(TestFour.class), createBatch(tested, full));
        tested = new MockBatcherTransform(4, 5);
        assertEquals(suitesOf(TestFour.class, TestFive.class), createBatch(tested, full));
        tested = new MockBatcherTransform(5, 5);
        assertEquals(suitesOf(TestFive.class), createBatch(tested, full));
    }

    @Test
    public void shouldPutNonSplittableClassesInOneBatch()
    {
        MockBatcherTransform tested = forNotSplittableClasses(1, 5, TestTwo.class, TestFour.class);
        // 15 tests - should end up as 3 per batch, with 3rd batch containing all tests in non-splittable TestFour
        Iterable<Description> full = fullSuite();
        assertEquals(suitesOf(TestOne.class, TestTwo.class), createBatch(tested, full));
        tested = forNotSplittableClasses(2, 5, TestTwo.class, TestFour.class);
        assertEquals(suitesOf(TestThree.class), createBatch(tested, full));
        tested = forNotSplittableClasses(3, 5, TestTwo.class, TestFour.class);
        assertEquals(suitesOf(TestFour.class), createBatch(tested, full));
        // only Test5 left, Test4 is all in batch 3
        tested = forNotSplittableClasses(4, 5, TestTwo.class, TestFour.class);
        assertEquals(suitesOf(TestFive.class), createBatch(tested, full));
        tested = forNotSplittableClasses(5, 5, TestTwo.class, TestFour.class);
        assertEquals(suitesOf(TestFive.class), createBatch(tested, full));
    }

    @Test
    public void shouldNonAccountForIgnoredTests()
    {
        MockBatcherTransform tested = new MockBatcherTransform(1, 5);
        // 17 tests (not counting ignored) - should end up as 3-4 per batch
        Iterable<Description> full = fullSuiteWithIgnores();
        assertEquals(suitesOf(TestOne.class, TestTwo.class, TestThree.class), createBatch(tested, full));
        tested = new MockBatcherTransform(2, 5);
        assertEquals(suitesOf(TestThree.class, TestFour.class), createBatch(tested, full));
        tested = new MockBatcherTransform(3, 5);
        assertEquals(suitesOf(TestFour.class, TestFive.class), createBatch(tested, full));
        tested = new MockBatcherTransform(4, 5);
        assertEquals(suitesOf(TestFive.class), createBatch(tested, full));
        tested = new MockBatcherTransform(5, 5);
        assertEquals(suitesOf(TestFive.class, PartiallyIgnoredTest.class), createBatch(tested, full));
    }

    @Test
    public void shouldNonAccountForIgnoredTestsAndPutNonSplittableTestsInOneBatch()
    {
        MockBatcherTransform tested = forNotSplittableClasses(1, 5, TestTwo.class, TestFour.class, PartiallyIgnoredTest.class);
        // 17 tests (not counting ignored)
        Iterable<Description> full = fullSuiteWithIgnores();
        assertEquals(suitesOf(TestOne.class, TestTwo.class, TestThree.class), createBatch(tested, full));
        tested = forNotSplittableClasses(2, 5, TestTwo.class, TestFour.class, PartiallyIgnoredTest.class);
        // this guy will get all tests in TestFour, 6 tests in it altogether
        assertEquals(suitesOf(TestThree.class, TestFour.class), createBatch(tested, full));
        tested = forNotSplittableClasses(3, 5, TestTwo.class, TestFour.class, PartiallyIgnoredTest.class);
        assertEquals(suitesOf(TestFive.class), createBatch(tested, full));
        tested = forNotSplittableClasses(4, 5, TestTwo.class, TestFour.class, PartiallyIgnoredTest.class);
        assertEquals(suitesOf(TestFive.class), createBatch(tested, full));
        tested = forNotSplittableClasses(5, 5, TestTwo.class, TestFour.class, PartiallyIgnoredTest.class);
        assertEquals(suitesOf(PartiallyIgnoredTest.class), createBatch(tested, full));
    }

    private Iterable<Description> fullSuite()
    {
        return suitesOf(TestOne.class, TestTwo.class, TestThree.class, TestFour.class, TestFive.class);
    }

    private Iterable<Description> fullSuiteWithIgnores()
    {
        return suitesOf(TestOne.class, TestTwo.class, TestThree.class, TestFour.class, TestFive.class,
                PartiallyIgnoredTest.class, FullyIgnoredTest.class);
    }

    private Iterable<Description> suitesOf(Class<?>... classes)
    {
        return Lists.newArrayList(Iterables.transform(Arrays.asList(classes), new Function<Class<?>, Description>()
        {
            @Override
            public Description apply(@Nullable Class<?> input)
            {
                try
                {
                    // small trick to extract description we need! ;)
                    return new JUnit4(input).getDescription();
                }
                catch (InitializationError initializationError)
                {
                    throw new RuntimeException(initializationError);
                }
            }
        }));
    }

    private Iterable<Description> createBatch(MockBatcherTransform tested, Iterable<Description> input)
    {
        // for comparison
        return Lists.newArrayList(tested.apply(input));
    }

}
