/*
 * Copyright (c) 2003 by Atlassian Software Systems Pty. Ltd.
 * All rights reserved.
 */
package com.atlassian.spring.container;

import junit.framework.TestCase;

/**
 * @author Ross Mason
 *
 * Tests implementations of ContainerContext
 */
public abstract class AbstractContainerContextTest extends TestCase
{
    public void testContainer() throws Exception
    {
        ContainerContext container = getContainer();

        try
        {
            container.getComponent(null);
            fail("Should get an exception with a null key");
        }
        catch (ComponentNotFoundException e)
        {
            // expected
        }

        try
        {
            container.getComponent(getInvalidKey());
            fail("Should get an exception with a invalid key");
        }
        catch (ComponentNotFoundException e1)
        {
            // expected
        }

        assertNotNull(container.getComponent(getValidKey()));

        Object dupKey = getDuplicateKey();
        //Only do this test if the container supports Duplicate keys
        if(dupKey!=null)
        {
            try
            {
                container.getComponent(dupKey);
                fail("Should get an exception with a duplicate key");
            }
            catch (ComponentNotFoundException e)
            {
                // expected
            }
        }

        //Just call to make sure it doesn't fail.  How do we test if further??
        container.refresh();
    }

    public abstract ContainerContext getContainer() throws Exception;

    public abstract Object getValidKey();

    public Object getInvalidKey()
    {
        return "12345676890Invalid";
    }

    public abstract Object getDuplicateKey();

}
