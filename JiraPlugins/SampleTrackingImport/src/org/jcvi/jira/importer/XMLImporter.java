package org.jcvi.jira.importer;

import noNamespace.EntityEngineXmlDocument;
import noNamespace.EntityEngineXmlType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.jcvi.glk.Extent;
import org.jcvi.glk.ExtentAttributeType;
import org.jcvi.glk.ctm.*;
import org.jcvi.glk.helpers.GLKHelper;
import org.jcvi.jira.importer.jiramodel.*;
import org.jcvi.jira.importer.mappers.*;
import org.jcvi.jira.importer.utils.CSVToHash;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

/**
 */
public class XMLImporter {
    private final EntityEngineXmlDocument doc;
    private final Project project;
    private final Set<UnmappedField> failedMappings
            = new HashSet<UnmappedField>();
    private int failedSamples = 0;

    public XMLImporter(Reader xmlLookups, Reader statusesCSV) throws XmlException, IOException {
        this.doc =  EntityEngineXmlDocument.Factory.parse(xmlLookups);
        populateLookupTables();
        populateMappingTables(new CSVToHash(statusesCSV));

        this.project = Project.getProject(Project.DEFAULT_PROJECT_NAME);
    }



    public void populateLookupTables()  {
        //read the file first
        EntityEngineXmlType xmlForReferences = doc.getEntityEngineXml();
        IssueType.staticPopulateFromXML(xmlForReferences);
        JIRAUser.staticPopulateFromXML(xmlForReferences);
        Priority.staticPopulateFromXML(xmlForReferences);
        Project.staticPopulateFromXML(xmlForReferences);
        Resolution.staticPopulateFromXML(xmlForReferences);
        Status.staticPopulateFromXML(xmlForReferences);
        Workflow.staticPopulateFromXML(xmlForReferences);
        CustomField.staticPopulateFromXML(xmlForReferences);
    }

    public void populateMappingTables(CSVToHash statusesCSV) {
        //note all of the JIRAStatus objects should have been populated before this is called
        StatusMapper.staticPopulateFromCSV(statusesCSV);
    }

    public void processCTMData(CTMHelper ctm,
                               GLKHelper glk,
                               String databaseName,
                               boolean activeOnly,
                               boolean verboseErrors) {

        ReferenceFilter filter = new ReferenceFilter(activeOnly,ctm,glk);

        //loop over all of the 'CTMReference's
        for( Reference reference: ctm.getReferences()) {
            String referenceID = databaseName+":"+reference.getExtentId();
            try {
                if (!filter.includeReference(reference)) {
                    continue;
                }
                //setup error handling for this sample
                Set<UnmappedField> samplesFailedMappings
                                        = new HashSet<UnmappedField>();

                Status status = null;
                try {
                    status = StatusMapper.getStatus(reference.getStatus());
                } catch (UnmappedField uf) {
                    samplesFailedMappings.add(uf);
                }

                Set<ChangeGroup> changes  = new HashSet<ChangeGroup>();
                Set<Comment>     comments = new HashSet<Comment>();
                //map the ctm_reference_history
                for(ReferenceHistory event: reference.getHistory()) {
                    try {
                        ChangeGroup update = SampleFactory.createChangeGroup(event);
                        changes.add(update);
                        Comment comment = CommentFactory.createComment(event);
                        comments.add(comment);
                    } catch (UnmappedField uf) {
                        samplesFailedMappings.add(uf);
                    }
                }

                                //create an issue based on the Reference
                Sample currentIssue = new Sample(project,
                                                 glk,
                                                 databaseName,
                                                 reference.getExtentId(),
                                                 status);
                currentIssue.addComments(comments);
                currentIssue.addChangeGroups(changes);

                //TODO: add in any reference attributes
//                for(ReferenceAttribute attribute : reference.getReferenceAttributes()) {
//                    System.out.println("Reference Attribute found: "+attribute.getId().getType().getName()+"="+attribute.getValue());
//                }

                //map ctm_task and ctm_task_history
                for(Task ctmTask: reference.getAllTasks()) {
                    try {
                        SubTask subTask = TaskMapper.addTaskToSample(currentIssue, ctmTask);
                        if (subTask != null) {
                            currentIssue.addSubTask(subTask);
                        }
                    } catch (UnmappedField ue) {
                        if (verboseErrors) {
                            System.err.println("Skipping sub-task owing to: " + ue.getMessage());
                        }
                        samplesFailedMappings.add(ue);
                    }
                }

                //check final status
                try {
                    currentIssue.fixStatus();
                } catch (UnmappedField ue) {
                    samplesFailedMappings.add(ue);
                }

                if (! samplesFailedMappings.isEmpty()) {
                    if (verboseErrors) {
                        String prefix = "Skipping sample "+referenceID+" owing to: ";
                        System.err.println(convertToErrorString(prefix, samplesFailedMappings));
                    } else {
                        System.err.println("Skipping sample "+referenceID+" owing to unmapped fields");
                    }
                    failedSamples++;
                    failedMappings.addAll(samplesFailedMappings);
                } else {
                    project.addSample(currentIssue);
                }
            } catch (InvalidGLKEntry ige) {
                System.err.println("Skipping sample "+referenceID+" owing invalid GLK Data for: " + ige.getField());
                failedSamples++;
            }
        }
    }

