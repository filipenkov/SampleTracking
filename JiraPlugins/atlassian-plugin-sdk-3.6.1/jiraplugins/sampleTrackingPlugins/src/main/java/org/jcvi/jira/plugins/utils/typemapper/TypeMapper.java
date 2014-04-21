package org.jcvi.jira.plugins.utils.typemapper;

/**
 * A generics based wrapper for first method that converts from one type to another
 * User: pedworth
 * Date: 10/31/11
 * Time: 1:25 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TypeMapper <OLDTYPE,NEWTYPE> {
    public NEWTYPE convert(OLDTYPE value);
}
