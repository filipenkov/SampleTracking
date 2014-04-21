package com.sysbliss.jira.plugins.workflow.model.layout.serialize;

import com.sysbliss.jira.plugins.workflow.model.WorkflowAnnotation;
import com.sysbliss.jira.plugins.workflow.model.WorkflowAnnotationImpl;
import com.sysbliss.jira.plugins.workflow.model.layout.JWDLayout;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: jdoklovic
 */
public class JSONAnnotationSerializer {

    public String serialize(final List<WorkflowAnnotation> annotations) throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        final StringWriter sw = new StringWriter();
        mapper.writeValue(sw, annotations);

        return sw.toString();
    }

    public List<WorkflowAnnotation> deserialize(String json) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonFactory jf = new JsonFactory();

        List<WorkflowAnnotation> annotations = mapper.readValue(jf.createJsonParser(new StringReader(json)), TypeFactory.collectionType(ArrayList.class, WorkflowAnnotationImpl.class));

        return annotations;
    }
}