    public static String convertToErrorString(String prefix, Set<UnmappedField> failedMappings) {
        StringBuilder errorList = new StringBuilder();
        for (UnmappedField field: failedMappings) {
            errorList.append(prefix)
                     .append(field.getMessage())
                     .append("\n");
        }
        return errorList.toString();
    }

    public Set<UnmappedField> getFailedMappings() {
        return failedMappings;
    }

    public int getFailedSamplesCount() {
        return failedSamples;
    }

    public void outputResults(File outputFile) throws IOException {
        //It should include enough info to satisfy the JIRA importer but not
        //add any issues.
        EntityEngineXmlType entityEngine = doc.getEntityEngineXml();
        {
            for(Issue issue : project.getSamples()) {
                issue.addToXML(entityEngine);
            }
        }
        XmlOptions options = new XmlOptions();
        options.setSavePrettyPrint();
        options.setSavePrettyPrintIndent(4);
        doc.save(outputFile, options);
        int size = project.getSamples().size();
        System.out.println("Import contains: "+size+" samples");
        System.out.println("Issues with errors: "+ getFailedSamplesCount());
    }

    private static class ReferenceFilter {
        private final GLKHelper glk;

        private final ReferenceStatus[] invalidStatuses;
        private final ReferenceStatus CTM_UNRESOLVED;
        private final ExtentAttributeType jiraIDAttribute;
        private final ExtentAttributeType deprecatedAttribute;

        public ReferenceFilter(boolean activeOnly,
                               CTMHelper ctm,
                               GLKHelper glk) {
            ReferenceStatus CTM_DEPRECATED = ctm.getReferenceStatus("Deprecated");
            CTM_UNRESOLVED = ctm.getReferenceStatus("Unresolved");
            if (activeOnly) {
                ReferenceStatus CTM_PUBLISHED  = ctm.getReferenceStatus("Published");
                invalidStatuses = new ReferenceStatus[] {
                        CTM_DEPRECATED,
                        CTM_PUBLISHED,
                        CTM_UNRESOLVED
                };
            } else {
                invalidStatuses = new ReferenceStatus[] {
                        CTM_DEPRECATED,
                };
            }
            jiraIDAttribute = glk.getExtentAttributeType("jira_id");
            deprecatedAttribute = glk.getExtentAttributeType("deprecated");
            this.glk = glk;
        }

        public boolean includeReference(Reference ref)
                throws InvalidGLKEntry {
            if (ref == null) {
                return false;
            }
            ReferenceStatus refStatus = ref.getStatus();
            if (refStatus == null) {
                return false;
            }
            for(ReferenceStatus invalidStatus: invalidStatuses) {
                if (invalidStatus != null &&
                        invalidStatus.equals(refStatus)) {
                    //no need to process further
                    return false;
                }
            }

            Extent extent = glk.getExtent(ref.getExtentId());
            //does it have an extent
            if (extent == null) {
                throw new InvalidGLKEntry("Extent id");
            }

            if (
            //is it deprecated
                extent.hasAttribute(deprecatedAttribute) ||
            //is it already in JIRA
                extent.hasAttribute(jiraIDAttribute)) {
                return false;
            }

            //no reason not to process it
            return true;
        }
    }
}
