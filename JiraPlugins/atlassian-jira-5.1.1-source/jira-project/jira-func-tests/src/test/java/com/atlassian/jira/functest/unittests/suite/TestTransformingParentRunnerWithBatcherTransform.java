package com.atlassian.jira.functest.unittests.suite;

import com.atlassian.jira.functest.framework.suite.SuiteTransform;
import com.atlassian.jira.functest.framework.suite.TransformingParentRunner;
import com.atlassian.jira.functest.unittests.suite.MockJUnit4TestClasses.FullyIgnoredTest;
import com.atlassian.jira.functest.unittests.suite.MockJUnit4TestClasses.PartiallyIgnoredTest;
import com.atlassian.jira.functest.unittests.suite.MockJUnit4TestClasses.PartiallyRunFirstTest;
import com.atlassian.jira.functest.unittests.suite.MockJUnit4TestClasses.RunFirstTest;
import com.atlassian.jira.functest.unittests.suite.MockJUnit4TestClasses.TestFive;
import com.atlassian.jira.functest.unittests.suite.MockJUnit4TestClasses.TestFour;
import com.atlassian.jira.functest.unittests.suite.MockJUnit4TestClasses.TestOne;
import com.atlassian.jira.functest.unittests.suite.MockJUnit4TestClasses.TestThree;
import com.atlassian.jira.functest.unittests.suite.MockJUnit4TestClasses.TestTwo;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.hamcrest.StringDescription;
import org.junit.Test;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import java.util.List;

import static com.atlassian.jira.functest.unittests.suite.MockBatcherTransform.forNotSplittableClasses;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link com.atlassian.jira.functest.framework.suite.BatcherTransform}.
 *
 * @since 4.4
 */
public class TestTransformingParentRunnerWithBatcherTransform
{
    @Test
    public void shouldHaveCustomName() throws Exception
    {
        TransformingParentRunner<Runner> tested = new TransformingParentRunner<Runner>("custom-name",
                newSuite(TestOne.class), newTransform(1, 1));
        assertEquals("custom-name", tested.getDescription().getDisplayName());
    }

    @Test
    public void shouldNotApplyBatchingGivenNumberOfBatchesLessThanOne() throws Exception
    {
        TransformingParentRunner<Runner> tested = newTested(newSuite(TestOne.class, TestTwo.class), 1, 0);
        assertSingleTests(tested,
                singleTest(TestOne.class, "testOneOne"),
                singleTest(TestTwo.class, "testTwoOne"),
                singleTest(TestTwo.class, "testTwoTwo"));
    }

    @Test
    public void shouldProcessEmptyTestCollectionInNonBatchingMode() throws Exception
    {
        TransformingParentRunner<Runner> tested = newTested(newSuite(), 1, 0);
        assertTrue(tested.getDescription().isEmpty());
    }

    @Test
    public void shouldProcessEmptyTestCollectionInBatchingMode() throws Exception
    {
        TransformingParentRunner<Runner> tested = newTested(newSuite(), 1, 5);
        assertTrue(tested.getDescription().isEmpty());
    }

    @Test
    public void shouldCreateOneBatchWithAllTests() throws InitializationError
    {
        TransformingParentRunner<Runner> tested = newTested(newSuite(TestOne.class, TestTwo.class), 1, 1);
        assertSingleTests(tested,
                singleTest(TestOne.class, "testOneOne"),
                singleTest(TestTwo.class, "testTwoOne"),
                singleTest(TestTwo.class, "testTwoTwo"));
    }

    @Test
    public void shouldCreateFirstBatchContainingOneTest() throws InitializationError
    {
        TransformingParentRunner<Runner> tested = newTested(newSuite(TestOne.class), 1, 7);
        assertSingleTests(tested, singleTest(TestOne.class, "testOneOne"));
    }

    @Test
    public void shouldCreateSecondBatchContainingOneTest() throws InitializationError
    {
        TransformingParentRunner<Runner> tested = newTested(newSuite(TestOne.class, TestTwo.class), 2, 7);
        assertSingleTests(tested, singleTest(TestTwo.class, "testTwoOne"));
    }

    @Test
    public void shouldCreateThirdBatchContainingOneTest() throws InitializationError
    {
        TransformingParentRunner<Runner> tested = newTested(newSuite(TestOne.class, TestTwo.class), 3, 7);
        assertSingleTests(tested, singleTest(TestTwo.class, "testTwoTwo"));
    }

