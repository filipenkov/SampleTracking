package com.atlassian.soy.impl;

import com.atlassian.soy.renderer.JsExpression;
import com.atlassian.soy.renderer.SoyClientFunction;
import com.atlassian.soy.renderer.SoyServerFunction;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import java.util.Map;
import java.util.Set;

public class ConcatFunction implements SoyClientFunction, SoyServerFunction<Object> {

    public String getName() {
        return "concat";
    }

    public JsExpression generate(JsExpression... jsExpressions) {
        JsExpression listA = jsExpressions[0];
        JsExpression listB = jsExpressions[1];
        return new JsExpression("atl_soy.concat(" + listA.getText() + ", " + listB.getText() + ")");
    }

    public Object apply(Object... args) {
        if (args[0] instanceof Iterable && args[1] instanceof Iterable) {
            return concatIterables((Iterable) args[0], (Iterable) args[1]);
        }

        if (args[0] instanceof Map && args[1] instanceof Map) {
            return concatMaps((Map) args[0], (Map) args[1]);
        }

        throw new IllegalArgumentException("concat() accepts two arguments that are either both Maps or both Iterables.");
    }

    private Iterable concatIterables(Iterable listA, Iterable listB) {
        return Iterables.concat(listA, listB);
    }

    private Map concatMaps(Map mapA, Map mapB) {
        return ImmutableMap.builder().putAll(mapA).putAll(mapB).build();
    }

    public Set<Integer> validArgSizes() {
        return ImmutableSet.of(2);
    }
}
