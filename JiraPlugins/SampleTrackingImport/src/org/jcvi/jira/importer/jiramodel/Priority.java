package org.jcvi.jira.importer.jiramodel;

import noNamespace.EntityEngineXmlType;
import noNamespace.PriorityType;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class Priority extends NameIDPair {
    private static Map<String,Priority> prioritiesByName
            = new HashMap<String, Priority>();

    public static final String DEFAULT_PRIORITY_NAME = "Major";

    public static Priority getPriority(String name) {
        return prioritiesByName.get(name);
    }

    public static void staticPopulateFromXML(EntityEngineXmlType xml) {
        for (PriorityType priorityType: xml.getPriorityArray()) {
            Priority priority = new Priority(priorityType);
            prioritiesByName.put(priority.getName(), priority);
        }
    }

    public Priority(PriorityType priorityType) {
        super(priorityType.getId(),priorityType.getName());
    }

    /*
     JIRA has the following priorities:
     Blocker
     Critical
     Major
     Minor
     Trivial
     Defer

     Found using a grep of the Entities.xml file
     */
    /*
    The CTM only has 4 priorities
    name
    --------------------
    critical
    high
    low
    medium

    Found using:
        DROP TABLE #Priorities;
        CREATE TABLE #Priorities
        (name VARCHAR(20)
        );

        \for db in giv giv2 giv3 piv swiv rtv gcv hrv2 mpv rsv hpiv1 hpiv3 hadv jev veev yfv vda msl mmp rbl vzv norv barda synflu
        INSERT INTO #Priorities (name)
        SELECT
            DISTINCT(priority) AS name
        FROM
            ${db}..ctm_task_history
        ;

        INSERT INTO #Priorities (name)
        SELECT
            DISTINCT(priority) AS name
        FROM
            ${db}..ctm_task
        ;

        \done

        SELECT DISTINCT(name)
        FROM #Priorities
        ORDER BY name;
     */

//    private static byte orderNumberCounter = 0;
//    private static byte getNewOrderNumber() {
//        return orderNumberCounter++;
//    }
//
//    public static final Priority BLOCKER  = new Priority((short)1,"Blocker","Blocks development and/or testing work, production could not run.","/images/icons/priority_blocker.gif","#cc0000");
//    public static final Priority CRITICAL = new Priority((short)2,"Critical","Crashes, loss of data, severe memory leak.","/images/icons/priority_critical.gif","#ff0000");
//    public static final Priority MAJOR    = new Priority((short)3,"Major","Major loss of function.","/images/icons/priority_major.gif","#009900");
//    public static final Priority MINOR    = new Priority((short)4,"Minor","Minor loss of function, or other problem where easy workaround is present.","/images/icons/priority_minor.gif","#006600");
//    public static final Priority TRIVIAL  = new Priority((short)5,"Trivial","Cosmetic problem like misspelt words or misaligned text.","/images/icons/priority_trivial.gif","#003300");
//    public static final Priority DEFER    = new Priority((short)6,"Defer","Change not required or relevent","/images/icons/priority_trivial.gif","#ffff99");
//
//    public static final Priority DEFAULT = MAJOR;
//
//    private static List<Priority> priorities = new ArrayList<Priority>(6);
//
//    private String name;
//    private short id;
//    private String description;
//    private String iconurl;
//    private String color;
//    private byte orderNumber;
//
//    public Priority(short id,
//                    String name,
//                    String description,
//                    String iconurl,
//                    String color
//                    ) {
//        this.id = id;
//        this.name = name;
//        this.description = description;
//        this.iconurl = iconurl;
//        this.color = color;
//        this.orderNumber = getNewOrderNumber();
//        priorities.add(this);
//    }
//
//    public String getName() {
//        return name;
//    }
//    public short getID() {
//        return id;
//    }
//
//    //static, add them all
//    public static void addIssueLinkToXML(EntityEngineXmlType xml) {
//        for(Priority priority: priorities) {
//            PriorityType priorityXML = xml.addNewPriority();
//            priorityXML.setId(priority.getID());
//            priorityXML.setName(priority.getName());
//            priorityXML.setDescription(priority.description);
//            //priority.setDescription2();//element?
//            priorityXML.setIconurl(priority.iconurl);
//            priorityXML.setStatusColor(priority.color);
//            priorityXML.setSequence(priority.orderNumber);
//        }
//    }

}
