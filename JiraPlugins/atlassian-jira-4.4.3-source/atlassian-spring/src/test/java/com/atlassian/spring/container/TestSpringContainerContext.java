/*
 * Copyright (c) 2003 by Atlassian Software Systems Pty. Ltd.
 * All rights reserved.
 */
package com.atlassian.spring.container;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Ross Mason
 *
 * The the SpringContainerContext implementation
 */
public class TestSpringContainerContext extends AbstractContainerContextTest
{

    /* (non-Javadoc)
     * @see com.atlassian.confluence.setup.AbstractContainerContextTest#getContainer()
     */
    public ContainerContext getContainer() throws Exception
    {
        ApplicationContext applicationContext =
           new ClassPathXmlApplicationContext("/spring-container-test.xml");
       SpringTestContainerContext container = new SpringTestContainerContext(applicationContext);
       return container;
    }

    /* (non-Javadoc)
     * @see com.atlassian.confluence.setup.AbstractContainerContextTest#getDuplicateKey()
     */
    public Object getDuplicateKey()
    {
        return String.class;
    }

    /* (non-Javadoc)
     * @see com.atlassian.confluence.setup.AbstractContainerContextTest#getValidKey()
     */
    public Object getValidKey()
    {
        return "myFoo";
    }

}
