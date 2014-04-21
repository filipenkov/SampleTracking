package com.atlassian.plugins.rest.common.sal.websudo.nopackageprotection;

import com.atlassian.sal.api.websudo.WebSudoRequired;

public class MethodProtectedByMethodAnnotation
{

    public void aMethod() {}

    @WebSudoRequired
    public void bMethod() {}
}
