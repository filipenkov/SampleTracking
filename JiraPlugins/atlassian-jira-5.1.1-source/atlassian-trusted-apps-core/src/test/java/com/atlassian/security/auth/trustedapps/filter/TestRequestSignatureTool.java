package com.atlassian.security.auth.trustedapps.filter;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import com.atlassian.security.auth.trustedapps.filter.RequestSignatureTool.UnableToVerifySignatureException;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class TestRequestSignatureTool
{
    private RequestSignatureTool requestSignatureTool = new RequestSignatureTool();
    
    @Test
    public void failsWhenSignatureIsInvalid() throws Exception
    {
        String signature = "XXXX";
        
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        KeyPair kp = kpg.genKeyPair();
        
        assertFalse(requestSignatureTool.verify(0, "http://www.example.com/", kp.getPublic(), signature));
    }
    
    @Test(expected = UnableToVerifySignatureException.class)
    public void failsWhenSignatureIsInvalidBase64Encoded() throws Exception
    {
        String signature = "";
        
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        KeyPair kp = kpg.genKeyPair();
        
        requestSignatureTool.verify(0, "http://www.example.com/", kp.getPublic(), signature);
    }
}
