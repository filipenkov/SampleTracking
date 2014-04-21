package com.atlassian.spring.hosted;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertEquals;

/**
 */
public class TestHostedBeanFactoryPostProcessor
{
    @Test
    public void testOverrides()
    {
        // The details are in the xml files
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("hosted-bean-factory-post-processor-test.xml");
        OverridableBean overridableBean = (OverridableBean) context.getBean("overridableBean");
        assertEquals("overridden", overridableBean.getValue());
    }

    @Test
    public void testOverridesAttributeTrue()
    {
        // The details are in the xml files
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("hosted-bean-factory-post-processor-attribute-test.xml");
        NonOverridableBean overridableBean = (NonOverridableBean) context.getBean("nonOverridableBean");
        assertEquals("overridden", overridableBean.getValue());
    }

    @Test(expected = HostedOverrideNotAllowedException.class)
    public void testOverridesAttributeFalse()
    {
        // The details are in the xml files
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("hosted-bean-factory-post-processor-attribute-test-false.xml");
        OverridableBean overridableBean = (OverridableBean) context.getBean("nonOverridableBean");
        assertEquals("overridden", overridableBean.getValue());
    }

    @Test
    public void testNotOverriddenBean()
    {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("hosted-bean-factory-post-processor-test.xml");
        OverridableBean overridableBean = (OverridableBean) context.getBean("notOverriddenBean");
        assertEquals("default", overridableBean.getValue());
    }

    @Test(expected = HostedOverrideNotAllowedException.class)
    public void testNonOverridableBean()
    {
        // The details are in the xml files
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("hosted-bean-factory-post-processor-fail-test.xml");
        NonOverridableBean nonOverridableBean = (NonOverridableBean) context.getBean("nonOverridableBean");
    }
}