    @Test
    public void shouldCreateBatchContainingZeroTestsGivenAllTestsAddedToPreviousBatches() throws InitializationError
    {
        TransformingParentRunner<Runner> tested = newTested(newSuite(TestOne.class, TestTwo.class), 4, 7);
        assertTrue(tested.getDescription().isEmpty());
    }

    @Test
    public void shouldSplitTestsAmongBatches() throws InitializationError
    {
        // 15 tests - should end up as 3 per batch
        TransformingParentRunner<Runner> tested = newTested(fullSuite(), 1, 5);
        assertSingleTests(tested,
                singleTest(TestOne.class, "testOneOne"),
                singleTest(TestTwo.class, "testTwoOne"),
                singleTest(TestTwo.class, "testTwoTwo"));
        tested = newTested(fullSuite(), 2, 5);
        assertSingleTests(tested,
                singleTest(TestThree.class, "testThreeOne"),
                singleTest(TestThree.class, "testThreeTwo"),
                singleTest(TestThree.class, "testThreeThree"));
        tested = newTested(fullSuite(), 3, 5);
        assertSingleTests(tested,
                singleTest(TestFour.class, "testFourOne"),
                singleTest(TestFour.class, "testFourTwo"),
                singleTest(TestFour.class, "testFourThree"));
        tested = newTested(fullSuite(), 4, 5);
        assertSingleTests(tested,
                singleTest(TestFour.class, "testFourFour"),
                singleTest(TestFive.class, "testFiveOne"),
                singleTest(TestFive.class, "testFiveTwo"));
        tested = newTested(fullSuite(), 5, 5);
        assertSingleTests(tested,
                singleTest(TestFive.class, "testFiveThree"),
                singleTest(TestFive.class, "testFiveFour"),
                singleTest(TestFive.class, "testFiveFive"));
    }

    @Test
    public void shouldPutNonSplittableClassesInOneBatch() throws InitializationError
    {
        TransformingParentRunner<Runner> tested = newTested(fullSuite(), 1, 5, TestTwo.class, TestFour.class);
        // 15 tests - should end up as 3 per batch, with 3rd batch containing all tests in non-splittable TestFour
        assertSingleTests(tested,
                singleTest(TestOne.class, "testOneOne"),
                singleTest(TestTwo.class, "testTwoOne"),
                singleTest(TestTwo.class, "testTwoTwo"));
        tested = newTested(fullSuite(), 2, 5, TestTwo.class, TestFour.class);
        assertSingleTests(tested,
                singleTest(TestThree.class, "testThreeOne"),
                singleTest(TestThree.class, "testThreeTwo"),
                singleTest(TestThree.class, "testThreeThree"));
        tested = newTested(fullSuite(), 3, 5, TestTwo.class, TestFour.class);
        assertSingleTests(tested,
                singleTest(TestFour.class, "testFourOne"),
                singleTest(TestFour.class, "testFourTwo"),
                singleTest(TestFour.class, "testFourThree"),
                singleTest(TestFour.class, "testFourFour"));
        tested = newTested(fullSuite(), 4, 5, TestTwo.class, TestFour.class);
        // only Test5 left, Test4 is all in batch 3
        assertSingleTests(tested,
                singleTest(TestFive.class, "testFiveOne"),
                singleTest(TestFive.class, "testFiveTwo"),
                singleTest(TestFive.class, "testFiveThree"));
        tested = newTested(fullSuite(), 5, 5, TestTwo.class, TestFour.class);
        assertSingleTests(tested,
                singleTest(TestFive.class, "testFiveFour"),
                singleTest(TestFive.class, "testFiveFive"));
    }

