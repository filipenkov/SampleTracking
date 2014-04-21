/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.jira.appconsistency.integrity.integritycheck.EntityIntegrityCheck;

public interface EntityCheck extends Check
{
    EntityIntegrityCheck getEntityIntegrityCheck();

    void setEntityIntegrityCheck(EntityIntegrityCheck entityIntegrityCheck);
}
