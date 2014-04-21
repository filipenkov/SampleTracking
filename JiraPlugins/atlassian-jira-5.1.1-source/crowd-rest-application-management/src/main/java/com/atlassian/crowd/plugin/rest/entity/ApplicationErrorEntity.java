package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.manager.application.ApplicationManagerException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents an error. All errors returned from REST resources should have the error entity in the response.
 *
 * @since 2.2
 */
@XmlRootElement (name = "error")
@XmlAccessorType (XmlAccessType.FIELD)
public class ApplicationErrorEntity
{
    // previously the enum constants were annotated with @XmlEnumValue, however this proved problematic because when
    // serialising to JSON, the Jackson JSON serialiser ignored the JAXB @XmlEnumValue value and used Enum#name.
    @XmlEnum
    public enum ErrorReason
    {
        APPLICATION_NOT_FOUND,
        APPLICATION_MODIFICATION_FAILED,
        DIRECTORY_NOT_FOUND,
        ILLEGAL_ARGUMENT,
        INVALID_CREDENTIAL,
        UNSUPPORTED_OPERATION,
        OPERATION_FAILED;

        public static ErrorReason of(Exception e)
        {
            if (e instanceof ApplicationNotFoundException)
            {
                return ErrorReason.APPLICATION_NOT_FOUND;
            }
            else if (e instanceof ApplicationManagerException)
            {
                return ErrorReason.APPLICATION_MODIFICATION_FAILED;
            }
            else if (e instanceof DirectoryNotFoundException)
            {
                return ErrorReason.DIRECTORY_NOT_FOUND;
            }
            else if (e instanceof IllegalArgumentException)
            {
                return ErrorReason.ILLEGAL_ARGUMENT;
            }
            else if (e instanceof InvalidCredentialException)
            {
                return ErrorReason.INVALID_CREDENTIAL;
            }
            else if (e instanceof UnsupportedOperationException)
            {
                return ErrorReason.UNSUPPORTED_OPERATION;
            }
            else
            {
                return ErrorReason.OPERATION_FAILED;
            }
        }
    }

    @XmlElement (name = "reason")
    private final ErrorReason reason;

    @XmlElement (name = "message")
    private final String message;

    /**
     * JAXB requires a no-arg constructor.
     */
    private ApplicationErrorEntity()
    {
        reason = null;
        message = null;
    }

    /**
     * Constructs an error entity.
     *
     * @param reason reason for the error.
     * @param message message
     */
    public ApplicationErrorEntity(final ErrorReason reason, final String message)
    {
        this.reason = reason;
        this.message = message;
    }

    public ErrorReason getReason()
    {
        return reason;
    }

    public String getMessage()
    {
        return message;
    }
}
