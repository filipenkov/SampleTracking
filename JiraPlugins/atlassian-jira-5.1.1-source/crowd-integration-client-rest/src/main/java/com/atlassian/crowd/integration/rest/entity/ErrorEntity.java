package com.atlassian.crowd.integration.rest.entity;

import com.atlassian.crowd.event.EventTokenExpiredException;
import com.atlassian.crowd.event.IncrementalSynchronisationNotAvailableException;
import com.atlassian.crowd.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidEmailAddressException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidTokenException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.exception.NestedGroupsNotSupportedException;
import com.atlassian.crowd.exception.UserNotFoundException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents an error. All errors returned from REST resources should have the error entity in the response.
 *
 * @since v2.1
 */
@XmlRootElement (name = "error")
@XmlAccessorType (XmlAccessType.FIELD)
public class ErrorEntity
{
    // previously the enum constants were annotated with @XmlEnumValue, however this proved problematic because when
    // serialising to JSON, the Jackson JSON serialiser ignored the JAXB @XmlEnumValue value and used Enum#name.
    @XmlEnum
    public enum ErrorReason
    {
        APPLICATION_ACCESS_DENIED,
        APPLICATION_PERMISSION_DENIED,
        EXPIRED_CREDENTIAL,
        GROUP_NOT_FOUND,
        ILLEGAL_ARGUMENT,
        INACTIVE_ACCOUNT,
        INVALID_USER_AUTHENTICATION,
        INVALID_CREDENTIAL,
        INVALID_EMAIL,
        INVALID_GROUP,
        INVALID_SSO_TOKEN,
        INVALID_USER,
        MEMBERSHIP_NOT_FOUND,
        NESTED_GROUPS_NOT_SUPPORTED,
        APPLICATION_NOT_FOUND,
        UNSUPPORTED_OPERATION,
        USER_NOT_FOUND,
        OPERATION_FAILED,
        EVENT_TOKEN_EXPIRED,
        INCREMENTAL_SYNC_NOT_AVAILABLE;

        public static ErrorReason of(Exception e)
        {
            if (e instanceof ApplicationAccessDeniedException)
            {
                return ErrorReason.APPLICATION_ACCESS_DENIED;
            }
            else if (e instanceof ApplicationNotFoundException)
            {
                return ErrorReason.APPLICATION_NOT_FOUND;
            }
            else if (e instanceof ApplicationPermissionException)
            {
                return ErrorReason.APPLICATION_PERMISSION_DENIED;
            }
            else if (e instanceof ExpiredCredentialException)
            {
                return ErrorReason.EXPIRED_CREDENTIAL;
            }
            else if (e instanceof GroupNotFoundException)
            {
                return ErrorReason.GROUP_NOT_FOUND;
            }
            else if (e instanceof IllegalArgumentException)
            {
                return ErrorReason.ILLEGAL_ARGUMENT;
            }
            else if (e instanceof InactiveAccountException)
            {
                return ErrorReason.INACTIVE_ACCOUNT;
            }
            else if (e instanceof InvalidAuthenticationException)
            {
                return ErrorReason.INVALID_USER_AUTHENTICATION;
            }
            else if (e instanceof InvalidCredentialException)
            {
                return ErrorReason.INVALID_CREDENTIAL;
            }
            else if (e instanceof InvalidEmailAddressException)
            {
                return ErrorReason.INVALID_EMAIL;
            }
            else if (e instanceof InvalidGroupException)
            {
                return ErrorReason.INVALID_GROUP;
            }
            else if (e instanceof InvalidTokenException)
            {
                return ErrorReason.INVALID_SSO_TOKEN;
            }
            else if (e instanceof InvalidUserException)
            {
                return ErrorReason.INVALID_USER;
            }
            else if (e instanceof MembershipNotFoundException)
            {
                return ErrorReason.MEMBERSHIP_NOT_FOUND;
            }
            else if (e instanceof NestedGroupsNotSupportedException)
            {
                return ErrorReason.NESTED_GROUPS_NOT_SUPPORTED;
            }
            else if (e instanceof UnsupportedOperationException)
            {
                return ErrorReason.UNSUPPORTED_OPERATION;
            }
            else if (e instanceof UserNotFoundException)
            {
                return ErrorReason.USER_NOT_FOUND;
            }
            else if (e instanceof EventTokenExpiredException)
            {
                return ErrorReason.EVENT_TOKEN_EXPIRED;
            }
            else if (e instanceof IncrementalSynchronisationNotAvailableException)
            {
                return ErrorReason.INCREMENTAL_SYNC_NOT_AVAILABLE;
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
    private ErrorEntity()
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
    public ErrorEntity(final ErrorReason reason, final String message)
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
