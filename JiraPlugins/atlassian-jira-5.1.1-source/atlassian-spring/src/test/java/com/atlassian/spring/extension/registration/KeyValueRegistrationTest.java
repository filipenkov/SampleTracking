package com.atlassian.spring.extension.registration;

import junit.framework.TestCase;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.C;
import org.springframework.beans.factory.BeanFactory;

public class KeyValueRegistrationTest extends TestCase
{
    public void testNormalOperation() throws RegistrationException
    {
        testSuccessfulRegistration(new ThingToRegister());
    }

    public void testMatchesSubclass() throws RegistrationException
    {
        testSuccessfulRegistration(new SubThingToRegister());
    }

    public void testTargetBeanNotFound()
    {
        Object bean = new ThingToRegister();

        Mock mockBeanFactory = new Mock(BeanFactory.class);

        mockBeanFactory.matchAndReturn("getBean", "target1", null);
        mockBeanFactory.matchAndReturn("getBean", "bean1", bean);

        KeyValueRegistration registration = new KeyValueRegistration("target1", "key1", "bean1", "methodWithNoKey");

        try
        {
            registration.register((BeanFactory) mockBeanFactory.proxy());
            fail("Expected registration exception");
        }
        catch (RegistrationException e)
        {
            // expected
        }
    }

    public void testSourceBeanNotFound()
    {
        Mock mockTarget = new Mock(ThingToRegisterWith.class);
        Mock mockBeanFactory = new Mock(BeanFactory.class);

        mockBeanFactory.matchAndReturn("getBean", "target1", mockTarget.proxy());
        mockBeanFactory.matchAndReturn("getBean", "bean1", null);

        KeyValueRegistration registration = new KeyValueRegistration("target1", "key1", "bean1", "methodWithNoKey");

        try
        {
            registration.register((BeanFactory) mockBeanFactory.proxy());
            fail("Expected registration exception");
        }
        catch (RegistrationException e)
        {
            // expected
        }
    }

    public void testMethodWithWrongArgs()
    {
        Object bean = new ThingToRegister();

        Mock mockTarget = new Mock(ThingToRegisterWith.class);
        Mock mockBeanFactory = new Mock(BeanFactory.class);

        mockBeanFactory.matchAndReturn("getBean", "target1", mockTarget.proxy());
        mockBeanFactory.matchAndReturn("getBean", "bean1", bean);

        KeyValueRegistration registration = new KeyValueRegistration("target1", "key1", "bean1", "methodWithNoKey");

        try
        {
            registration.register((BeanFactory) mockBeanFactory.proxy());
            fail("Expected registration exception");
        }
        catch (RegistrationException e)
        {
            // expected
        }
    }

    public void testMethodWithWrongClass()
    {
        Object bean = new ThingToRegister();

        Mock mockTarget = new Mock(ThingToRegisterWith.class);
        Mock mockBeanFactory = new Mock(BeanFactory.class);

        mockBeanFactory.matchAndReturn("getBean", "target1", mockTarget.proxy());
        mockBeanFactory.matchAndReturn("getBean", "bean1", bean);

        try
        {
            KeyValueRegistration registration = new KeyValueRegistration("target1", "key1", "bean1", "methodWithWrongType");
            registration.register((BeanFactory) mockBeanFactory.proxy());
            fail("Expected registration exception");
        }
        catch (RegistrationException e)
        {
            // expected
        }
    }

    private void testSuccessfulRegistration(Object bean) throws RegistrationException
    {
        Mock mockTarget = new Mock(ThingToRegisterWith.class);
        Mock mockBeanFactory = new Mock(BeanFactory.class);

        mockBeanFactory.matchAndReturn("getBean", "target1", mockTarget.proxy());
        mockBeanFactory.matchAndReturn("getBean", "bean1", bean);

        mockTarget.expect("addThingToRegister", C.args(C.eq("key1"), C.same(bean)));

        KeyValueRegistration registration = new KeyValueRegistration("target1", "key1", "bean1", "addThingToRegister");
        registration.register((BeanFactory) mockBeanFactory.proxy());
        mockTarget.verify();
    }
}
