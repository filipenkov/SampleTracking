package org.jcvi.jira.importer.jiramodel;

import noNamespace.EntityEngineXmlType;
import noNamespace.ProjectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
public class Project extends NameIDPair {
    private final Set<Sample> samples = new HashSet<Sample>();

    public void addSample(Sample sample) {
        samples.add(sample);
    }

    public Set<Sample> getSamples() {
        return samples;
    }

    private final String abbreviation;
    private static Map<String,Project> projectsByName
            = new HashMap<String, Project>();

    public static final String DEFAULT_PROJECT_NAME = "IMPORT";

    public static Project getProject(String name) {
        if (name == null) {
            return null;
        }
        return projectsByName.get(name);
    }

    public static void staticPopulateFromXML(EntityEngineXmlType xml) {

        for (ProjectType projectType : xml.getProjectArray()) {
            Project project = new Project(projectType);
            projectsByName.put(project.getName(), project);
        }
    }

    public Project(ProjectType projectType) {
        super(projectType.getId(), projectType.getName());
        this.abbreviation = projectType.getKey();
    }

    public String getAbbreviation() {
        return abbreviation;
    }
}
