package com.atlassian.jira.rest.v2.issue.customfield;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.AbstractCustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.rest.api.field.FieldBean;
import com.atlassian.jira.rest.api.v2.customfield.CustomFieldMarshaller;
import com.atlassian.jira.rest.v2.issue.ResourceUriBuilder;
import com.atlassian.jira.rest.v2.issue.VelocityRequestContextFactories;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.rest.v2.issue.project.ProjectBeanFactory;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

/**
 * This class implements custom field marshalling.
 *
 * @since v4.2
 */
class MarshallerServiceImpl
{
    /**
     * This unmodifiable set contains the Class objects for all Java primitive types.
     */
    private static final Set<Class> PRIMITIVES = Collections.unmodifiableSet(new HashSet<Class>(Arrays.asList(
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            Character.class,
            String.class,
            Boolean.class)
    ));

    /**
     * Logger for this instance.
     */
    private final Logger log = LoggerFactory.getLogger(MarshallerServiceImpl.class);

    /**
     * The VelocityRequestContextFactory instance.
     */
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    /**
     * A ResourceUriBuilder instance.
     */
    private final ResourceUriBuilder resourceUriBuilder;

    /**
     * A ContextUriInfo.
     */
    private final ContextUriInfo contextUriInfo;
    
    private final VersionBeanFactory versionBeanFactory;
    private final ProjectBeanFactory projectBeanFactory;

    public MarshallerServiceImpl(VelocityRequestContextFactory velocityRequestContextFactory,
            ResourceUriBuilder resourceUriBuilder, ContextUriInfo contextUriInfo, VersionBeanFactory versionBeanFactory,
            ProjectBeanFactory projectBeanFactory)
    {
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.resourceUriBuilder = resourceUriBuilder;
        this.contextUriInfo = contextUriInfo;
        this.versionBeanFactory = versionBeanFactory;
        this.projectBeanFactory = projectBeanFactory;
    }

    @SuppressWarnings ("unchecked")
    public FieldBean marshall(CustomField customField, Issue issue, Map<String, CustomFieldMarshaller> marshallers)
    {
        final CustomFieldType customFieldType = customField.getCustomFieldType();
        final String cfTypeName = customFieldType.getKey();
        final Object transportObject = customFieldType.getValueFromIssue(customField, issue);

        // holds the marshalled object
        Object marshalled;
        try
        {
            // CASE 1: the CustomFieldType implements its own marshalling
            if (customFieldType instanceof CustomFieldMarshaller)
            {
                // convert to something that can be marshalled by JAXB
                marshalled = ((CustomFieldMarshaller) customFieldType).marshall(customField, transportObject);
            }
            // CASE 2: there is a custom field marshaller defined
            else if (marshallers.containsKey(cfTypeName))
            {
                marshalled = marshallers.get(cfTypeName).marshall(customField, transportObject);
            }
            // CASE 3: it's a built-in, or a subclass thereof
            else if (customFieldType instanceof AbstractCustomFieldType)
            {
                // we probably know how to marshall this
                ServiceOutcome<Object> outcome = marshallBuiltins((AbstractCustomFieldType) customFieldType, customField, issue);

                // if not, try to handle it as a primitive
                marshalled = outcome.isValid() ? outcome.getReturnedValue() : primitivesOrNull(transportObject);
            }
            // CASE 4: the transport may be a primitive Java type
            else
            {
                marshalled = primitivesOrNull(transportObject);
            }
        }
        catch (RuntimeException e)
        {
            log.error(format("Error calling marshaller for custom field '%s'", cfTypeName), e);
            marshalled = null;
        }

        return new FieldBean(customField.getName(), cfTypeName, marshalled);
    }

    /**
     * Marshalls any built-in custom field or subclass thereof by using the visitor.
     *
     * @param customFieldType an AbstractCustomFieldType instance
     * @param customField a CustomField instance
     * @param issue an Issue
     * @return a CustomFieldBean
     */
    @SuppressWarnings ("unchecked")
    public ServiceOutcome<Object> marshallBuiltins(AbstractCustomFieldType customFieldType, CustomField customField, Issue issue)
    {
        URI baseURI = VelocityRequestContextFactories.getBaseURI(velocityRequestContextFactory);
        return (ServiceOutcome<Object>) customFieldType.accept(new BuiltinCustomFieldMarshaller(issue, customField, baseURI, contextUriInfo, resourceUriBuilder, projectBeanFactory, versionBeanFactory));
    }

    /**
     * If the given object is a primitive type, or a Collection of primitive types, or null, then it is returned as-is.
     * Otherwise this method returns null;
     *
     * @param object an Object
     * @return the input Object, or null
     */
    protected Object primitivesOrNull(Object object)
    {
        // primitives are safe to return
        if (isPrimitive(object))
        {
            return object;
        }

        // collection of primitives are also OK
        if (Collection.class.isAssignableFrom(object.getClass()))
        {
            for (Object o : ((Collection) object))
            {
                if (!isPrimitive(o))
                {
                    return null;
                }
            }

            return object;
        }

        // anything else is not...
        return null;
    }

    /**
     * Returns true if the given object reference points to a basic Java type (String, Number, etc) or is null.
     *
     * @param object an Object
     * @return true iff the given object reference points to a basic Java type or null
     */
    private boolean isPrimitive(Object object)
    {
        return object == null || PRIMITIVES.contains(object.getClass());
    }
}
