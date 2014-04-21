package com.atlassian.plugins.rest.common.interceptor.impl.test;

import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;

@InterceptorChain(MyInterceptor.class)
public class ClassResource
{
    public void run()
    {}

}