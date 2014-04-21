package com.atlassian.jira.issue.fields.screen;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test for {@link com.atlassian.jira.issue.fields.screen.AbstractGVBean}.
 *
 * @since v4.1
 */
public class TestAbstractGVBean extends ListeningTestCase
{
    @Test
    public void testSetGenericValue()
    {
        final AtomicBoolean ran = new AtomicBoolean(false);
        AbstractGVBean testBean = new AbstractGVBean()
        {
            @Override
            protected void init()
            {
                ran.set(true);
            }
        };

        final MockGenericValue value = new MockGenericValue("doesntMatter");
        testBean.setGenericValue(value);
        assertTrue(ran.get());
        assertSame(value, testBean.getGenericValue());
        assertFalse(testBean.isModified());
    }

    @Test
    public void testUpdateGVNoGV()
    {
        final TestBean bean = new TestBean();
        assertFalse(bean.isModified());
        bean.updateGV("bb", "b");
        assertTrue(bean.isModified());
    }

    @Test
    public void testUpdateGVWithGv()
    {
        final MockGenericValue genericValue = new MockGenericValue("test");
        genericValue.set("b", "b");

        final TestBean bean = new TestBean();
        bean.setGenericValue(genericValue);
        assertFalse(bean.isModified());
        bean.updateGV("b", "b");
        assertFalse(bean.isModified());
        bean.updateGV("b", 1);
        assertTrue(bean.isModified());
        bean.setModified(false);
        bean.updateGV("c", "c");
        assertTrue(bean.isModified());
    }

    @Test
    public void testVlauesEqals() throws Exception
    {
        final TestBean bean = new TestBean();
        assertTrue(bean.valuesEqual(null, null));
        assertTrue(bean.valuesEqual(9L, 9L));
        assertFalse(bean.valuesEqual(9L, null));
        assertFalse(bean.valuesEqual(null, 9L));
    }

    private static class TestBean extends AbstractGVBean
    {
        @Override
        protected void init()
        {
        }
    }
}