    @Test
    public void shouldNonAccountForIgnoredTests() throws InitializationError
    {
        TransformingParentRunner<Runner> tested = newTested(fullSuiteWithIgnores(), 1, 5);
        // 17 tests (not counting ignored) - should end up as 3-4 per batch
        assertSingleTests(tested,
                singleTest(TestOne.class, "testOneOne"),
                singleTest(TestTwo.class, "testTwoOne"),
                singleTest(TestTwo.class, "testTwoTwo"),
                singleTest(TestThree.class, "testThreeOne"));
        tested = newTested(fullSuiteWithIgnores(), 2, 5);
        assertSingleTests(tested,
                singleTest(TestThree.class, "testThreeTwo"),
                singleTest(TestThree.class, "testThreeThree"),
                singleTest(TestFour.class, "testFourOne"),
                singleTest(TestFour.class, "testFourTwo"));
        tested = newTested(fullSuiteWithIgnores(), 3, 5);
        assertSingleTests(tested,
                singleTest(TestFour.class, "testFourThree"),
                singleTest(TestFour.class, "testFourFour"),
                singleTest(TestFive.class, "testFiveOne"));
        tested = newTested(fullSuiteWithIgnores(), 4, 5);
        assertSingleTests(tested,
                singleTest(TestFive.class, "testFiveTwo"),
                singleTest(TestFive.class, "testFiveThree"),
                singleTest(TestFive.class, "testFiveFour"));
        tested = newTested(fullSuiteWithIgnores(), 5, 5);
        assertSingleTests(tested,
                singleTest(TestFive.class, "testFiveFive"),
                singleTest(PartiallyIgnoredTest.class, "notIgnoredTest1"),
                singleTest(PartiallyIgnoredTest.class, "notIgnoredTest2"));
    }

    @Test
    public void shouldNonAccountForIgnoredTestsAndPutNonSplittableTestsInOneBatch() throws InitializationError
    {
        TransformingParentRunner<Runner> tested = newTested(fullSuiteWithIgnores(), 1, 5, TestTwo.class, TestFour.class, PartiallyIgnoredTest.class);
        // 17 tests (not counting ignored)
        assertSingleTests(tested,
                singleTest(TestOne.class, "testOneOne"),
                singleTest(TestTwo.class, "testTwoOne"),
                singleTest(TestTwo.class, "testTwoTwo"),
                singleTest(TestThree.class, "testThreeOne"));
        tested = newTested(fullSuiteWithIgnores(), 2, 5, TestTwo.class, TestFour.class, PartiallyIgnoredTest.class);
        // this guy will get all tests in TestFour, 6 tests in it altogether
        assertSingleTests(tested,
                singleTest(TestThree.class, "testThreeTwo"),
                singleTest(TestThree.class, "testThreeThree"),
                singleTest(TestFour.class, "testFourOne"),
                singleTest(TestFour.class, "testFourTwo"),
                singleTest(TestFour.class, "testFourThree"),
                singleTest(TestFour.class, "testFourFour"));
        tested = newTested(fullSuiteWithIgnores(), 3, 5, TestTwo.class, TestFour.class, PartiallyIgnoredTest.class);
        assertSingleTests(tested,
                singleTest(TestFive.class, "testFiveOne"),
                singleTest(TestFive.class, "testFiveTwo"),
                singleTest(TestFive.class, "testFiveThree"));
        tested = newTested(fullSuiteWithIgnores(), 4, 5, TestTwo.class, TestFour.class, PartiallyIgnoredTest.class);
        assertSingleTests(tested,
                singleTest(TestFive.class, "testFiveFour"),
                singleTest(TestFive.class, "testFiveFive"));
        tested = newTested(fullSuiteWithIgnores(), 5, 5, TestTwo.class, TestFour.class, PartiallyIgnoredTest.class);
        assertSingleTests(tested,
                singleTest(PartiallyIgnoredTest.class, "notIgnoredTest1"),
                singleTest(PartiallyIgnoredTest.class, "notIgnoredTest2"));
    }

    @Test
    public void shouldPutRunFirstTestsAtTheBeginningOfBatches() throws InitializationError
    {
        TransformingParentRunner<Runner> tested = newTested(fullSuiteWithRunFirst(), 1, 5, TestTwo.class, TestFour.class);
        // 22 tests, 5 run first
        assertSingleTests(tested,
                singleTest(RunFirstTest.class, "testOne"),
                singleTest(TestOne.class, "testOneOne"),
                singleTest(TestTwo.class, "testTwoOne"),
                singleTest(TestTwo.class, "testTwoTwo"),
                singleTest(TestThree.class, "testThreeOne"));
        tested = newTested(fullSuiteWithRunFirst(), 2, 5, TestTwo.class, TestFour.class);
        // this guy will get all tests in TestFour, 7 tests in it altogether
        assertSingleTests(tested,
                singleTest(RunFirstTest.class, "testTwo"),
                singleTest(TestThree.class, "testThreeTwo"),
                singleTest(TestThree.class, "testThreeThree"),
                singleTest(TestFour.class, "testFourOne"),
                singleTest(TestFour.class, "testFourTwo"),
                singleTest(TestFour.class, "testFourThree"),
                singleTest(TestFour.class, "testFourFour"));
        tested = newTested(fullSuiteWithRunFirst(), 3, 5, TestTwo.class, TestFour.class);
        assertSingleTests(tested,
                singleTest(RunFirstTest.class, "testThree"),
                singleTest(TestFive.class, "testFiveOne"),
                singleTest(TestFive.class, "testFiveTwo"),
                singleTest(TestFive.class, "testFiveThree"));
        tested = newTested(fullSuiteWithRunFirst(), 4, 5, TestTwo.class, TestFour.class);
        assertSingleTests(tested,
                singleTest(PartiallyRunFirstTest.class, "runFirstTest1"),
                singleTest(TestFive.class, "testFiveFour"),
                singleTest(TestFive.class, "testFiveFive"));
        tested = newTested(fullSuiteWithRunFirst(), 5, 5, TestTwo.class, TestFour.class);
        assertSingleTests(tested,
                singleTest(PartiallyRunFirstTest.class, "runFirstTest2"),
                singleTest(PartiallyRunFirstTest.class, "notRunFirstTest1"),
                singleTest(PartiallyRunFirstTest.class, "notRunFirstTest2"));
    }

