package com.atlassian.plugins.rest.common.validation;

import com.atlassian.plugins.rest.common.interceptor.MethodInvocation;
import com.atlassian.plugins.rest.common.interceptor.ResourceInterceptor;
import com.atlassian.sal.api.message.I18nResolver;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;

import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * Uses JSR-303 to validate incoming entity objects.  If validation fails, it will return a 400 with a {@link ValidationErrors}
 * instance containing the constraint violations.  The error messages will be processed with either SAL or the
 * provided custom {@link MessageInterpolator}.
 *
 * @since 2.0
 */
public class ValidationInterceptor implements ResourceInterceptor
{
    private final ValidatorFactory factory;

    public ValidationInterceptor(I18nResolver i18nResolver)
    {
        this(new SalMessageInterpolator(i18nResolver));

    }

    public ValidationInterceptor(MessageInterpolator messageInterpolator)
    {
        // Yes, this cast is unnecessary in Java 6, but seems to be required in Java 5
        this.factory = ((Configuration)Validation.byDefaultProvider().configure().messageInterpolator(messageInterpolator)).buildValidatorFactory();
    }

    public void intercept(MethodInvocation invocation) throws IllegalAccessException, InvocationTargetException
    {
        Validator validator = factory.getValidator();

        // find the entity for the resource method
        int entityIndex = -1;
        AbstractResourceMethod method = invocation.getMethod();
        for (int i = 0; i < method.getParameters().size(); i++) {
            Parameter parameter = method.getParameters().get(i);

            if (Parameter.Source.ENTITY == parameter.getSource()) {
                entityIndex = i;
                break;
            }
        }


        // entity found, so let's validate
        if (entityIndex > -1)
        {
            Set <ConstraintViolation<Object>> constraintViolations = validator.validate(invocation.getParameters()[entityIndex]);
            if (!constraintViolations.isEmpty())
            {
                ValidationErrors errors = new ValidationErrors();
                for (ConstraintViolation<Object> violation : constraintViolations)
                {
                    ValidationError error = new ValidationError();
                    error.setMessage(violation.getMessage());
                    error.setPath(violation.getPropertyPath().toString());
                    errors.addError(error);
                }
                invocation.getHttpContext().getResponse().setResponse(Response.status(400).entity(errors).build());
                return;
            }
        }

        invocation.invoke();
    }
}
