package com.atlassian.spring.extension.registration;

import org.springframework.beans.factory.BeanFactory;

/**
 * Created by IntelliJ IDEA.
* User: cmiller
* Date: Sep 20, 2007
* Time: 12:19:57 PM
* To change this template use File | Settings | File Templates.
*/
interface Registration
{
    void register(BeanFactory beanFactory) throws RegistrationException;
}
