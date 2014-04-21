package com.atlassian.spring.extension.registration;

import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.Method;

class KeyValueRegistration implements Registration
{
    private final String targetBeanName;

    private final String key;
    private final String beanNameToRegister;
    private final String registrationMethodName;

    public KeyValueRegistration(String targetBeanName, String key, String beanNameToRegister, String registrationMethodName)
    {
        this.targetBeanName = targetBeanName;
        this.key = key;
        this.beanNameToRegister = beanNameToRegister;
        this.registrationMethodName = registrationMethodName;
    }

    public void register(BeanFactory beanFactory) throws RegistrationException
    {
        Object beanToRegister = findBeanToRegister(beanFactory);
        Object targetBean = findTargetBean(beanFactory);

        try
        {
            Method registrationMethod = findRegistrationMethod(targetBean.getClass(), beanToRegister.getClass(), registrationMethodName);
            registrationMethod.invoke(targetBean, new Object[] { key, beanToRegister });
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RegistrationException("Unable to register bean " + beanNameToRegister + " with " + targetBeanName + ": " + e.getMessage(), e);
        }
    }

    private Object findBeanToRegister(BeanFactory beanFactory)
            throws RegistrationException
    {
        Object beanToRegister = beanFactory.getBean(beanNameToRegister);
        if (beanToRegister == null)
            throw new RegistrationException("Unable to register " + beanNameToRegister + " with " + targetBeanName + ": bean with name " + beanNameToRegister + " not found.");
        return beanToRegister;
    }

    private Object findTargetBean(BeanFactory beanFactory)
            throws RegistrationException
    {
        Object targetBean = beanFactory.getBean(targetBeanName);
        if (targetBean == null)
            throw new RegistrationException("Unable to register " + beanNameToRegister + " with " + targetBeanName + ": bean with name " + targetBeanName + " not found.");
        return targetBean;
    }

    private Method findRegistrationMethod(Class targetClass, Class classToRegister, String registrationMethodName) throws NoSuchMethodException
    {
        for (int i = 0; i < targetClass.getMethods().length; i++)
        {
            Method method = targetClass.getMethods()[i];

            if (method.getName().equals(registrationMethodName))
            {
                Class[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 2 && parameterTypes[0].isAssignableFrom(String.class) && parameterTypes[1].isAssignableFrom(classToRegister))
                    return method;
            }
        }

        throw new NoSuchMethodException("No registration method " + registrationMethodName + " found on " + targetClass.getName() + " for type " + targetClass.getName());
    }
}
