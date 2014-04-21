package com.atlassian.jira;

import com.atlassian.crowd.embedded.ofbiz.OfBizApplicationDao;
import com.atlassian.crowd.embedded.ofbiz.OfBizDirectoryDao;
import com.atlassian.crowd.embedded.ofbiz.OfBizGroupDao;
import com.atlassian.crowd.embedded.ofbiz.OfBizInternalMembershipDao;
import com.atlassian.crowd.embedded.ofbiz.OfBizUserDao;
import com.atlassian.event.internal.EventExecutorFactoryImpl;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.local.testutils.MultiTenantContextTestUtils;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.atlassian.plugin.event.impl.DefaultPluginEventManager;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.picocontainer.Parameter;
import org.quartz.impl.StdScheduler;
import webwork.action.ActionSupport;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A test that makes sure that classes that we inject do not have concrete classes in their constructors
 */
public class TestInjectionParameters extends LegacyJiraMockTestCase
{
    private static final Logger log = Logger.getLogger(TestInjectionParameters.class);

    private static final String CLASS_EXT = ".class";
    private static final Set<Class> EXCLUSIONS = new HashSet<Class>();

    static
    {
        EXCLUSIONS.add(OfBizApplicationDao.class);
        EXCLUSIONS.add(EventExecutorFactoryImpl.class);
        EXCLUSIONS.add(ExternalLinkUtilImpl.class);
        EXCLUSIONS.add(DefaultPluginEventManager.class);
        // StdScheduler comes from Quartz Scheduler library.
        EXCLUSIONS.add(StdScheduler.class);
        // Ignore these temporarily until we update Crowd Embedded
        EXCLUSIONS.add(OfBizDirectoryDao.class);
        EXCLUSIONS.add(OfBizUserDao.class);
        EXCLUSIONS.add(OfBizGroupDao.class);
        EXCLUSIONS.add(OfBizInternalMembershipDao.class);
    }


    @Test
    public void testConstructorUsage() throws ClassNotFoundException
    {
        MultiTenantContextTestUtils.setupMultiTenantSystem();

        final ContainerRegistrar registrar = new ContainerRegistrar();
        final MyComponentContainer container = new MyComponentContainer();
        registrar.registerComponents(container, true);

        final Set<Class> classes = getClassesForPackage("com.atlassian");
        final Set<Class> classesToCheck = new HashSet<Class>();
        //add all implementation classes from pico.  They could get dodgy stuff injected!
        classesToCheck.addAll(container.getImplementationsToCheck());

        //add all webwork classes since they get injected with compnents
        for (Class aClass : classes)
        {
            if (ActionSupport.class.isAssignableFrom(aClass) && !aClass.getName().contains("Test"))
            {
                classesToCheck.add(aClass);
            }
        }

        final Set<Class> offenders = new HashSet<Class>();
        for (Class classToCheck : classesToCheck)
        {
            if (EXCLUSIONS.contains(classToCheck))
            {
                continue;
            }
            final Constructor[] constructors = classToCheck.getConstructors();
            for (Constructor constructor : constructors)
            {
                final Class[] parameterTypes = constructor.getParameterTypes();
                for (Class parameterType : parameterTypes)
                {
                    if (isOffender(parameterType, container))
                    {
                        offenders.add(classToCheck);
                        break;
                    }
                }
            }
        }

        if (!offenders.isEmpty())
        {
            final StringBuilder out = new StringBuilder();
            out.append("Found ").append(offenders.size()).append(" classes that are injectable and take concrete classes.\n").
                    append("They may break if logging & profiling is enabled. Change these classes to have interfaces injected or add them to the exclusions:\n");
            for (Class offender : offenders)
            {
                out.append("* ").append(offender.getCanonicalName()).append("\n");
            }
            fail(out.toString());
        }
    }

    private boolean isOffender(Class parameter, MyComponentContainer container)
    {
        if (parameter.isPrimitive())
        {
            return false;
        }
        if (parameter.isInterface())
        {
            return false;
        }

        //I suppose concrete parameters are allowed if there's no interface for them.
        if (parameter.getInterfaces().length == 0)
        {
            return false;
        }

        if (parameter.equals(StepDescriptor.class) || parameter.equals(ActionDescriptor.class))
        {
            return false;
        }

        Set<Class> nonProfiledClasses = container.getNonProfiledClasses();
        if (nonProfiledClasses.contains(parameter))
        {
            return false;
        }

        return true;
    }

