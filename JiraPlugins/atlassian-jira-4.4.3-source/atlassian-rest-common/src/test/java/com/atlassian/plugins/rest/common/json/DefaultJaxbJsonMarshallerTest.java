package com.atlassian.plugins.rest.common.json;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class DefaultJaxbJsonMarshallerTest
{
    private JaxbJsonMarshaller marshaller;

    @Before
    public void setUp()
    {
        marshaller = new DefaultJaxbJsonMarshaller();
    }

    @Test
    public void testMarshalBoolean() throws JAXBException
    {
        assertEquals("true", marshaller.marshal(Boolean.TRUE));
    }

    @Test
    public void testMarshalInteger() throws JAXBException
    {
        assertEquals("2", marshaller.marshal(Integer.valueOf(2)));
    }

    @Test
    public void testMarshalString() throws JAXBException
    {
        assertEquals("\"foobar\"", marshaller.marshal("foobar"));
    }

    @Test
    public void testMarshalMap() throws JAXBException
    {
        assertEquals("{\"foo\":\"bar\"}", marshaller.marshal(Collections.singletonMap("foo", "bar")));
    }

    @Test
    public void testMarshalList() throws JAXBException
    {
        assertEquals("[\"foo\",\"bar\"]", marshaller.marshal(Arrays.asList("foo", "bar")));
    }

    @Test
    public void testMarshalObjectWithMember() throws Exception
    {
        assertEquals("{\"string\":\"foo\"}", marshaller.marshal(new ObjectWithMember("foo")));
    }

    @Test
    public void testMarshalObjectWithNullMember() throws Exception
    {
        assertEquals("{}", marshaller.marshal(new ObjectWithMember(null)));
    }

    @Test
    public void testMarshalObjectWithMemberMissingAnnotation() throws Exception
    {
        assertEquals("{}", marshaller.marshal(new ObjectWithMemberMissingAnnotation("foo")));
    }

    @Test
    public void testMarshalObjectWithMemberWithRenaming() throws Exception
    {
        assertEquals("{\"str\":\"foo\"}", marshaller.marshal(new ObjectWithMemberWithRenaming("foo")));
    }

    @XmlRootElement
    private static class ObjectWithMember
    {
        @SuppressWarnings("unused")
        @XmlElement
        private final String string;

        public ObjectWithMember(String string)
        {
            this.string = string;
        }
    }

    @XmlRootElement
    private static class ObjectWithMemberMissingAnnotation
    {
        @SuppressWarnings("unused")
        private final String string;

        public ObjectWithMemberMissingAnnotation(String string)
        {
            this.string = string;
        }
    }

    @XmlRootElement
    private static class ObjectWithMemberWithRenaming
    {
        @SuppressWarnings("unused")
        @XmlElement(name = "str")
        private final String string;

        public ObjectWithMemberWithRenaming(String string)
        {
            this.string = string;
        }
    }
}

