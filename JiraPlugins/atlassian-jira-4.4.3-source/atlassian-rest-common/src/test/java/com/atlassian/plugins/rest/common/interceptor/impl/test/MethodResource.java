package com.atlassian.plugins.rest.common.interceptor.impl.test;

import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;

public class MethodResource
{
    @InterceptorChain(MyInterceptor.class)
    public void run()
    {}

}
