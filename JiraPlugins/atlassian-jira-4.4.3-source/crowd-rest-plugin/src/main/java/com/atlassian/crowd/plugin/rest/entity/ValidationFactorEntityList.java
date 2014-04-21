package com.atlassian.crowd.plugin.rest.entity;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * List of ValidationFactors.
 */
@XmlRootElement (name = "validation-factors")
@XmlAccessorType (XmlAccessType.FIELD)
public class ValidationFactorEntityList
{
    @XmlElements (@XmlElement (name="validation-factor", type= ValidationFactorEntity.class))
    private final List<ValidationFactorEntity> validationFactors;

    private ValidationFactorEntityList()
    {
        validationFactors = new ArrayList<ValidationFactorEntity>();
    }

    public ValidationFactorEntityList(final List<ValidationFactorEntity> validationFactors)
    {
        this.validationFactors = validationFactors;
    }

    public List<ValidationFactorEntity> getValidationFactors()
    {
        return validationFactors;
    }
}
