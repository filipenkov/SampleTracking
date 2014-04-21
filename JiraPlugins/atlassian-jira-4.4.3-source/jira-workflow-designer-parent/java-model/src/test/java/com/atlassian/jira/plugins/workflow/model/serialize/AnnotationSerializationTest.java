package com.atlassian.jira.plugins.workflow.model.serialize;

import com.sysbliss.jira.plugins.workflow.model.WorkflowAnnotation;
import com.sysbliss.jira.plugins.workflow.model.WorkflowAnnotationImpl;
import com.sysbliss.jira.plugins.workflow.model.layout.serialize.JSONAnnotationSerializer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Author: jdoklovic
 */
public class AnnotationSerializationTest {

    @Test
    public void serializeMultipleAnnotations() throws Exception {
        List<WorkflowAnnotation> annotations = new ArrayList<WorkflowAnnotation>();

        WorkflowAnnotation a1 = new WorkflowAnnotationImpl();
        a1.setId("1");
        a1.setDescription("i am the first!");

        WorkflowAnnotation a2 = new WorkflowAnnotationImpl();
        a2.setId("2");
        a2.setDescription("i am the second!");

        annotations.add(a1);
        annotations.add(a2);

        JSONAnnotationSerializer serializer = new JSONAnnotationSerializer();
        String json = serializer.serialize(annotations);

        assertTrue(json.contains("first"));
        assertTrue(json.contains("second"));
    }

    @Test
    public void deserializeMultipleAnnotations() throws Exception {
        List<WorkflowAnnotation> annotations = new ArrayList<WorkflowAnnotation>();
        String json = "[{\"id\":\"1\",\"description\":\"i am the first!\"},{\"id\":\"1\",\"description\":\"i am the second!\"}]";

        JSONAnnotationSerializer serializer = new JSONAnnotationSerializer();
        annotations = serializer.deserialize(json);

        assertEquals(2, annotations.size());

    }
}
