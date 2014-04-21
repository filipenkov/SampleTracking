package com.atlassian.jira.web.action.admin.index;

import org.apache.log4j.Logger;

/**
 * A logger that doesnt say much
*
* @since v3.13
*/
class QuietLogger extends Logger
{
    public QuietLogger(String string)
    {
        super(string);
    }

    public void error(Object object, Throwable throwable)
    {
        //shhh
    }


    public void error(Object object)
    {
        // ssshhh
    }

    public void info(Object object)
    {
        // sssh
    }
}
