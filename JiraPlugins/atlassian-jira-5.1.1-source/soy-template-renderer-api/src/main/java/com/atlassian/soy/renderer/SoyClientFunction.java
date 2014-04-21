package com.atlassian.soy.renderer;


import java.util.Set;

public interface SoyClientFunction
{

    String getName();

    JsExpression generate(JsExpression... args);

    Set<Integer> validArgSizes();
    
}
