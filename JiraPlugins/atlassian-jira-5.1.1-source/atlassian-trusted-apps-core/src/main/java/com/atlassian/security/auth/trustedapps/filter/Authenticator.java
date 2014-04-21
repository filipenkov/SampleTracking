package com.atlassian.security.auth.trustedapps.filter;

import com.atlassian.security.auth.trustedapps.TransportErrorMessage;
import com.atlassian.security.auth.trustedapps.Null;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

/**
 * Authenticate a TrustedApplication request
 */
public interface Authenticator
{
    Result authenticate(HttpServletRequest request, HttpServletResponse response);

    static class Result
    {
        private final Result.Status status;
        private final TransportErrorMessage message;
        private final Principal user;

        Result(Result.Status status)
        {
            this(status, null, null);
        }

        Result(Result.Status status, TransportErrorMessage message)
        {
            this(status, message, null);

            Null.not("message", message);
        }

        Result(Result.Status status, Principal principal)
        {
            this(status, null, principal);

            Null.not("principal", principal);
        }

        Result(Result.Status status, TransportErrorMessage message, Principal user)
        {
            if (status == null)
            {
                throw new IllegalArgumentException("status");
            }
            this.status = status;
            this.message = message;
            this.user = user;
        }

        public Result.Status getStatus()
        {
            return status;
        }

        public String getMessage()
        {
            return message.toString();
        }

        public Principal getUser()
        {
            return user;
        }

        static final class Status
        {
            /**
             * Necessary to declare these as int constants as javac is too dumb to realise that a final member of a
             * final class's static constants is a constant
             */
            static final class Constants
            {
                static final int SUCCESS = 0;
                static final int FAILED = 1;
                static final int ERROR = 2;
                static final int NO_ATTEMPT = 3;
            }

            static final Result.Status SUCCESS = new Result.Status(Result.Status.Constants.SUCCESS, "success");
            static final Result.Status FAILED = new Result.Status(Result.Status.Constants.FAILED, "failed");
            static final Result.Status ERROR = new Result.Status(Result.Status.Constants.ERROR, "error");
            static final Result.Status NO_ATTEMPT = new Result.Status(Result.Status.Constants.NO_ATTEMPT, "no attempt");

            private final int ordinal;
            private final String name;

            private Status(int ordinal, String name)
            {
                this.ordinal = ordinal;
                this.name = name;
            }

            int getOrdinal()
            {
                return ordinal;
            }

            public String toString()
            {
                return name;
            }
        }

        public static final class NoAttempt extends Result
        {
            NoAttempt()
            {
                super(Status.NO_ATTEMPT);
            }
        }

        public static final class Error extends Result
        {
            Error(TransportErrorMessage message)
            {
                super(Status.ERROR, message);
            }
        }

        public static final class Failure extends Result
        {
            Failure(TransportErrorMessage message)
            {
                super(Status.FAILED, message);
            }
        }

        public static final class Success extends Result
        {
            private final String signedRequestUrl;
            
            public Success(Principal principal)
            {
                super(Status.SUCCESS, principal);
                this.signedRequestUrl = null;
            }
            
            public Success(Principal principal, String signedRequestUrl)
            {
                super(Status.SUCCESS, principal);
                this.signedRequestUrl = signedRequestUrl;
            }
            
            public String getSignedUrl()
            {
                return signedRequestUrl;
            }
        }
    }
}
