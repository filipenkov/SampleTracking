package com.atlassian.gadgets.oauth.serviceprovider.internal;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.gadgets.GadgetRequestContext;
import com.atlassian.gadgets.GadgetRequestContextFactory;
import com.atlassian.gadgets.spec.GadgetSpec;
import com.atlassian.gadgets.spec.GadgetSpecFactory;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.Consumer.SignatureMethod;
import com.atlassian.oauth.serviceprovider.ConsumerInformationRenderer;
import com.atlassian.oauth.serviceprovider.ServiceProviderToken;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.templaterenderer.TemplateRenderer;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OpenSocialConsumerInformationRendererTest
{
    private static final URI GADGET_SPEC_URI = URI.create("http://container/gadget.xml");
    private static final GadgetSpec GADGET_SPEC = GadgetSpec.gadgetSpec(GADGET_SPEC_URI).build();

    private static final KeyPair KEYS;
    static {
        try
        {
            KEYS = RSAKeys.generateKeyPair();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }
    private static final Principal USER = new Principal()
    {
        public String getName()
        {
            return "bob";
        }
    };

    private static final Consumer RSA_CONSUMER = Consumer.key("consumer-rsa").name("Consumer using RSA").description("description").signatureMethod(SignatureMethod.RSA_SHA1).publicKey(KEYS.getPublic()).callback(URI.create("http://consumer/callback")).build();
    private static final ServiceProviderToken TOKEN = ServiceProviderToken.newAccessToken("1234").tokenSecret("5678").consumer(RSA_CONSUMER).authorizedBy(USER).properties(ImmutableMap.of(OpenSocial.XOAUTH_APP_URL, "http://container/gadget.xml")).build();
    
    @Mock TemplateRenderer templateRenderer;
    @Mock GadgetSpecFactory gadgetSpecFactory;
    @Mock GadgetRequestContextFactory gadgetRequestContextFactory;
    
    ConsumerInformationRenderer renderer;
    
    @Mock HttpServletRequest request;
    
    @Before
    public void setUp()
    {
        renderer = new OpenSocialConsumerInformationRenderer(templateRenderer, gadgetSpecFactory, gadgetRequestContextFactory);
    }
    
    @Test
    public void verifyThatRenderFetchesGadgetSpecAndWritesToTemplateRenderer() throws Exception
    {
        when(gadgetRequestContextFactory.get(request)).thenReturn(GadgetRequestContext.NO_CURRENT_REQUEST);
        when(gadgetSpecFactory.getGadgetSpec(GADGET_SPEC_URI, GadgetRequestContext.NO_CURRENT_REQUEST)).thenReturn(GADGET_SPEC);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer writer = new PrintWriter(baos);
        renderer.render(TOKEN, request, writer);
        
        Map<String, Object> expectedContext = ImmutableMap.of(
            "consumer", TOKEN.getConsumer(),
            "gadgetSpec", GADGET_SPEC
        );
        
        verify(templateRenderer).render("opensocial-consumer-info.vm", expectedContext, writer);
    }
    
    @Test
    public void assertThatCanRenderReturnsTrueWhenTokenHasXOAuthAppUrlProperty()
    {
        assertTrue(renderer.canRender(TOKEN, request));
    }
    
    @Test
    public void assertThatHasAppUrlReturnsFalseWhenTokenDoesNotHaveXOAuthAppUrlProperty()
    {
        ServiceProviderToken token = ServiceProviderToken.newAccessToken("1234")
            .tokenSecret("5678")
            .consumer(RSA_CONSUMER)
            .authorizedBy(USER)
            .build();
        assertFalse(renderer.canRender(token, request));
    }
}
