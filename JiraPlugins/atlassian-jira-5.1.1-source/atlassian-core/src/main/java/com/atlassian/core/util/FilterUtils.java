/*
 * Created by IntelliJ IDEA.
 * User: mike
 * Date: Jan 7, 2002
 * Time: 4:32:26 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.atlassian.core.util;

import com.opensymphony.util.TextUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FilterUtils
{
    /**
     * Returned string is non-null IFF there is a true value (ie some text)
     */
    public static String verifyString(String s)
    {
        if (TextUtils.stringSet(TextUtils.noNull(s).trim()))
        {
            return s;
        }
        else
        {
            return null;
        }
    }

    /**
     * Retirned string array is non-null IFF there is a true value (ie some text)
     */
    public static String[] verifyStringArray(String[] sa)
    {
        List result = new ArrayList();

        for (int i = 0; i < sa.length; i++)
        {
            String s = verifyString(sa[i]);
            if (s != null)
                result.add(s);
        }

        if (result.size() == 0)
        {
            return null;
        }
        else
        {
            String[] resultSa = new String[result.size()];
            int count = 0;
            for (Iterator iterator = result.iterator(); iterator.hasNext();)
            {
                resultSa[count++] = (String) iterator.next();
            }

            return resultSa;
        }
    }

    public static Long verifyLong(Long id)
    {
        if (id != null && id.longValue() > 0)
        {
            return id;
        }

        return null;
    }

}
