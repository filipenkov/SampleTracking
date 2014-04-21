package com.atlassian.security.auth.trustedapps.filter;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import com.atlassian.security.auth.trustedapps.BouncyCastleEncryptionProvider;

import org.bouncycastle.util.encoders.Base64;

/**
 * Verifies signatures of URLs.
 * 
 * @since 2.4
 */
class RequestSignatureTool
{
    public static final String ALGORITHM = "SHA1withRSA";
    private static final String UTF8 = "utf-8";
    
    private Signature getSigImpl() throws NoSuchAlgorithmException
    {
        return Signature.getInstance(ALGORITHM, BouncyCastleEncryptionProvider.PROVIDER);
    }
    
    public boolean verify(long timestamp, String requestUrl, PublicKey key, String signature)
        throws UnableToVerifySignatureException
    {
        try
        {
            String signatureMaterial = Long.toString(timestamp) + '\n' + requestUrl;
            
            Signature sig = getSigImpl();
            
            sig.initVerify(key);
            sig.update(signatureMaterial.getBytes(UTF8));
            return sig.verify(Base64.decode(signature));
        }
        catch (InvalidKeyException e)
        {
            throw new UnableToVerifySignatureException(e);
        }
        catch (SignatureException e)
        {
            throw new UnableToVerifySignatureException(e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new UnableToVerifySignatureException(e);
        }
        catch (StringIndexOutOfBoundsException e)
        {
            throw new UnableToVerifySignatureException(e);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new UnableToVerifySignatureException(e);
        }
    }

    class UnableToVerifySignatureException extends Exception
    {
        public UnableToVerifySignatureException(Exception cause)
        {
            super(cause);
        }
    }
}
