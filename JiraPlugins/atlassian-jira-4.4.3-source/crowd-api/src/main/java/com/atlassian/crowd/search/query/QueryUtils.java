package com.atlassian.crowd.search.query;

import org.apache.commons.lang.Validate;

import java.util.Collections;
import java.util.Arrays;

import com.google.common.collect.Collections2;

public class QueryUtils
{
    /**
     * Check whether {@code givenType} is assignable from any of the {@code types}
     * @param givenType the type to check
     * @param types the possible types {@code givenType} should be assignable from.
     * @param <U> the type of {@code givenType}
     * @return {@code givenType} if it is assignable from any of the types.
     * @throws IllegalArgumentException if {@code givenType} is not assignable to any of the types.
     */
    public static <U> Class<U> checkAssignableFrom(Class<U> givenType, Class<?>... types)
    {
        Validate.notNull(givenType);
        Validate.notNull(types);
        for (Class<?> type : types)
        {
            if (type != null && type.isAssignableFrom(givenType))
            {
                return givenType;
            }
        }
        throw new IllegalArgumentException("Given type (" + givenType.getName() + ") must be assignable from one of " + Arrays.toString(types));
    }
}
