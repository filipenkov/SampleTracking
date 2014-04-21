package com.atlassian.plugins.rest.common.expand;

import static org.junit.Assert.*;
import com.atlassian.plugins.rest.common.expand.parameter.DefaultExpandParameter;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.annotation.XmlAttribute;

public class SelfExpandingTest
{
    private EntityCrawler entityCrawler;

    @Before
    public void setUp()
    {
        entityCrawler = new EntityCrawler();
    }

    @Test
    public void testSelfExpanding()
    {
        final SomeClass someClass = new SomeClass();
        assertFalse(someClass.expandable.isExpanded());
        entityCrawler.crawl(
            someClass,
            new DefaultExpandParameter(Lists.newArrayList("*")),
            new SelfExpandingExpander.Resolver());
        assertTrue(someClass.expandable.isExpanded());
    }

    public static class SomeClass
    {
        @Expandable
        public final SomeExpandableEntity expandable = new SomeExpandableEntity();
    }

    public static class SomeExpandableEntity implements SelfExpanding
    {
        @XmlAttribute
        private String expand;
        private boolean expanded = false;

        public void expand()
        {
            expanded = true;
        }

        public boolean isExpanded()
        {
            return expanded;
        }
    }
}
