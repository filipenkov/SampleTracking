package com.atlassian.plugins.rest.common.json;

import static org.junit.Assert.assertEquals;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
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

    @Test
    public void testMarshalObjectWithJsonAnnotatedPropertyNull() throws Exception
    {
        assertEquals("Default behaviour with @JsonProperty annotations should exclude null members",
                "{}", marshaller.marshal(new JsonBeanInclusionDefault(null)));
    }

    @Test
    public void testMarshalObjectWithJsonSerializeAnnotationButNoExplicitInclusionMeansAlways() throws Exception
    {
        assertEquals("Behaviour with @JsonSerialize includes non-null members",
                "{\"name\":null}", marshaller.marshal(new JsonBeanWithAnnotationButNoExplicitInclusion(null)));
    }

    @Test
    public void testMarshalObjectWithJsonAnnotatedPropertyNullAndAlwaysInclusion() throws Exception
    {
        assertEquals("If we ask for null members to always be included they should be in the result",
                "{\"name\":null}", marshaller.marshal(new JsonBeanInclusionAlways(null)));
    }

    @Test
    public void testMarshalObjectWithJsonAnnotatedPropertyNullAndNonNullInclusion() throws Exception
    {
        assertEquals("Non-NULL inclusion means null members should not be in the output",
                "{}", marshaller.marshal(new JsonBeanInclusionNonNull(null)));
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

    static class JsonBeanInclusionDefault
    {
        @JsonProperty
        public final String name;

        JsonBeanInclusionDefault(String name)
        {
            this.name = name;
        }
    }

    @JsonSerialize
    static class JsonBeanWithAnnotationButNoExplicitInclusion
    {
        @JsonProperty
        public final String name;

        JsonBeanWithAnnotationButNoExplicitInclusion(String name)
        {
            this.name = name;
        }
    }

    @JsonSerialize(include = Inclusion.ALWAYS)
    static class JsonBeanInclusionAlways
    {
        @JsonProperty
        public final String name;

        JsonBeanInclusionAlways(String name)
        {
            this.name = name;
        }
    }

    @JsonSerialize(include = Inclusion.NON_NULL)
    static class JsonBeanInclusionNonNull
    {
        @JsonProperty
        public final String name;

        JsonBeanInclusionNonNull(String name)
        {
            this.name = name;
        }
    }
}

