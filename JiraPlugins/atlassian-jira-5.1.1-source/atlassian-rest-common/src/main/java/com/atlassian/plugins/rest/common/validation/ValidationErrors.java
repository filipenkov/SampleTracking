package com.atlassian.plugins.rest.common.validation;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents errors from a validation failure.  Should contain at least one {@link ValidationError}}
 *
 * @since 2.0
 */
@XmlRootElement
public class ValidationErrors
{
    private List<ValidationError> errors = new ArrayList<ValidationError>();

    public List<ValidationError> getErrors()
    {
        return errors;
    }

    public void addError(ValidationError error)
    {
        errors.add(error);
    }

    public void setErrors(List<ValidationError> errors)
    {
        this.errors = errors;
    }
}
