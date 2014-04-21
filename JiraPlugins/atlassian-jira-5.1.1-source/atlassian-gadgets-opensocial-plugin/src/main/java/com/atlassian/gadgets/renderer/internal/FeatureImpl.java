package com.atlassian.gadgets.renderer.internal;

import com.atlassian.gadgets.spec.Feature;

public class FeatureImpl implements Feature
{
    private org.apache.shindig.gadgets.spec.Feature shindigFeature;

    public FeatureImpl(org.apache.shindig.gadgets.spec.Feature feature)
    {
        this.shindigFeature = feature;
    }

    public String getParameterValue(String paramName)
    {
        return shindigFeature.getParams().get(paramName);
    }

    public String getName()
    {
        return shindigFeature.getName();
    }
}
