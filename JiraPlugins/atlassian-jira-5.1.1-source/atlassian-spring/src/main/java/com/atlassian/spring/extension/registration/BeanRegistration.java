package com.atlassian.spring.extension.registration;

import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.BeansException;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 */
public class BeanRegistration implements BeanFactoryAware, InitializingBean
{
    private BeanFactory beanFactory;
    private List/*<Registration>*/ registrations = new ArrayList();

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException
    {
        this.beanFactory = beanFactory;
    }

    public void setRegistrations(List registrations)
    {
        this.registrations = registrations;
    }

    public List getRegistrations()
    {
        return registrations;
    }

    public void afterPropertiesSet() throws RegistrationException
    {
        for (Iterator it = registrations.iterator(); it.hasNext();)
        {
            Registration registration = (Registration) it.next();
            registration.register(beanFactory);
            it.remove();
        }
    }
}
