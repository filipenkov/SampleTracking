package com.atlassian.soy.renderer;

import java.util.Set;

public interface SoyServerFunction<T>
{

    String getName();

    T apply(Object... args);

    Set<Integer> validArgSizes();

}
