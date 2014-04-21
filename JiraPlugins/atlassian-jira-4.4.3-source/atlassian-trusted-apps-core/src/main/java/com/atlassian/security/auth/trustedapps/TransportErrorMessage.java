package com.atlassian.security.auth.trustedapps;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * TransportErrorMessages are reported when a client makes a TrustedApplication request.
 * <p>
 * The String format of these is important. They basically consist of three elements:
 * <ol>
 * <li>Error Code (one of the {@link Code} constants)
 * <li>Message (a MessageFormat formatted message)
 * <li>Parameters (encoded as a JSON array)
 * </ol>
 * These are separated by a semi-colon tab.
 * <p>
 * To parse a String into a {@link TransportErrorMessage} use a {@link Parser}. If the String is not in the correct
 * format, exceptions will be thrown. The format to the {@link #toString()} method and the format the {@link Parser}
 * accepts must be symmetric and constant.
 * 
 * @TODO replace inner classes with enum in JDK5
 */
public class TransportErrorMessage
{
    static final Parser PARSER = new Parser();

    // --------------------------------------------------------------------------------------------------------- members

    private final Code code;
    private final String message;
    private final String[] params;

    // ----------------------------------------------------------------------------------------------------------- ctors

    TransportErrorMessage(final Code code, final String message, final String[] params)
    {
        Null.not("code", code);
        Null.not("params", params);
        Null.not("message", message);
        for (int i = 0; i < params.length; i++)
        {
            Null.not(String.valueOf(i), params[i]);
        }

        this.code = code;
        this.message = message;
        this.params = params;
    }

    TransportErrorMessage(final Code code, final String message)
    {
        this(code, message, new String[0]);
    }

    TransportErrorMessage(final Code code, final String message, final String param)
    {
        this(code, message, new String[] { param });
    }

    TransportErrorMessage(final Code code, final String message, final String one, final String two)
    {
        this(code, message, new String[] { one, two });
    }

    TransportErrorMessage(final Code code, final String message, final String one, final String two, final String three)
    {
        this(code, message, new String[] { one, two, three });
    }

    // ------------------------------------------------------------------------------------------------------- accessors

    public Code getCode()
    {
        return code;
    }

    public String[] getParameters()
    {
        return params.clone();
    }

    public String getFormattedMessage()
    {
        return MessageFormat.format(message, (Object[]) params);
    }

    /**
     * String representation of a {@link TransportErrorMessage}. Of the form:
     * 
     * <pre>
     * code {@link Parser#SEPARATOR} message {@link Parser#SEPARATOR} params
     * </pre>
     * 
     * where the params are encoded as a JSON array.
     * <p>
     * Note: The String representation of a {@link TransportErrorMessage} is sent across the wire as an error to the
     * client. Therefore this format is static and must not change.
     * 
     * @see Parser#parse(String)
     */
    @Override
    public String toString()
    {
        return PARSER.toString(this);
    }

    // ------------------------------------------------------------------------------------------------- specializations

    static class System extends TransportErrorMessage
    {
        System(final Throwable cause, final String appId)
        {
            super(Code.SYSTEM, "Exception: {0} occurred serving request for application: {1}", cause.toString(), appId);
        }
    }

    static class BadMagicNumber extends TransportErrorMessage
    {
        public BadMagicNumber(final String keyName, final String appId)
        {
            super(Code.BAD_MAGIC, "Unable to decrypt certificate {0} for application {1}", keyName, appId);
        }
    }

    public static class BadProtocolVersion extends TransportErrorMessage
    {
        public BadProtocolVersion(final String versionString)
        {
            super(Code.BAD_PROTOCOL_VERSION, "Bad protocol version: {0}", versionString);
        }
    }

    /**
     * AppId not found in request
     */
    public static class ApplicationIdNotFoundInRequest extends TransportErrorMessage
    {
        public ApplicationIdNotFoundInRequest()
        {
            super(Code.APP_ID_NOT_FOUND, "Application ID not found in request");
        }
    }

    public static class SecretKeyNotFoundInRequest extends TransportErrorMessage
    {
        public SecretKeyNotFoundInRequest()
        {
            super(Code.SECRET_KEY_NOT_FOUND, "Secret Key not found in request");
        }
    }

    public static class MagicNumberNotFoundInRequest extends TransportErrorMessage
    {
        public MagicNumberNotFoundInRequest()
        {
            super(Code.MAGIC_NUMBER_NOT_FOUND, "Magic Number not found in request");
        }
    }

    public static class ApplicationUnknown extends TransportErrorMessage
    {
        public ApplicationUnknown(final String appId)
        {
            super(Code.APP_UNKNOWN, "Unknown Application: {0}", appId);
        }
    }

    public static class UserUnknown extends TransportErrorMessage
    {
        public UserUnknown(final String userName)
        {
            super(Code.USER_UNKNOWN, "Unknown User: {0}", userName);
        }
    }

    public static class PermissionDenied extends TransportErrorMessage
    {
        public PermissionDenied()
        {
            super(Code.PERMISSION_DENIED, "Permission Denied");
        }
    }
    
    public static class BadSignature extends TransportErrorMessage
    {
        public BadSignature(String url)
        {
            super(Code.BAD_SIGNATURE, "Bad signature for URL: {0}", url);
        }
        
        public BadSignature()
        {
            super(Code.BAD_SIGNATURE, "Missing signature in a v2 request");
        }
    }

    // --------------------------------------------------------------------------------------------------- inner classes

    /**
     * Used to convert an error message String from the server at the client to a TransportErrorMessage.
     * <p>
     * String format is:
     * 
     * <pre>
     * code SEPARATOR message SEPARATOR args
     * </pre>
     * 
     * args are JSON array formatted.
     * 
     * @see TransportErrorMessage#toString()
     */
    static class Parser
    {
        static final String SEPARATOR = ";\t";

        TransportErrorMessage parse(final String inputItring) throws IllegalArgumentException
        {
            Null.not("inputString", inputItring);
            final String[] args = inputItring.split(SEPARATOR);
            if (args.length != 3)
            {
                throw new IllegalArgumentException("Cannot split message into Code, Message, Parameters:" + inputItring);
            }
            final Code code = Code.get(args[0]);
            final String[] params = StringUtil.split(args[2]);
            return new TransportErrorMessage(code, args[1], params);
        }

        /**
         * Format a String representation of a {@link TransportErrorMessage} in such a way as the {@link #parse(String)}
         * method can parse it.
         * 
         * @param msg
         *            the message to turn into a String
         * @return the String representation of the message
         */
        String toString(final TransportErrorMessage msg)
        {
            return new StringBuffer(msg.code.getCode()).append(Parser.SEPARATOR).append(msg.message).append(Parser.SEPARATOR).append(
                StringUtil.toString(msg.params)).toString();
        }
    }

    /**
     * Typesafe enum that contains all known error codes.
     * <p>
     * Note: for backwards compatibility, do not ever remove a code once its been released. Deprecate if necessary, but
     * not remove.
     */
    public static final class Code
    {
        private static final Map<String, Code> ALL = new HashMap<String, Code>();

        public static final Code UNKNOWN = new Code(Severity.ERROR, "UNKNOWN");

        public static final Code APP_UNKNOWN = new Code(Severity.ERROR, "APP_UNKNOWN");
        public static final Code SYSTEM = new Code(Severity.ERROR, "SYSTEM");
        public static final Code BAD_PROTOCOL_VERSION = new Code(Severity.ERROR, "BAD_PROTOCOL_VERSION");
        public static final Code APP_ID_NOT_FOUND = new Code(Severity.ERROR, "APP_ID_NOT_FOUND");
        public static final Code SECRET_KEY_NOT_FOUND = new Code(Severity.ERROR, "SECRET_KEY_NOT_FOUND");
        public static final Code MAGIC_NUMBER_NOT_FOUND = new Code(Severity.ERROR, "MAGIC_NUMBER_NOT_FOUND");

        public static final Code BAD_REMOTE_IP = new Code(Severity.FAIL, "BAD_REMOTE_IP");
        public static final Code BAD_XFORWARD_IP = new Code(Severity.FAIL, "BAD_XFORWARD_IP");
        public static final Code BAD_URL = new Code(Severity.FAIL, "BAD_URL");
        public static final Code OLD_CERT = new Code(Severity.FAIL, "OLD_CERT");
        public static final Code MISSING_CERT = new Code(Severity.FAIL, "MISSING_CERT");
        public static final Code BAD_MAGIC = new Code(Severity.FAIL, "BAD_MAGIC");
        public static final Code USER_UNKNOWN = new Code(Severity.ERROR, "USER_UNKNOWN");
        public static final Code PERMISSION_DENIED = new Code(Severity.ERROR, "PERMISSION_DENIED");
        public static final Code BAD_SIGNATURE = new Code(Severity.FAIL, "BAD_SIGNATURE");

        static Code get(final String code)
        {
            final Code result = ALL.get(code);
            return (result == null) ? Code.UNKNOWN : result;
        }

        private final Severity severity;
        private final String code;

        private Code(final Severity severity, final String code)
        {
            Null.not("severity", severity);
            Null.not("code", code);
            this.severity = severity;
            this.code = code;
            if (ALL.containsKey(code))
            {
                throw new IllegalArgumentException(code + " is already mapped as a " + this.getClass().getName());
            }
            ALL.put(code, this);
        }

        public Severity getSeverity()
        {
            return severity;
        }

        public String getCode()
        {
            return code;
        }

        public static final class Severity
        {
            static final Severity ERROR = new Severity("ERROR");
            static final Severity FAIL = new Severity("FAIL");

            private final String name;

            private Severity(final String name)
            {
                this.name = name;
            }

            @Override
            public String toString()
            {
                return name;
            }
        }
    }
}