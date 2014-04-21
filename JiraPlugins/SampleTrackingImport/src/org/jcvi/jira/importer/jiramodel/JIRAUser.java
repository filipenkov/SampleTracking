package org.jcvi.jira.importer.jiramodel;

import noNamespace.EntityEngineXmlType;
import noNamespace.UserType;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class JIRAUser extends NameIDPair {
    public static final String SAMPLETRACKING_USER = "sampletracking";
    public static final String OLD_SYSTEM_USER = "glk_admin";
    private static final String UNKNOWN_USER = "unknown";

    //mappings
    private static Map<String,String> mapUserName
                = new HashMap<String, String>();

    private static Map<String,JIRAUser> usersByName
            = new HashMap<String, JIRAUser>();

    static {
        //setup the non-direct mappings
        mapUserName.put(OLD_SYSTEM_USER, SAMPLETRACKING_USER);
        mapUserName.put("ctm_admin", SAMPLETRACKING_USER);
        mapUserName.put("jquinon","jquinone");
        mapUserName.put("unassigned",UNKNOWN_USER);//todo
    }

    public static JIRAUser getSystemUser() {
        return getUser(SAMPLETRACKING_USER);
    }

    public static JIRAUser getUser(String name) {
        if (name == null) {
            return null;
        }
        if (mapUserName.containsKey(name)) {
            name = mapUserName.get(name);
        }
        JIRAUser user = usersByName.get(name);
        if (user == null) {
            System.err.println("Unknown User: "+name);
            mapUserName.put(name,UNKNOWN_USER);
            user = usersByName.get(UNKNOWN_USER);
        }
//        checkIfTheUserHasBeenUsedBefore(user);
        return user;
    }

    public static void staticPopulateFromXML(EntityEngineXmlType xml) {
        for (UserType userType : xml.getUserArray()) {
            JIRAUser user = new JIRAUser(userType);
            usersByName.put(user.getName(), user);
        }
    }

    public JIRAUser(UserType userType) {
        super(userType.getId(),userType.getUserName());
    }

    public boolean equals(Object o) {
        return (o != null &&
                o instanceof JIRAUser &&
                ((JIRAUser) o).getID() == this.getID());
    }

//    private static Set<JIRAUser>usedUsers = new HashSet<JIRAUser>();

//    private static void checkIfTheUserHasBeenUsedBefore(JIRAUser user) {
//        if (usedUsers.contains(user)) {
//            return;
//        }
////        System.out.println(user.getName());
//        usedUsers.add(user);
//    }
}
