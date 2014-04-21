package com.atlassian.spring.container;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * A custom {@link BeanFactory} used in the {@link SpringContainerContext}. The factory has:
 * An extra createBeanMethod
 */
public class AtlassianBeanFactory extends DefaultListableBeanFactory
{
    public AtlassianBeanFactory(AutowireCapableBeanFactory beanFactory)
    {
        super(beanFactory);
    }

    /**
     * Behaves as {@link AutowireCapableBeanFactory#applyBeanPostProcessorsBeforeInitialization(Object, String)} but it
     * also calls the {@link #parentBeanFactory}'s version if it's a {@link AutowireCapableBeanFactory}
     */
    public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
            throws BeansException
    {
        existingBean = super.applyBeanPostProcessorsBeforeInitialization(existingBean, beanName);

        BeanFactory parentBeanFactory = getParentBeanFactory();
        while (parentBeanFactory != null)
        {
            if (parentBeanFactory instanceof AutowireCapableBeanFactory)
            {
                AutowireCapableBeanFactory autowireCapableParentFactory = (AutowireCapableBeanFactory) parentBeanFactory;
                existingBean = autowireCapableParentFactory.applyBeanPostProcessorsBeforeInitialization(existingBean, beanName);
            }

            if (parentBeanFactory instanceof HierarchicalBeanFactory)
            {
                parentBeanFactory = ((HierarchicalBeanFactory)parentBeanFactory).getParentBeanFactory();
            }
            else
            {
                parentBeanFactory = null;
            }
        }

        return existingBean;
    }

    /**
     * Behaves as {@link AutowireCapableBeanFactory#applyBeanPostProcessorsAfterInitialization(Object, String)} but it 
     * also calls the {@link #parentBeanFactory}'s version if it's a {@link AutowireCapableBeanFactory}
     */
    public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
            throws BeansException
    {
        existingBean = super.applyBeanPostProcessorsAfterInitialization(existingBean, beanName);

        BeanFactory parentBeanFactory = getParentBeanFactory();
        while (parentBeanFactory != null)
        {
            if (parentBeanFactory instanceof AutowireCapableBeanFactory)
            {
                AutowireCapableBeanFactory autowireCapableParentFactory = (AutowireCapableBeanFactory) parentBeanFactory;
                existingBean = autowireCapableParentFactory.applyBeanPostProcessorsAfterInitialization(existingBean, beanName);
            }

            if (parentBeanFactory instanceof HierarchicalBeanFactory)
            {
                parentBeanFactory = ((HierarchicalBeanFactory) parentBeanFactory).getParentBeanFactory();
            }
            else
            {
                parentBeanFactory = null;
            }
        }

        return existingBean;
    }


    public String toString()
    {
        return "toString overridden for performance reasons";
    }
}
