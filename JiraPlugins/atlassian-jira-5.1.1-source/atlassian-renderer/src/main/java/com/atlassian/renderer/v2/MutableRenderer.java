package com.atlassian.renderer.v2;

import java.util.List;

/**
 * This sub interface was introduced to fix JRA-13626. The ComponentManager in JIRA requires that we register the
 * V2Renderer instance with an interface. The chosen interface was previously Renderer, but doing this caused
 * ClassCastExceptions to be thrown in some cases when trying to cast the component back to a V2Renderer object.
 *
 * Introducing this interface eliminates the need to cast the component back to a concrete class.
 *
 * @see com.atlassian.renderer.v2.V2Renderer
 */
public interface MutableRenderer extends Renderer
{
    void setComponents(List components);
}
