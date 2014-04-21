package com.atlassian.plugins.rest.module.jersey;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.nio.charset.Charset;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.atlassian.plugins.rest.module.ResourceConfigManager;
import com.sun.jersey.core.impl.provider.entity.XMLRootElementProvider.App;
import com.sun.jersey.core.spi.factory.MessageBodyFactory;

@RunWith(value=MockitoJUnitRunner.class)
public class JerseyEntityHandlerTest {

	@Mock MessageBodyFactory messageBodyFactory;
	@Mock ResourceConfigManager resourceConfigManager;
	@Mock Providers providers;
	@Mock ContextResolver resolver;
	
	/**
	 * @see REST-172
	 * @throws Exception
	 */
	@Test
	public void testMarshall() throws Exception
	{
		MessageBodyWriter writer = new App(providers);
		
		when(messageBodyFactory.getMessageBodyWriter(eq(AnEntity.class), eq(AnEntity.class),
				(Annotation[])anyObject(), eq(MediaType.APPLICATION_XML_TYPE)))
			.thenReturn(writer);
		
		JerseyEntityHandler handler = new JerseyEntityHandler(messageBodyFactory, resourceConfigManager);

		final String text = "Some \n\ntext";
		
		AnEntity entity = new AnEntity();
		entity.setText(text);
		
		String marshalledEntity = handler.marshall(entity, MediaType.APPLICATION_XML_TYPE, Charset.forName("UTF-8"));
		
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><anEntity><text>"
				+text+"</text></anEntity>", 
				marshalledEntity);
	}
	
	
	@XmlRootElement
	public static class AnEntity
	{
	    @NotNull
	    private String text;

	    public String getText()
	    {
	        return text;
	    }

	    public void setText(String text)
	    {
	        this.text = text;
	    }
	}
}