    private static Set<Class> getClassesForPackage(String pckgname) throws ClassNotFoundException
    {
        // This will hold a list of directories matching the pckgname. There may be more than one if a package is split over multiple jars/paths
        List<File> directories = new ArrayList<File>();
        try
        {
            ClassLoader cld = Thread.currentThread().getContextClassLoader();
            if (cld == null)
            {
                throw new ClassNotFoundException("Can't get class loader.");
            }
            String path = pckgname.replace('.', '/');
            // Ask for all resources for the path
            final Enumeration<URL> resources = cld.getResources(path);
            while (resources.hasMoreElements())
            {
                directories.add(new File(URLDecoder.decode(resources.nextElement().getPath(), "UTF-8")));
            }
        }
        catch (NullPointerException x)
        {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Null pointer exception)");
        }
        catch (UnsupportedEncodingException encex)
        {
            throw new ClassNotFoundException(pckgname + " does not appear to be a valid package (Unsupported encoding)");
        }
        catch (IOException ioex)
        {
            throw new ClassNotFoundException("IOException was thrown when trying to get all resources for " + pckgname);
        }

        final Set<Class> classes = new HashSet<Class>();
        // For every directory identified capture all the .class files
        for (File directory : directories)
        {
            scanDirectories(pckgname, classes, directory);
        }
        return classes;
    }

    private static void scanDirectories(String pckgname, Set<Class> classes, File directory)
            throws ClassNotFoundException
    {
        // Get the list of the files contained in the package
        File[] files = directory.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    scanDirectories(pckgname + "." + file.getName(), classes, file);
                }
                // we are only interested in .class files
                String fileName = file.getName();
                if (fileName.endsWith(CLASS_EXT))
                {
                    // removes the .class extension
                    try
                    {
                        classes.add(Class.forName(pckgname + '.' + fileName.substring(0, fileName.length() - CLASS_EXT.length())));
                    }
                    catch (NoClassDefFoundError e)
                    {
                        // do nothing. this class hasn't been found by the loader, and we don't care.
                    }
                    catch (ExceptionInInitializerError e)
                    {
                        //also do nothing.
                    }
                }
            }
        }
    }

    static class MyComponentContainer extends ComponentContainer
    {
        // Classes that are not registered against and interface will *not* be profiled.  They should
        // be excluded when checking for illegal parameters
        private Set<Class> nonProfiledClasses = new HashSet<Class>();
        // The actual classes that are components need to be checked for illegal parameters.
        private Set<Class> implementationsToCheck = new HashSet<Class>();

        @Override
        void instance(Scope scope, Object instance)
        {
            super.instance(scope, instance);
            nonProfiledClasses.add(instance.getClass());
        }

        @Override
        void instance(Scope scope, String key, Object instance)
        {
            super.instance(scope, key, instance);
            nonProfiledClasses.add(instance.getClass());
        }

        @Override
        <T, S extends T> void instance(Scope scope, Class<T> key, S instance)
        {
            super.instance(scope, key, instance);
            if (!key.isInterface())
            {
                nonProfiledClasses.add(key);
            }
            else
            {
                implementationsToCheck.add(instance.getClass());
            }

        }

        @Override
        void implementation(Scope scope, Class<?> implementation)
        {
            super.implementation(scope, implementation);
            nonProfiledClasses.add(implementation);
        }

        @Override
        <T> void implementation(Scope scope, Class<? super T> key, Class<T> implementation)
        {
            super.implementation(scope, key, implementation);
            if (!key.isInterface())
            {
                nonProfiledClasses.add(key);
            }
            else
            {
                implementationsToCheck.add(implementation);
            }
        }

        @Override
        <T> void implementation(Scope scope, Class<? super T> key, Class<T> implementation, Object... parameterKeys)
        {
            super.implementation(scope, key, implementation, parameterKeys);
            if (!key.isInterface())
            {
                nonProfiledClasses.add(key);
            }
            else
            {
                implementationsToCheck.add(implementation);
            }
        }

        @Override
        <T> void implementation(Scope scope, Class<? super T> key, Class<T> implementation, Parameter[] parameters)
        {
            super.implementation(scope, key, implementation, parameters);
            if (!key.isInterface())
            {
                nonProfiledClasses.add(key);
            }
            else
            {
                implementationsToCheck.add(implementation);
            }
        }

        @Override
        <T> void implementationUseDefaultConstructor(Scope scope, Class<T> key, Class<? extends T> implementation)
        {
            super.implementationUseDefaultConstructor(scope, key, implementation);
            if (!key.isInterface())
            {
                nonProfiledClasses.add(key);
            }
            else
            {
                implementationsToCheck.add(implementation);
            }
        }

        public Set<Class> getNonProfiledClasses()
        {
            return nonProfiledClasses;
        }

        public Set<Class> getImplementationsToCheck()
        {
            return implementationsToCheck;
        }
    }
}
