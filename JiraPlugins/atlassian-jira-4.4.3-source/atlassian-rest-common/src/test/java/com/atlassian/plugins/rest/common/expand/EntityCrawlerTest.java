package com.atlassian.plugins.rest.common.expand;

import com.atlassian.plugins.rest.common.expand.parameter.DefaultExpandParameter;
import com.atlassian.plugins.rest.common.expand.resolver.EntityExpanderResolver;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Field;
import java.util.Set;

/**
 * Testing {@link EntityCrawler}
 */
public class EntityCrawlerTest
{
    private EntityCrawler entityCrawler;

    @Before
    public void setUp()
    {
        entityCrawler = new EntityCrawler();
    }

    @Test
    public void testGetExpandableWithNullField()
    {
        assertNull(entityCrawler.getExpandable(null));
    }

    @Test
    public void testGetExpandableWithNoExpandableAnnotation() throws Exception
    {
        final Field field = SomeClass.class.getField("field1");
        assertNull(entityCrawler.getExpandable(field));
    }

    @Test
    public void testGetExpandableWithValuedExpandable() throws Exception
    {
        final Field field = SomeClass.class.getField("field2");
        assertEquals("field2Value", entityCrawler.getExpandable(field).value());
    }

    @Test
    public void testGetExpandableWithNamedXmlElement() throws Exception
    {
        final Field field = SomeClass.class.getField("field3");
        assertEquals("field3Value", entityCrawler.getExpandable(field).value());
    }

    @Test
    public void testGetExpandableWithUnNamedXmlElement() throws Exception
    {
        final Field field = SomeClass.class.getField("field4");
        assertEquals("field4", entityCrawler.getExpandable(field).value());
    }

    @Test
    public void testGetExpandableWithNoXmlElement() throws Exception
    {
        final Field field = SomeClass.class.getField("field5");
        assertEquals("field5", entityCrawler.getExpandable(field).value());
    }

    @Test
    public void testCrawlGetsFieldsFromSuperClass() throws Exception
    {
        final Set<String> expectedFields = Sets.newHashSet("field2Value", "field3Value", "field4", "field5");

        entityCrawler.crawl(new SomeDerivedClass(), new DefaultExpandParameter(Lists.newArrayList("*")),
                new EntityExpanderResolver()
                {
                    public boolean hasExpander(Class<?> type)
                    {
                        return true;
                    }

                    public <T> EntityExpander<T> getExpander(Class<? extends T> type)
                    {
                        return new EntityExpander<T>()
                        {
                            public T expand(ExpandContext<T> tExpandContext, EntityExpanderResolver expanderResolver, EntityCrawler entityCrawler)
                            {
                                assertTrue(expectedFields.remove(tExpandContext.getExpandable().value()));
                                return null;
                            }
                        };
                    }
                });

        assertTrue(expectedFields.isEmpty());
    }

    private static class SomeClass
    {
        public Object field1 = new Object();

        @Expandable ("field2Value")
        public Object field2 = new Object();

        @Expandable
        @XmlElement (name = "field3Value")
        public Object field3 = new Object();

        @Expandable
        @XmlElement
        public Object field4 = new Object();

        @Expandable
        public Object field5 = new Object();

        @Expandable
        public Object ignoredField;
    }

    private static class SomeDerivedClass extends SomeClass
    {
        @XmlAttribute
        private String expand;
    }
}
