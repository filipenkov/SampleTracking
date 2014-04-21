package com.atlassian.upm;

import com.atlassian.upm.api.util.Option;

import org.apache.commons.lang.StringUtils;

import static com.atlassian.upm.api.util.Option.none;
import static com.atlassian.upm.api.util.Option.some;

public class Strings
{
    /**
     * Returns the first non-empty {@link String} in the {@link Iterable}, or {@code none()} if none exist.
     */
    public static Option<String> getFirstNonEmpty(Iterable<String> vals)
    {
        for (String val : vals)
        {
            if (!StringUtils.isEmpty(val))
            {
                return some(val);
            }
        }
        return none(String.class);
    }
}
