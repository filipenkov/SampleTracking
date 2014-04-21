package com.atlassian.crowd.model.application;

import com.atlassian.crowd.model.InternalEntityTemplate;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class ApplicationImplTest
{
    private static final String LONG_STRING = StringUtils.repeat("X", 300);

    @Test(expected=IllegalArgumentException.class)
    public void testApplicationImpl()
    {
        new ApplicationImpl(new InternalEntityTemplate(0L, LONG_STRING, true, null, null));
    }
}
