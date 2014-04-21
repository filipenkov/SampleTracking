package org.jcvi.jira.importer.mappers;

import org.jcvi.glk.ctm.User;
import org.jcvi.jira.importer.jiramodel.JIRAUser;

import java.util.HashMap;
import java.util.Map;

/**
 * Doesnt' do much mapping. For most users their ctm name is identical
 * to their jira name.
 * todo: possibly needs some clipping for people like me who have a
 * different db username than ldap username.
 */
public class UserMapper {
        //Hardcoded lookup table. Stored here at the top of the file to
    //make it obvious. It is written as a table / 2D-array to both
    //make editing simple and reduce code changes if it needs removing
    //to a separate config file later.
    private static final int POS_INITIALS     = 0;
    private static final int POS_JIRAUSER     = 1;
    private static final String[][] TABLE_INITIALS_JIRANAME = {
            {"AA","aakopov"},
            {"AB","aboyne"},
            {"AR","aransier"},
            {"Becky","rhalpin"},
            {"Bridget","unknown"},
            {"BS","bszczypi"},
            {"DC","unknown"},
            {"DS","dspiro"},
            {"EG","eghedin"},
            {"EH","ehine"},
            {"GD","unknown"},
            {"Javier","unknown"},
            {"JB","jbera"},
            {"JH","unknown"},
            {"JO","jonuska"},
            {"JQ","jquinon"},
            {"JS","unknown"},
            {"JZ","unknown"},
            {"Katie","unknown"},
            {"KM","unknown"},
            {"LO","llosada"},
            {"Mary","unknown"},
            {"MK","mkim"},
            {"MM","mmclella"},
            {"MR","mrosenbe"},
            {"MS","msarmien"},
            {"MYK","unknown"},
            {"NF","nfedorov"},
            {"NM","nmiller"},
            {"NS","unknown"},
            {"OM","omemon"},
            {"RA","ralthoff"},
            {"RH","rhalpin"},
            {"SJ","sjin"},
            {"TF","tamaraf"},
            {"TG","tgallagh"},
            {"TT","ttsitrin"},
            {"VS","vsubbu"},
            {"KD","unknown"},
            {"RK","rkuzmick"},
            {"JMZ","zaborsky"}
    };
    private static Map<String,String> initialsToUsername =
            new HashMap<String, String>();
    static {
        //doesn't lookup JIRAUser as it may not have been initialized
        for(String[] row : TABLE_INITIALS_JIRANAME) {
            initialsToUsername.put(row[POS_INITIALS], row[POS_JIRAUSER]);
        }
    }

    public static JIRAUser getUserFromInitials(String initials)
            throws UnmappedField {
        if (initials == null) {
            return null;
        }
        String username = initialsToUsername.get(initials);
        if (username == null) {
            throw new UnmappedField("User Initials",initials);
        }
        return JIRAUser.getUser(username);
    }

    public static JIRAUser getUser(User user)
            throws UnmappedField {
        if (user == null) {
            return null;
        }
        return JIRAUser.getUser(user.getName());
    }

}
