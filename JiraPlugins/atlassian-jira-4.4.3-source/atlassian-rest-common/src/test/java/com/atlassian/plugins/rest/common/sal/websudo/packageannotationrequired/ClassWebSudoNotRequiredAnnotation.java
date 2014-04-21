package com.atlassian.plugins.rest.common.sal.websudo.packageannotationrequired;

import com.atlassian.sal.api.websudo.WebSudoNotRequired;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoNotRequired
public class ClassWebSudoNotRequiredAnnotation
{
    public void aMethod() {}


    @WebSudoRequired
    public void bMethod() {}
}
