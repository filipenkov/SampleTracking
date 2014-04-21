package com.atlassian.plugins.rest.common.sal.websudo.nopackageprotection;

import com.atlassian.sal.api.websudo.WebSudoNotRequired;
import com.atlassian.sal.api.websudo.WebSudoRequired;


@WebSudoRequired
public class ClassProtectedByClassAnnotation
{
    public void aMethod(){}
}
