package com.atlassian.upm.test.osgi;

import java.lang.reflect.Array;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;

public final class Mockery
{
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayOf(Class<T> clazz, int length)
    {
        T[] out = (T[]) Array.newInstance(clazz, length);
        for (int i = 0; i < length; ++i)
        {
            out[i] = mock(clazz);
        }
        return out;
    }

    public static <T> List<T> listOf(Class<T> clazz, int length)
    {
        return asList(arrayOf(clazz, length));
    }
}
