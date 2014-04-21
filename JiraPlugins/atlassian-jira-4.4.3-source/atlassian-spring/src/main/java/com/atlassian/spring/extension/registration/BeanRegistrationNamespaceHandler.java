package com.atlassian.spring.extension.registration;

import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;
import java.util.ArrayList;

public class BeanRegistrationNamespaceHandler implements BeanDefinitionDecorator
{
    private static final String REGISTRATION_BEAN_NAME = "__registration_bean";


    public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext)
    {
        Element element = (Element)node;

        BeanDefinition registration = createRegistrationIfNecessary(parserContext);
        List registrations = (List) registration.getPropertyValues().getPropertyValue("registrations").getValue();
        registrations.add(new KeyValueRegistration(element.getAttribute("target"), element.getAttribute("key"), definition.getBeanName(), element.getAttribute("registrationMethod")));
        return definition;
    }

    private BeanDefinition createRegistrationIfNecessary(ParserContext parserContext)
    {
        if (!parserContext.getRegistry().containsBeanDefinition(REGISTRATION_BEAN_NAME))
        {
            BeanDefinitionBuilder registrationBean = BeanDefinitionBuilder.rootBeanDefinition(BeanRegistration.class);
            registrationBean.addPropertyValue("registrations", new ArrayList());
            parserContext.getRegistry().registerBeanDefinition(REGISTRATION_BEAN_NAME, registrationBean.getBeanDefinition());
        }

        return parserContext.getRegistry().getBeanDefinition(REGISTRATION_BEAN_NAME);
    }
}
