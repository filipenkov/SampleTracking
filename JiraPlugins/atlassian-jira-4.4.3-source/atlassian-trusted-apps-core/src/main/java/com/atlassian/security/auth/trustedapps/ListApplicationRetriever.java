package com.atlassian.security.auth.trustedapps;

import com.atlassian.security.auth.trustedapps.Transcoder.Base64Transcoder;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

public class ListApplicationRetriever implements ApplicationRetriever
{
    private final List<String> values;
    private final EncryptionProvider encryptionProvider;
    private final Transcoder transcoder = new Base64Transcoder();

    ListApplicationRetriever(final EncryptionProvider encryptionProvider, final List<String> values)
    {
        Null.not("encryptionProvider", encryptionProvider);
        Null.not("values", values);
        int i = 0;
        for (final Object element : values)
        {
            Null.not("value: " + i++, element);
        }

        this.encryptionProvider = encryptionProvider;
        this.values = new ArrayList<String>(values);
    }

    public Application getApplication() throws RetrievalException
    {
        if (values.size() < 2)
        {
            final TransportErrorMessage error =
                    new TransportErrorMessage(TransportErrorMessage.Code.MISSING_CERT,
                            "\"Application Certificate too small.\" Values found: [" + values.size() + "] ." + values);
            final Exception cause = new TransportException(error) {};
            throw new ApplicationNotFoundException(cause);
        }
        if (values.size() == 2)
        {
            return getApplicationProtocolV0();
        }
        return getApplicationProtocolV1();
    }

    private Application getApplicationProtocolV1() throws RetrievalException
    {
        // decorate the protocol zero version
        final Application result = getApplicationProtocolV0();
        // with some validation of the certificate
        final String protocol = values.get(2);
        final String magic = values.get(3);
        try
        {
            final Integer protocolVersion = isBlank(protocol) ? null : Integer.valueOf(protocol);
            try
            {
                TrustedApplicationUtils.validateMagicNumber("application details", result.getID(), protocolVersion, magic);
            }
            catch (final InvalidCertificateException e)
            {
                throw new InvalidApplicationDetailsException(e);
            }
        }
        catch (final NumberFormatException e)
        {
            throw new InvalidApplicationDetailsException(e);
        }
        return result;
    }

    private Application getApplicationProtocolV0() throws RetrievalException
    {
        try
        {
            final String id = values.get(0);
            final String keyStr = values.get(1);

            if (keyStr == null)
            {
                throw new ApplicationNotFoundException("Public Key not found");
            }

            final byte[] data = transcoder.decode(keyStr);
            final PublicKey key = encryptionProvider.toPublicKey(data);
            return new SimpleApplication(id, key);
        }
        catch (final InvalidKeySpecException e)
        {
            throw new RuntimeException(e);
        }
        catch (final NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
        catch (final NoSuchProviderException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static boolean isBlank(final String input)
    {
        return (input == null) || (input.trim().length() == 0);
    }
}