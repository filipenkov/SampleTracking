package com.atlassian.applinks.core.rest.model;

import java.io.StringWriter;
import java.util.Collections;

import javax.xml.bind.JAXBContext;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LinkAndAuthProviderEntityTest
{
    @Test
    public void objectCanBeJaxbSerialisedWhenIncludedInList() throws Exception
    {
        ListEntity<LinkAndAuthProviderEntity> le =
            new ListEntity<LinkAndAuthProviderEntity>(Collections.singletonList(new LinkAndAuthProviderEntity()));
        
        JAXBContext context = JAXBContext.newInstance(ListEntity.class);
        
        StringWriter sw = new StringWriter();
        context.createMarshaller().marshal(le, sw);
        
        String s = sw.toString();
        
        // The real test is that it marshals without error; perform a sanity check as well
        assertTrue(s.contains("xsi:type=\"linkAndAuthProviderEntity\""));
    }
}
