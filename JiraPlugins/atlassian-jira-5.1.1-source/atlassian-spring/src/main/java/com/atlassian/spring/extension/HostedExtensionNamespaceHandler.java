package com.atlassian.spring.extension;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class HostedExtensionNamespaceHandler extends NamespaceHandlerSupport
{
    public void init()
    {
        super.registerBeanDefinitionDecoratorForAttribute("override", new HostedOverrideBeanDefinitionDecorator());
    }
}
