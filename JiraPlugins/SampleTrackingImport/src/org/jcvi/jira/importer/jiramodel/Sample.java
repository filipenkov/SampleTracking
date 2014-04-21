package org.jcvi.jira.importer.jiramodel;

import noNamespace.EntityEngineXmlType;
import org.jcvi.glk.*;
import org.jcvi.glk.helpers.GLKHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * An object representing the information about an issue.
 */
public class Sample extends Issue {
    //Sub-Tasks
    private final Set<SubTask> subtasks = new HashSet<SubTask>();

    //GLK data
    private final GLKData glkData;

    public Sample(Project project,
                  GLKHelper helper,
                  String database,
                  EUID extentID,
                  Status status) throws InvalidGLKEntry {
        super(project,
              IssueType.getTaskType(IssueType.SAMPLE_ISSUE_TYPE_NAME),
              null, //no created date stored in the CTM
              status,
              buildCustomFieldObject(helper,extentID,database));
        this.glkData = new GLKData(helper, database, extentID);
    }

    private static HashMap<CustomField, String> buildCustomFieldObject(GLKHelper helper, EUID extentID, String database) throws InvalidGLKEntry {
        Extent extent = helper.getExtent(extentID);
        Extent lot = getParentChecked(extent,"Extent","Lot");
        Extent collection = getParentChecked(extent,"Lot","Collection");

        HashMap<CustomField, String>customFieldValues = new HashMap<CustomField, String>();
        //Todo: Something less hardcoded
        customFieldValues.put(CustomField.getCustomField("Extent Id (load)"),""+extentID);
        customFieldValues.put(CustomField.getCustomField("Database"),database);
        customFieldValues.put(CustomField.getCustomField("BAC Id (load)"),""+extent.getReference());
        customFieldValues.put(CustomField.getCustomField("Collection Code (load)"),""+collection.getReference());
        customFieldValues.put(CustomField.getCustomField("Lot (load)"),""+lot.getReference());
        String[][] attributes = {
            {"batch_id","Batch Id (load)"},
            {"sample_number","Sample Id"},
            {"blinded_number","Blinded Number"},
            {"subtype","Computed Subtype"}
        };
        for (String[] pair: attributes) {
            ExtentAttributeType type = helper.getExtentAttributeType(pair[0]);
            ExtentAttribute attribute = extent.getAttribute(type);

            CustomField customField = CustomField.getCustomField(pair[1]);

            if (attribute != null && attribute.getValue() != null) {
                customFieldValues.put(customField,attribute.getValue());
            }
        }
        return customFieldValues;
    }

    /**
     * Small utility method to add null checks to recursion up the Extent tree
     * @param start         The extent to get the parent of
     * @param currentLevel  Used for error messages only
     * @param nextLevel     Used for error messages only
     * @return  The parent of the extent, never null
     * @throws InvalidGLKEntry  if the start Extent is null or has no parent
     * todo: add check of the ExtentType?
     */
    private static Extent getParentChecked(Extent start, String currentLevel, String nextLevel)
            throws InvalidGLKEntry {
        if (start == null) {
            throw new InvalidGLKEntry("Invalid "+currentLevel);
        }
        Extent parent = start.getParent();
        if (parent == null) {
            throw new InvalidGLKEntry("Invalid "+nextLevel);
        }
        return parent;
    }

    public void addSubTask(SubTask subtask) {
        subtasks.add(subtask);
    }

    //JIRA Fields
    public String getSummary() {
        return glkData.getDatabase()+"_"+
               glkData.getCollectionCode()+"_"+
               glkData.getBACID();
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getExtentID() {
        //not used but the object would be incomplete without it
        return glkData.extentID;
    }

    @Override
    public JIRAUser getAssignee(){
        //CTM Reference objects don't have an assignee
        return JIRAUser.getSystemUser();
    }

    @Override
    public Priority getPriority() {
        //CTM References don't have priorities
        return Priority.getPriority(Priority.DEFAULT_PRIORITY_NAME);
    }

    //XML OUTPUT
    public void addToXML(EntityEngineXmlType xml) {
        super.addToXML(xml);
        addSubTaskXML(xml);
    }

    private void addSubTaskXML(EntityEngineXmlType xml) {
        short number = 1; //ordinal used to order the list of subtasks in the
                          //issue view
        for(SubTask subTask: subtasks) {
            subTask.addToXML(xml);
            subTask.addIssueLinkToXML(xml,number++);
        }
    }

    public GLKData getGLKData() {
        return glkData;
    }
    //collects together the GLK accessing methods
    public static class GLKData {
        private final String extentID;
        private final String BACID;
        private final String collectionCode;
        private final String database;
        private final String sampleNumber;

        //carries out all of the data access during creation.
        //It doesn't store the helper
        private GLKData(GLKHelper helper, String database, EUID extentID)
                    throws InvalidGLKEntry {
            this.extentID = extentID.toString();
            this.database = database;

            //access the GLK
            Extent extent = helper.getExtent(extentID);
            if (extent == null) {
                throw new InvalidGLKEntry("Extent_id");
            }
            //Some attributes are stored in the Extent table
            //BACID is stored in the ref_id field on a Sample's Extent
            this.BACID = extent.getReference();
            Extent set = extent.getParent();
            Extent collection = set.getParent();
            //collection code is stored in the ref_id of the Collection's Extent
            if (collection == null || collection.getReference() == null) {
                throw new InvalidGLKEntry("collection code");
            }
            this.collectionCode = collection.getReference();
            //read attributes
            ExtentAttributeType sampleNumberCodeAttribute
                    = helper.getExtentAttributeType(ExtentAttributeTypeNames.SAMPLE_NUMBER);

            ExtentAttribute sampleNumberAttrib = extent.getAttribute(sampleNumberCodeAttribute);
            if (sampleNumberAttrib == null) {
                //todo: hack for hrv2 samples that don't have a sample number
                //this value is only used in the names of subtasks and so
                //isn't really very important
//                throw new InvalidGLKEntry(ExtentAttributeTypeNames.SAMPLE_NUMBER);
                this.sampleNumber = BACID;
            } else {
                this.sampleNumber = sampleNumberAttrib.getValue();
            }
        }
        //custom fields
        //BAC Id            BAC Id (load)
        public String getBACID() {
            return BACID;
        }
        //Batch Id          Batch Id (load)
        //Blinded Number
        //Collection Code   Collection Code (load)
        public String getCollectionCode() {
            return collectionCode;
        }
        //Computed Subtype
        //Database
        public String getDatabase() {
            return database;
        }
        //(Draft Submission)
        //                  Extent Id (load)
        //Lot               Lot (load)
        //Sample Id

        /**
         * Note not all Sample Numbers are actually numbers.
         * Some have an identifying suffix added
         * @return The value from the GLK.
         */
        public String getSampleNumber() {
            return sampleNumber;
        }
    }
}
