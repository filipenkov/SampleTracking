package com.atlassian.crowd.directory.ldap;

import java.util.List;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextProcessor;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.NameClassPairCallbackHandler;

/**
 * Wrap an {@link LdapTemplate} and perform all operations with the context
 * ClassLoader set to this class's ClassLoader.
 * <code>com.sun.naming.internal.NamingManager</code> uses the context
 * ClassLoader so, without this wrapper, calls that originate from plugins and
 * end up using LDAP will fail when they can't see the Spring LDAP
 * implementation classes.
 */
public class LdapTemplateWithClassLoaderWrapper
{
    private final LdapTemplate template;

    public LdapTemplateWithClassLoaderWrapper(LdapTemplate template)
    {
        this.template = template;
    }

    static <T> T invokeWithContextClassLoader(CallableWithoutCheckedException<T> runnable)
    {
        Thread current = Thread.currentThread();
        ClassLoader orig = current.getContextClassLoader();

        try
        {
            ClassLoader classLoaderForThisClass = LdapTemplateWithClassLoaderWrapper.class.getClassLoader();
            current.setContextClassLoader(classLoaderForThisClass);

            return runnable.call();
        }
        finally
        {
            current.setContextClassLoader(orig);
        }
    }

    public List search(final Name base, final String filter, final SearchControls controls, final ContextMapper mapper)
    {
        return invokeWithContextClassLoader(new CallableWithoutCheckedException<List>() {
            public List call()
            {
                return template.search(base, filter, controls, mapper);
            }
        });
    }

    public List search(final Name base, final String filter, final SearchControls controls, final ContextMapper mapper,
            final DirContextProcessor processor)
    {
        return invokeWithContextClassLoader(new CallableWithoutCheckedException<List>() {
            public List call()
            {
                return template.search(base, filter, controls, mapper, processor);
            }
        });
    }

    public Object lookup(final String dn)
    {
        return invokeWithContextClassLoader(new CallableWithoutCheckedException<Object>() {
            public Object call()
            {
                return template.lookup(dn);
            }
        });
    }

    public void search(final Name base, final String filter, final SearchControls controls,
            final NameClassPairCallbackHandler handler, final DirContextProcessor processor)
    {
        invokeWithContextClassLoader(new CallableWithoutCheckedException<Void>() {
            public Void call()
            {
                template.search(base, filter, controls, handler, processor);
                return null;
            }
        });
    }

    public void unbind(final Name dn)
    {
        invokeWithContextClassLoader(new CallableWithoutCheckedException<Void>() {
            public Void call()
            {
                template.unbind(dn);
                return null;
            }
        });
    }

    public void bind(final Name dn, final Object obj, final Attributes attributes)
    {
        invokeWithContextClassLoader(new CallableWithoutCheckedException<Void>() {
            public Void call()
            {
                template.bind(dn, obj, attributes);
                return null;
            }
        });
    }

    public void modifyAttributes(final Name dn, final ModificationItem[] mods)
    {
        invokeWithContextClassLoader(new CallableWithoutCheckedException<Void>() {
            public Void call()
            {
                template.modifyAttributes(dn, mods);
                return null;
            }
        });
    }

    public void modifyAttributes(final String dn, final ModificationItem[] mods)
    {
        invokeWithContextClassLoader(new CallableWithoutCheckedException<Void>() {
            public Void call()
            {
                template.modifyAttributes(dn, mods);
                return null;
            }
        });
    }

    public void lookup(final String dn, final String[] attributes, final AttributesMapper mapper)
    {
        invokeWithContextClassLoader(new CallableWithoutCheckedException<Void>() {
            public Void call()
            {
                template.lookup(dn, attributes, mapper);
                return null;
            }
        });
    }

    public void setIgnorePartialResultException(boolean ignore)
    {
        /* This doesn't load classes */
        template.setIgnorePartialResultException(ignore);
    }

    static interface CallableWithoutCheckedException<T>
    {
        T call();
    }
}
