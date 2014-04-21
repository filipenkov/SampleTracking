/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.user;

public interface UserEventType
{
    public static int USER_SIGNUP = 0;
    public static int USER_CREATED = 1;
    public static int USER_FORGOTPASSWORD = 2;
    public static int USER_FORGOTUSERNAME = 3;
    public static int USER_CANNOTCHANGEPASSWORD = 4;
}
