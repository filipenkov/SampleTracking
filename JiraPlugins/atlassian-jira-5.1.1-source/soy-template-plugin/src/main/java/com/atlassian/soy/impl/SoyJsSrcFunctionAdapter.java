package com.atlassian.soy.impl;

import com.atlassian.soy.renderer.JsExpression;
import com.atlassian.soy.renderer.SoyClientFunction;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.template.soy.jssrc.restricted.JsExpr;
import com.google.template.soy.jssrc.restricted.SoyJsSrcFunction;

import java.util.List;
import java.util.Set;

public class SoyJsSrcFunctionAdapter implements SoyJsSrcFunction {

    private final SoyClientFunction function;

    public SoyJsSrcFunctionAdapter(SoyClientFunction function) {
        this.function = function;
    }

    @Override
    public JsExpr computeForJsSrc(List<JsExpr> args) {
        return new JsExpr(function.generate(Lists.transform(args, new Function<JsExpr, JsExpression>() {
            public JsExpression apply(JsExpr from) {
                return new JsExpression(from.getText());
            }
        }).toArray(new JsExpression[args.size()])).getText(), Integer.MAX_VALUE);
    }

    @Override
    public String getName() {
        return function.getName();
    }

    @Override
    public Set<Integer> getValidArgsSizes() {
        return function.validArgSizes();
    }
}
