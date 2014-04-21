package com.atlassian.plugins.rest.common.json;

import javax.xml.bind.JAXBException;

/**
 * Utility service that will allow clients to marshall a Jaxb bean to Json using the same configuration that the REST
 * module uses internally to create Json.
 *
 * @since v1.0.2
 */
public interface JaxbJsonMarshaller
{
    /**
     * Given a jaxbBean this method will return a JSON string.
     *
     * @param jaxbBean the bean to be converted to JSON
     * @return a JSON string
     * @throws JsonMarshallingException if any error occurs marshalling the JSON object
     * @since 1.1
     */
    String marshal(Object jaxbBean) throws JsonMarshallingException;

    /**
     * Given a jaxbBean and all the jaxb classes required to convert the bean to JSON this method will return a JSON
     * string.
     *
     * @param jaxbBean    the bean to be converted to JSON
     * @param jaxbClasses the jaxb classes in use by the jaxb bean.
     * @return a JSON string
     * @throws javax.xml.bind.JAXBException if there's a problem marshalling the bean provided
     * @since 1.0.2
     * @deprecated since 1.1, use {@link #marshal(Object)}
     */
    @Deprecated
    String marshal(Object jaxbBean, Class... jaxbClasses) throws JAXBException;
}
