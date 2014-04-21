package com.atlassian.applinks.core.rest.model;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlRootElement;

import junit.framework.Assert;

import org.junit.Test;

import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;

/**
 * This class demonstrates that exchanging versions of ManifestEntity which
 * have more or less fields is supported. This happens when an AppLink is
 * created between two products which have a different version of the Applinks
 * plugin.
 * 
 * This test assumes JAXB is the underlying implementation of
 * AppLinksManifestDownloader.requestFactory. It doesn't test the whole chain of
 * exchange.
 * 
 * @since 3.4
 *
 */
public class ManifestDiscrepanciesTest
{
    @Test
    public void testMissingField() throws Exception
    {
        // Marshalls a manifest that has 1 field
        ManifestExampleOneField sentManifest = new ManifestExampleOneField("value1");
        JAXBContext marshallContext = JAXBContext.newInstance(ManifestExampleOneField.class);
        
        StringWriter writer = new StringWriter();
        marshallContext.createMarshaller().marshal(sentManifest, writer);
        
        // Tries to unmarshall it as a 2-field manifest
        JAXBContext unmarshallContext = JAXBContext.newInstance(ManifestExampleTwoFields.class);
        StringReader reader = new StringReader(writer.toString());
        
        ManifestExampleTwoFields receivedManifest = (ManifestExampleTwoFields) unmarshallContext.createUnmarshaller().unmarshal(reader);
        
        // Check the value is the same
        Assert.assertEquals("Manifest's values", sentManifest.getField1(), receivedManifest.getField1());   
        Assert.assertEquals("Manifest's default values", false, receivedManifest.getField2());        
    }

    @Test
    public void testTooManyFields() throws Exception
    {
        // Marshalls a 2-fields manifest
        ManifestExampleTwoFields sentManifest = new ManifestExampleTwoFields("value1", true);
        JAXBContext marshallContext = JAXBContext.newInstance(ManifestExampleTwoFields.class);
        
        StringWriter writer = new StringWriter();
        marshallContext.createMarshaller().marshal(sentManifest, writer);        
        
        // Tries to unmarshall it as a 1-field manifest
        JAXBContext unmarshallContext = JAXBContext.newInstance(ManifestExampleOneField.class);
        StringReader reader = new StringReader(writer.toString());
        
        ManifestExampleOneField receivedManifest = (ManifestExampleOneField) unmarshallContext.createUnmarshaller().unmarshal(reader);    

        // Check the value is the same
        Assert.assertEquals("Manifest's values", sentManifest.getField1(), receivedManifest.getField1());
    }
    
    

    /* The 2 following classes are used by the tests to marshall/unmarshall and see whether
     * a missing field/too many fields affect the operation
     */
    @XmlRootElement(name = "manifest")
    static class ManifestExampleOneField {
        private String field1;
        public ManifestExampleOneField(String field1)
        {
            super();
            this.field1 = field1;
        }
        @SuppressWarnings("unused")
        private ManifestExampleOneField() {
            // Empty constructor used by Jaxb when unmarshalling
        }

        public String getField1()
        {
            return field1;
        }
    }
    
    @XmlRootElement(name = "manifest")
    static class ManifestExampleTwoFields {
        private String field1;
        private boolean field2;
        public ManifestExampleTwoFields(String field1, boolean field2)
        {
            super();
            this.field1 = field1;
            this.field2 = field2;
        }
        @SuppressWarnings("unused")
        private ManifestExampleTwoFields() {
            // Empty constructor used by Jaxb when unmarshalling
        }

        public String getField1()
        {
            return field1;
        }
        public boolean getField2()
        {
            return field2;
        }
    }
}
