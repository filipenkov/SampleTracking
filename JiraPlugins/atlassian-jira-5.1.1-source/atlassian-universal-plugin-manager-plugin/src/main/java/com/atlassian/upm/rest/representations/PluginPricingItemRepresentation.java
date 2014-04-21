package com.atlassian.upm.rest.representations;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import com.atlassian.plugins.domain.model.plugin.PluginPricing;

import com.google.common.base.Function;

public class PluginPricingItemRepresentation
{
    @JsonProperty private final String description;
    @JsonProperty private final Float usdAmount;
    @JsonProperty private final Integer unitCount;

    @JsonCreator
    public PluginPricingItemRepresentation(@JsonProperty("description") String description,
                                           @JsonProperty("usdAmount") Float usdAmount,
                                           @JsonProperty("unitCount") Integer unitCount)
    {
        this.description = description;
        this.usdAmount = usdAmount;
        this.unitCount = unitCount;
    }

    public PluginPricingItemRepresentation(PluginPricing pricing)
    {
        this.description = pricing.getDescription();
        this.usdAmount = pricing.getUsdAmount();
        this.unitCount = pricing.getUnitCount();
    }

    public String getDescription()
    {
        return description;
    }

    public Float getUsdAmount()
    {
        return usdAmount;
    }

    public Integer getUnitCount()
    {
        return unitCount;
    }

    public static Function<PluginPricing, PluginPricingItemRepresentation> toPricingItem = new Function<PluginPricing, PluginPricingItemRepresentation>()
    {
        @Override
        public PluginPricingItemRepresentation apply(PluginPricing pp)
        {
            return new PluginPricingItemRepresentation(pp);
        }
    };
}
