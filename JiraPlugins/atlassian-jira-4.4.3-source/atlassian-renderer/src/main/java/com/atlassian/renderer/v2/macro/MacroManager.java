package com.atlassian.renderer.v2.macro;

public interface MacroManager
{
    /**
     * Get the first enabled macro we can find with the given name from all our
     * enabled libraries. Returns null either if no macro exists with that name,
     * or if all macros with that name are disabled.
     *
     * <p>If two macros have the same name in different packages, you'll probably get
     * a random macro back.
     *
     * @param name the name of the macro to retrieve
     * @return the appropriate macro, or null of the macro is either non-existent
     *         or disabled.
     */
    Macro getEnabledMacro(String name);
}
