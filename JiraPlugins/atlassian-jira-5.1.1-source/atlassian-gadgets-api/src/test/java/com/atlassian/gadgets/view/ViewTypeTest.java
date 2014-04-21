package com.atlassian.gadgets.view;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ViewTypeTest
{
    ViewType viewOne;

    @Before
    public void setUp()
    {
        viewOne = ViewType.createViewType("viewOne", "viewOneAliasOne", "viewOneAliasTwo");
    }

    @After
    public void tearDown()
    {
        ViewType.removeViewType(viewOne);
    }


    @Test(expected = IllegalArgumentException.class)
    public void assertThatViewTypeNamesMustBeUnique()
    {
        ViewType.createViewType("viewOne");
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertThatViewTypeAliasesMustBeUnique()
    {
        ViewType.createViewType("viewOneAliasOne");
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertThatViewTypesAreDeletedProperly()
    {
        ViewType viewTwo = ViewType.createViewType("viewTwo");
        assertTrue(ViewType.removeViewType(viewTwo));
        assertFalse(ViewType.removeViewType(viewTwo));
        ViewType.valueOf("viewTwo");
    }

    @Test
    public void assertThatViewTypeValueOfFindsCorrectViewType()
    {
        ViewType viewType = ViewType.valueOf("viewOne");
        assertEquals(viewOne, viewType);
    }

    @Test
    public void assertThatViewTypeValueOfFindsCorrectViewTypeByAlias()
    {
        ViewType viewType = ViewType.valueOf("viewOneAliasOne");
        assertEquals(viewOne, viewType);
    }

}
