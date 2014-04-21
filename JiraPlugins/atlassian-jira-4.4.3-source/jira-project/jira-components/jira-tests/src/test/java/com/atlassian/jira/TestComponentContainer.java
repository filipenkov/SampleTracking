package com.atlassian.jira;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.plugin.osgi.hostcomponents.HostComponentRegistration;
import com.atlassian.plugin.osgi.hostcomponents.impl.DefaultComponentRegistrar;
import org.picocontainer.Parameter;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class TestComponentContainer extends ListeningTestCase
{
    private final ComponentContainer.Scope PROVIDED = ComponentContainer.Scope.PROVIDED;
    private final ComponentContainer.Scope INTERNAL = ComponentContainer.Scope.INTERNAL;

    @Test
    public void testProvideImplementationGivesAvailableComponent() throws Exception
    {
        final ComponentContainer container = new ComponentContainer();
        container.implementation(PROVIDED, One.class, OneTwoThreeImpl.class);

        final DefaultComponentRegistrar registrar = new DefaultComponentRegistrar();
        container.getHostComponentProvider().provide(registrar);
        final List<HostComponentRegistration> components = registrar.getRegistry();
        assertNotNull(components);
        assertEquals(1, components.size());
        final HostComponentRegistration component = components.iterator().next();
        assertNotNull(component);

        final Object instance = component.getInstance();
        assertNotNull(instance);
        assertTrue(instance.toString(), instance instanceof OneTwoThreeImpl);

        assertEquals(1, component.getMainInterfaceClasses().length);
        assertEquals(One.class, component.getMainInterfaceClasses()[0]);

        assertNotNull(container.getComponentInstance(One.class));
    }

    @Test
    public void testMultipleProvideGivesAllComponentInterfaces() throws Exception
    {
        final ComponentContainer container = new ComponentContainer();
        DelegateComponentAdapter.Builder.builderFor(OneTwoThreeImpl.class).implementing(One.class).implementing(Two.class).registerWith(PROVIDED,
            container);

        final DefaultComponentRegistrar registrar = new DefaultComponentRegistrar();
        container.getHostComponentProvider().provide(registrar);
        final List<HostComponentRegistration> components = registrar.getRegistry();
        assertNotNull(components);
        assertEquals(2, components.size());
        final HostComponentRegistration component = components.get(0);
        final List<Class<?>> interfaces = CollectionUtil.transform(components, new Function<HostComponentRegistration, Class<?>>()
        {
            public Class<?> get(final HostComponentRegistration input)
            {
                final Class<?>[] mainInterfaceClasses = input.getMainInterfaceClasses();
                assertNotNull(mainInterfaceClasses);
                assertEquals(1, mainInterfaceClasses.length);
                return mainInterfaceClasses[0];
            }
        });
        assertTrue(interfaces.contains(One.class));
        assertTrue(interfaces.contains(Two.class));
        assertFalse(interfaces.contains(Three.class));
        assertTrue(component.getInstance() instanceof OneTwoThreeImpl);

        assertNotNull(container.getComponentInstance(One.class));
    }

    @Test
    public void testProvideInstanceGivesAvailableComponent() throws Exception
    {
        final ComponentContainer container = new ComponentContainer();
        final OneTwoThreeImpl instance = new OneTwoThreeImpl();
        container.instance(PROVIDED, One.class, instance);

        final DefaultComponentRegistrar registrar = new DefaultComponentRegistrar();
        container.getHostComponentProvider().provide(registrar);
        final List<HostComponentRegistration> components = registrar.getRegistry();
        assertNotNull(components);
        assertEquals(1, components.size());
        final HostComponentRegistration component = components.iterator().next();
        assertNotNull(component);

        assertSame(instance, component.getInstance());
        assertTrue(component.getInstance() instanceof OneTwoThreeImpl);

        assertEquals(1, component.getMainInterfaceClasses().length);
        assertEquals(One.class, component.getMainInterfaceClasses()[0]);

        assertNotNull(container.getComponentInstance(One.class));

    }

    @Test
    public void testInternalInstanceGivesNoAvailableComponent() throws Exception
    {
        final ComponentContainer container = new ComponentContainer();
        container.instance(INTERNAL, One.class, new OneTwoThreeImpl());

        final DefaultComponentRegistrar registrar = new DefaultComponentRegistrar();
        container.getHostComponentProvider().provide(registrar);
        final List<HostComponentRegistration> components = registrar.getRegistry();
        assertNotNull(components);
        assertEquals(0, components.size());
        assertNotNull(container.getComponentInstance(One.class));
    }

    @Test
    public void testInternalImplementationGivesNoAvailableComponent() throws Exception
    {
        final ComponentContainer container = new ComponentContainer();
        container.implementation(INTERNAL, One.class, OneTwoThreeImpl.class);

        final DefaultComponentRegistrar registrar = new DefaultComponentRegistrar();
        container.getHostComponentProvider().provide(registrar);
        final List<HostComponentRegistration> components = registrar.getRegistry();
        assertNotNull(components);
        assertEquals(0, components.size());
        assertNotNull(container.getComponentInstance(One.class));
    }

    @Test
    public void testInternalConcreteImplementationGivesNoAvailableComponent() throws Exception
    {
        final ComponentContainer container = new ComponentContainer();
        container.implementation(INTERNAL, OneTwoThreeImpl.class);

        final DefaultComponentRegistrar registrar = new DefaultComponentRegistrar();
        container.getHostComponentProvider().provide(registrar);
        final List<HostComponentRegistration> components = registrar.getRegistry();
        assertNotNull(components);
        assertEquals(0, components.size());
        assertNotNull(container.getComponentInstance(OneTwoThreeImpl.class));
    }

    @Test
    public void testProvideImplementationTwice() throws Exception
    {
        final ComponentContainer container = new ComponentContainer();
        container.implementation(PROVIDED, One.class, OneTwoThreeImpl.class);
        try
        {
            container.implementation(PROVIDED, One.class, OneTwoThreeImpl.class);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testProvideImplementationWithParameter() throws Exception
    {
        final ComponentContainer container = new ComponentContainer();
        container.implementation(PROVIDED, One.class, OneTwoThreeImpl.class);
        try
        {
            container.implementation(PROVIDED, One.class, OneTwoThreeImpl.class, new Parameter[0]);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testProvideInstanceTwice() throws Exception
    {
        final ComponentContainer container = new ComponentContainer();
        container.implementation(PROVIDED, One.class, OneTwoThreeImpl.class);
        try
        {
            container.instance(PROVIDED, One.class, new OneTwoThreeImpl());
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testProvideImplementationDefaultConstructorTwice() throws Exception
    {
        final ComponentContainer container = new ComponentContainer();
        container.implementation(PROVIDED, One.class, OneTwoThreeImpl.class);
        try
        {
            container.implementationUseDefaultConstructor(PROVIDED, One.class, OneTwoThreeImpl.class);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    //JRADEV-3445: Make sure that we don't do locale sensitive operations when generating the spring name. 
    @Test
    public void testExtractSpringLikeBeanNameFromInterfaceTurkishLocale()
    {
        Locale currentLocale = Locale.getDefault();
        Locale.setDefault(new Locale("tr", "TR"));
        try
        {
            assertEquals("issueFactory", ComponentContainer.extractSpringLikeBeanNameFromInterface(new HashSet<String>(), IssueFactory.class));
        }
        finally
        {
            Locale.setDefault(currentLocale);
        }
    }

    @Test
    public void testExtractSpringLikeBeanNameFromInterface()
    {
        assertEquals("one", ComponentContainer.extractSpringLikeBeanNameFromInterface(new HashSet<String>(), One.class));
        assertEquals("one", ComponentContainer.extractSpringLikeBeanNameFromInterface(new HashSet<String>(), One.class, Two.class, Three.class));
        // The name chosen is based on the alphabetically-first interface class name
        assertEquals("three", ComponentContainer.extractSpringLikeBeanNameFromInterface(new HashSet<String>(), Two.class, Three.class));
    }

    @Test
    public void testExtractSpringLikeBeanNameFromInterfaceWithDuplicateKeys()
    {
        HashSet<String> keys = new HashSet<String>();
        assertEquals("one", ComponentContainer.extractSpringLikeBeanNameFromInterface(keys, One.class));
        keys.add("one");
        assertEquals("one1", ComponentContainer.extractSpringLikeBeanNameFromInterface(keys, One.class));
        keys.add("one1");
        assertEquals("one2", ComponentContainer.extractSpringLikeBeanNameFromInterface(keys, One.class));
    }

    @Test
    public void testExtractSpringLikeBeanNameFromInterfaceWithNullClasses()
    {
        try
        {
            ComponentContainer.extractSpringLikeBeanNameFromInterface(new HashSet<String>(), (Class<?>[])null);
            fail("Should not accept null interface list");
        }
        catch (IllegalArgumentException ex)
        {
            // expected
        }
    }

    @Test
    public void testExtractSpringLikeBeanNameFromInterfaceWithEmptyClassesArray()
    {
        try
        {
            ComponentContainer.extractSpringLikeBeanNameFromInterface(new HashSet<String>(), new Class[0]);
            fail("Should not accept empty interface list");
        }
        catch (IllegalArgumentException ex)
        {
            // expected
        }
    }

    public interface One
    {}

    public interface Two
    {}

    public interface Three
    {}

    public interface IssueFactory
    {}

    public static class OneTwoThreeImpl implements One, Two, Three
    {}
}
