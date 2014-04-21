package com.atlassian.labs.hipchat.test.mock;


import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class MoreAnswers {

    public static final FirstArgument<Object> FIRST_ARGUMENT = new FirstArgument<Object>();

    public static <T> Answer<T> firstArg() {
        return (Answer<T>) FIRST_ARGUMENT;
    }

    private static class FirstArgument<T> implements Answer<T> {

        @Override
        public T answer(InvocationOnMock invocation) throws Throwable {
            return (T) invocation.getArguments()[0];
        }
    }
}