    @Test
    public void runFirstTestMustBeSplittable()
    {
        // TODO
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotAllowMoreRunFirstTestsThanBatches() throws InitializationError
    {
        // 5 @RunFirst tests - not enough batches
        newTested(fullSuiteWithRunFirst(), 1, 4);
    }

    private Suite fullSuite() throws InitializationError
    {
        return newSuite(TestOne.class, TestTwo.class, TestThree.class, TestFour.class, TestFive.class);
    }

    private Suite fullSuiteWithIgnores() throws InitializationError
    {
        return newSuite(TestOne.class, TestTwo.class, TestThree.class, TestFour.class, TestFive.class,
                PartiallyIgnoredTest.class, FullyIgnoredTest.class);
    }

    private Suite fullSuiteWithRunFirst() throws InitializationError
    {
        return newSuite(TestOne.class, TestTwo.class, TestThree.class, TestFour.class, TestFive.class,
                RunFirstTest.class, PartiallyRunFirstTest.class);
    }

    private Suite newSuite(Class<?>... classes) throws InitializationError
    {
        return new Suite(new AllDefaultPossibilitiesBuilder(false), classes);
    }

    private TransformingParentRunner<Runner> newTested(Suite suite, int batchNumber, int numberOfBatches,
            Class<?>... nonSplittable)
            throws InitializationError
    {
        return new TransformingParentRunner<Runner>(suite, newTransform(batchNumber, numberOfBatches, nonSplittable));
    }

    private Iterable<SuiteTransform> newTransform(int batchNumber, int numberOfBatches,
            Class<?>... nonSplittable)
    {
        return ImmutableList.of((SuiteTransform) forNotSplittableClasses(batchNumber, numberOfBatches, nonSplittable));
    }

    private List<Description> collectSingleTests(Runner runner)
    {
        return singleTestsOf(runner.getDescription());
    }

    private List<Description> singleTestsOf(Description description)
    {
        if (description.isTest())
        {
            return ImmutableList.of(description);
        }
        else
        {
            List<Description> answer = Lists.newArrayList();
            for (Description child : description.getChildren())
            {
                answer.addAll(singleTestsOf(child));
            }
            return answer;
        }

    }

    private void assertSingleTests(Runner runner, SingleTest... expected)
    {
        List<Description> actual = collectSingleTests(runner);
        assertEquals(expected.length, actual.size());
        for (int i=0; i<expected.length; i++)
        {
            SingleTest expectedSingle = expected[i];
            Description actualSingle = actual.get(i);
            assertTrue(new StringDescription().appendText("Expected").appendValue(expectedSingle)
                    .appendText(" does not match actual ").appendValue(actualSingle).toString(),
                    expectedSingle.matches(actualSingle));
        }
    }

    private SingleTest singleTest(Class<?> testClass, String method)
    {
        return new SingleTest(testClass, method);
    }

    private static class SingleTest
    {
        final Class<?> testClass;
        final String method;

        public SingleTest(Class<?> testClass, String method)
        {
            this.testClass = testClass;
            this.method = method;
        }

        public boolean matches(Description description)
        {
            return description.isTest()
                    && testClass.equals(description.getTestClass())
                    && method.equals(description.getMethodName());
        }

        @Override
        public String toString()
        {
            return new StringDescription().appendText("SingleTest[testClass=").appendValue(testClass)
                    .appendText(",methodName=").appendValue(method).appendText("]").toString();
        }
    }

}
