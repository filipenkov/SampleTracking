package com.atlassian.spring.extension.registration;

public interface ThingToRegisterWith
{
    public void addThingToRegister(String key, ThingToRegister value);

    public void methodWithNoKey(ThingToRegister value);

    public void methodWithWrongType(String key, SubThingToRegister value);
}
