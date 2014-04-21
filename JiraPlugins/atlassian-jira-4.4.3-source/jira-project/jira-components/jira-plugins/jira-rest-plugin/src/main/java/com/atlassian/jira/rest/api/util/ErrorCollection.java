package com.atlassian.jira.rest.api.util;

import com.atlassian.jira.util.dbc.Assertions;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A JAXB representation of an {@link com.atlassian.jira.util.ErrorCollection} useful for returning via JSON or XML.
 *
 * @since v4.2
 */
@XmlRootElement
public class ErrorCollection
{
    /**
     * Returns a new builder. The generated builder is equivalent to the builder created by the {@link
     * com.atlassian.jira.rest.api.util.ErrorCollection.Builder#newBuilder()} method.
     *
     * @return a new Builder
     */
    public static Builder builder()
    {
        return Builder.newBuilder();
    }

    /**
     * Returns a new ErrorCollection containing a list of error messages.
     *
     * @param messages an array of Strings containing error messages
     * @return a new ErrorCollection
     */
    public static ErrorCollection of(String... messages)
    {
        return of(Arrays.asList(messages));
    }

    /**
     * Returns a new ErrorCollection containing a list of error messages.
     *
     * @param messages an Iterable of Strings containing error messages
     * @return a new ErrorCollection
     */
    public static ErrorCollection of(Iterable<String> messages)
    {
        Builder b = builder();
        for (String message : messages)
        {
            b.addErrorMessage(message);
        }

        return b.build();
    }

    /**
     * Returns a new ErrorCollection containing all the errors contained in the input error collection.
     *
     * @param errorCollection a com.atlassian.jira.util.ErrorCollection
     * @return a new ErrorCollection
     */
    public static ErrorCollection of(com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        return builder().addErrorCollection(errorCollection).build();
    }

    /**
     * Generic error messages
     */
    @XmlElement
    private Collection<String> errorMessages = new ArrayList<String>();

    @XmlElement
    private Map<String, String> errors = new HashMap<String, String>();

    /**
     * Builder used to create a new immutable error collection.
     */
    public static class Builder
    {
        private ErrorCollection errorCollection;

        public static Builder newBuilder()
        {
            return new Builder(Collections.<String>emptyList());
        }

        public static Builder newBuilder(ValidationError... errors)
        {
            Assertions.notNull("errors", errors);

            return new Builder(Collections.<String>emptyList());
        }

        public static Builder newBuilder(Set<String> errorMessages)
        {
            Assertions.notNull("errorMessages", errorMessages);

            return new Builder(errorMessages);
        }

        public static Builder newBuilder(Collection<ValidationError> errors)
        {
            Assertions.notNull("errors", errors);

            return new Builder(Collections.<String>emptyList());
        }

        public static Builder newBuilder(ErrorCollection errorCollection)
        {
            Assertions.notNull("errorCollection", errorCollection);

            return new Builder(errorCollection.getErrorMessages());
        }

        Builder(Collection<String> errorMessages)
        {
            this.errorCollection = new ErrorCollection(errorMessages);
        }

        public Builder addErrorCollection(com.atlassian.jira.util.ErrorCollection errorCollection)
        {
            Assertions.notNull("errorCollection", errorCollection);

            this.errorCollection.addErrorCollection(errorCollection);
            return this;
        }

        public Builder addErrorMessage(String errorMessage)
        {
            Assertions.notNull("errorMessage", errorMessage);

            this.errorCollection.addErrorMessage(errorMessage);
            return this;
        }

        public ErrorCollection build()
        {
            return this.errorCollection;
        }
    }

    @SuppressWarnings ( { "UnusedDeclaration", "unused" })
    private ErrorCollection()
    {}

    private ErrorCollection(Collection<String> errorMessages)
    {
        this.errorMessages.addAll(notNull("errorMessages", errorMessages));
    }

    @SuppressWarnings ("unchecked")
    private void addErrorCollection(com.atlassian.jira.util.ErrorCollection errorCollection)
    {
        errorMessages.addAll(notNull("errorCollection", errorCollection).getErrorMessages());
        errors.putAll(errorCollection.getErrors());
    }

    private void addErrorMessage(String errorMessage)
    {
        errorMessages.add(errorMessage);
    }

    public boolean hasAnyErrors()
    {
        return !errorMessages.isEmpty() && !errors.isEmpty();
    }

    public Collection<String> getErrorMessages()
    {
        return errorMessages;
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object o)
    {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}