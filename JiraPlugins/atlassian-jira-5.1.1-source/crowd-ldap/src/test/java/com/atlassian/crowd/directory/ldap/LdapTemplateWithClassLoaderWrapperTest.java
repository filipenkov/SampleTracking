package com.atlassian.crowd.directory.ldap;

import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.crowd.directory.ldap.LdapTemplateWithClassLoaderWrapper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LdapTemplateWithClassLoaderWrapperTest
{
    @Test
    public void invokeWithContextClassLoaderUsesClassClassLoader()
    {
        ClassLoader ctxt = Thread.currentThread().getContextClassLoader();

        ClassLoader dummyClassLoader = new ClassLoader(ctxt)
        {
        };

        Thread.currentThread().setContextClassLoader(dummyClassLoader);

        final ClassLoader expectedClassLoaderDuringInvocation = LdapTemplateWithClassLoaderWrapper.class.getClassLoader();
        final AtomicBoolean wasRun = new AtomicBoolean(false);

        String s = LdapTemplateWithClassLoaderWrapper.invokeWithContextClassLoader(new LdapTemplateWithClassLoaderWrapper.CallableWithoutCheckedException<String>(){
            public String call()
            {
                assertEquals(expectedClassLoaderDuringInvocation,
                        Thread.currentThread().getContextClassLoader());

                wasRun.set(true);

                return "result";
            }
        });

        assertEquals("result", s);
        assertEquals(dummyClassLoader, Thread.currentThread().getContextClassLoader());
        assertTrue(wasRun.get());
    }
}
