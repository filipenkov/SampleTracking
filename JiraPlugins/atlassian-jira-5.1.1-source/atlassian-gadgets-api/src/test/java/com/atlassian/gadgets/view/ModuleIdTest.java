package com.atlassian.gadgets.view;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ModuleIdTest
{
    @Test(expected = IllegalArgumentException.class)
    public void createFromNullStringThrowsIllegalArgumentException()
    {
        ModuleId.valueOf(null);
    }

    @Test(expected = NumberFormatException.class)
    public void createFromNonNumericStringThrowsNumberFormatException()
    {
        ModuleId.valueOf("non-numeric");
    }

    @Test
    public void createFromNumericStringParsesValue()
    {
        assertThat(ModuleId.valueOf("42").value(), is(equalTo(42L)));
    }
}
