package com.atlassian.soy.impl;

import com.atlassian.soy.renderer.SoyServerFunction;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.tofu.restricted.SoyTofuFunction;

import java.util.List;
import java.util.Set;

public class SoyTofuFunctionAdapter implements SoyTofuFunction {

    private final SoyServerFunction<?> soyServerFunction;

    public SoyTofuFunctionAdapter(SoyServerFunction soyServerFunction) {
        this.soyServerFunction = soyServerFunction;
    }

    @Override
    public SoyData computeForTofu(List<SoyData> args) {
        final Object[] pluginArgs = Lists.transform(args, new Function<SoyData, Object>() {
            public Object apply(SoyData from) {
                return SoyDataConverter.convertFromSoyData(from);
            }
        }).toArray();
        return SoyDataConverter.convertToSoyData(soyServerFunction.apply(pluginArgs));
    }

    @Override
    public String getName() {
        return soyServerFunction.getName();
    }

    @Override
    public Set<Integer> getValidArgsSizes() {
        return soyServerFunction.validArgSizes();
    }
}
