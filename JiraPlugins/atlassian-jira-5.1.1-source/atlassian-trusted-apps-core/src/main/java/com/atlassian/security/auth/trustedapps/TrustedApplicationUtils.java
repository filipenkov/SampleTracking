package com.atlassian.security.auth.trustedapps;

import com.atlassian.security.auth.trustedapps.TrustedApplicationUtils.Header.Response;
import com.atlassian.security.auth.trustedapps.request.TrustedRequest;

/**
 * Utility class for trusted applications
 */
public class TrustedApplicationUtils
{
    /**
     * Used in Request/Response Header values for validating the capabilites of the client/server.
     * 
     * @since 0.35
     */
    public static final class Constant
    {
        /**
         * The protocol version. The first version of this protocol did not contain this header and so was verion#0.
         */
        public static final Integer VERSION = new Integer(1);
        
        public static final Integer VERSION_TWO = Integer.valueOf(2);

        /**
         * Magic number used to validate successful decryption.
         */
        public static final String MAGIC = String.valueOf(0xBADC0FEE);

        /**
         * Default charset used for encoding/decoding Strings.
         */
        public static final String CHARSET_NAME = "utf-8";

        public static final String CERTIFICATE_URL_PATH = "/admin/appTrustCertificate";
        
        private Constant()
        {
        }
    }

    /**
     * Request/Response header parameters
     * 
     * @since 0.35
     */
    public static final class Header
    {
        private static final String PREFIX = "X-Seraph-Trusted-App-";

        public static final class Request
        {
            /**
             * Header name for trusted application ID
             */
            public static final String ID = PREFIX + "ID";

            /**
             * Header name for the secret key, used to encrypt the certificate.
             */
            public static final String SECRET_KEY = PREFIX + "Key";

            /**
             * Header name for trusted application certificate
             */
            public static final String CERTIFICATE = PREFIX + "Cert";

            /**
             * Header name for trusted application protocol version
             */
            public static final String VERSION = PREFIX + "Version";

            /**
             * Header name for magic number for decryption validation
             */
            public static final String MAGIC = PREFIX + "Magic";

            public static final String SIGNATURE = PREFIX + "Signature";

            private Request()
            {
            }
        }

        public static final class Response
        {
            /**
             * Header that will contain trusted application error message if it fails
             */
            public static final String ERROR = PREFIX + "Error";

            /**
             * Header used to indicate the status of a response to a trusted app request
             */
            public static final String STATUS = PREFIX + "Status";

            private Response()
            {
            }
        }

        private Header()
        {
        }
    }

    /**
     * Add request parameters to the trusted request. Values are extracted from the given certificate.
     * 
     * @param certificate
     *            the encrypted certificate to retrieve values from
     * @param request
     *            the request to populate
     */
    public static void addRequestParameters(final EncryptedCertificate certificate, final TrustedRequest request)
    {
        request.addRequestParameter(Header.Request.ID, certificate.getID());
        request.addRequestParameter(Header.Request.CERTIFICATE, certificate.getCertificate());
        request.addRequestParameter(Header.Request.SECRET_KEY, certificate.getSecretKey());
        
        Integer version = certificate.getProtocolVersion();
        if (version != null)
        {
            request.addRequestParameter(Header.Request.VERSION, version.toString());
        }
        
        request.addRequestParameter(Header.Request.MAGIC, certificate.getMagicNumber());
        if (certificate.getSignature() != null)
        {
            request.addRequestParameter(Header.Request.SIGNATURE, certificate.getSignature());
        }
    }
    
    /**
     * Get a {@link TransportErrorMessage} from the {@link Response#ERROR} header. This contains an error code that can
     * be used for i18n purposes as well the parameters. You can also get a default formatted error message.
     * 
     * @param errorMessage the String containing the error message. Must 
     * @return
     */
    public static TransportErrorMessage parseError(String errorMessage)
    {
        return TransportErrorMessage.PARSER.parse(errorMessage);
    }

    public static void validateMagicNumber(String msg, String appId, Integer protocolVersion, String magicNumber) throws InvalidCertificateException
    {
        // if empty don't worry
        if ((protocolVersion != null) && !TrustedApplicationUtils.Constant.MAGIC.equals(magicNumber))
        {
            throw new InvalidCertificateException(new TransportErrorMessage.BadMagicNumber(msg, appId));
        }
    }
}