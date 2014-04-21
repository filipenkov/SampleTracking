package com.atlassian.streams.api.common;

import java.util.NoSuchElementException;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Suppliers;

import org.junit.Test;

import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static com.google.common.base.Functions.compose;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OptionTest
{
    @Test
    public void assertThatFoldOnNoneReturnsValueFromSupplier()
    {
        assertThat(none().fold(Suppliers.ofInstance("a"), Functions.toStringFunction()), is(equalTo("a")));
    }

    @Test
    public void assertThatFoldOnSomeReturnsValueAfterFunctionIsApplied()
    {
        assertThat(some(1).fold(Suppliers.ofInstance(0), increment()), is(equalTo(2)));
    }

    @Test
    public void assertThatIsDefinedIsTrueForSome()
    {
        assertTrue(some("a").isDefined());
    }

    @Test
    public void assertThatIsDefinedIsFalseForNone()
    {
        assertFalse(none().isDefined());
    }

    @Test
    public void assertThatGetOnSomeReturnsValue()
    {
        assertThat(some(1).get(), is(equalTo(1)));
    }

    @Test(expected = NoSuchElementException.class)
    public void assertThatGetOnNoneThrowsException()
    {
        none().get();
    }

    @Test
    public void assertThatGetOrElseOnSomeReturnsValue()
    {
        assertThat(some(1).getOrElse(0), is(equalTo(1)));
    }

    @Test
    public void assertThatGetOrElseOnNoneReturnsElseValue()
    {
        assertThat(none(Integer.class).getOrElse(0), is(equalTo(0)));
    }

    @Test
    public void assertThatGetOrElseOnNoneReturnsValueFromSupplier()
    {
        assertThat(none(Integer.class).getOrElse(Suppliers.ofInstance(0)), is(equalTo(0)));
    }

    @Test
    public void assertThatIteratorOverSomeContainsOnlyValue()
    {
        assertThat(some(1), contains(1));
    }

    @Test
    public void assertThatNoneIsEmptyIterable()
    {
        assertThat(none(), is(emptyIterable()));
    }

    @Test
    public void assertThatMapAppliesFunctionToSomeValue()
    {
        assertThat(some(1).map(increment()), is(equalTo(some(2))));
    }

    @Test
    public void assertThatMapOverNoneDoesNothing()
    {
        assertThat(none(Integer.class).map(increment()), is(equalTo(none(Integer.class))));
    }

    @Test
    public void assertThatFlatMapAppliesFunctionToSomeValue()
    {
        assertThat(some(1).flatMap(liftedIncrement()), is(equalTo(some(2))));
    }

    @Test
    public void assertThatFlatMapOverNoneDoesNothing()
    {
        assertThat(none(Integer.class).flatMap(liftedIncrement()), is(equalTo(none(Integer.class))));
    }

    @Test
    public void assertThatEqualSomesAreEqual()
    {
        assertTrue(some(2).equals(some(2)));
    }

    @Test
    public void assertThatNonEqualSomesAreNotEqual()
    {
        assertFalse(some(1).equals(some(2)));
    }

    @Test
    public void assertThatHashCodesFromEqualSomesAreEqual()
    {
        assertTrue(some(1).hashCode() == some(1).hashCode());
    }

    private Function<Integer, Option<Integer>> liftedIncrement()
    {
        return compose(Option.<Integer>option(), increment());
    }

    private Function<Integer, Integer> increment()
    {
        return Increment.INSTANCE;
    }

    private enum Increment implements Function<Integer, Integer>
    {
        INSTANCE;

        public Integer apply(Integer i)
        {
            return i + 1;
        }
    }
}
