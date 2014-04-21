package com.atlassian.soy.impl;

import com.atlassian.soy.renderer.SoyClientFunction;
import com.atlassian.soy.renderer.SoyServerFunction;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.jssrc.restricted.JsExpr;
import com.google.template.soy.jssrc.restricted.SoyJsSrcFunction;
import com.google.template.soy.tofu.restricted.SoyTofuFunction;

import java.util.List;
import java.util.Set;

public class CompositeFunctionAdaptor implements SoyJsSrcFunction, SoyTofuFunction
{

    private final SoyTofuFunctionAdapter serverAdaptor;
    private final SoyJsSrcFunctionAdapter clientAdaptor;

    public CompositeFunctionAdaptor(Object function)
    {
        this.serverAdaptor = new SoyTofuFunctionAdapter((SoyServerFunction) function);
        this.clientAdaptor = new SoyJsSrcFunctionAdapter((SoyClientFunction) function);
    }

    @Override
    public JsExpr computeForJsSrc(List<JsExpr> args)
    {
        return clientAdaptor.computeForJsSrc(args);
    }

    @Override
    public SoyData computeForTofu(List<SoyData> args)
    {
        return serverAdaptor.computeForTofu(args);
    }

    @Override
    public String getName()
    {
        return serverAdaptor.getName(); // Is doesn't matter which one is called
    }

    @Override
    public Set<Integer> getValidArgsSizes()
    {
        return serverAdaptor.getValidArgsSizes(); // Is doesn't matter which one is called
    }
}
