package com.atlassian.spring.extension;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import com.atlassian.spring.extension.registration.BeanRegistrationNamespaceHandler;

public class AtlassianExtensionNamespaceHandler extends NamespaceHandlerSupport
{
    public void init()
    {
        registerBeanDefinitionDecorator("registration", new BeanRegistrationNamespaceHandler());
    }
}
