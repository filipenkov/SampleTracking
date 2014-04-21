package org.jcvi.jira.plugins.utils.typemapper;

//todo: replace with a 'local' implementation, to allow these utils to be
//used outside of jira
import com.atlassian.jira.util.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Created by IntelliJ IDEA.
 * User: pedworth
 * Static methods that are useful in manipulating the types of Object and
 * Collections
 */
public class TypeUtils {

    /**
     * Type safe cast of a collection to a type
     * @param values        The collection to test and cast (NOT NULL)
     * @param typeClass     The class for the type to test against,
     *                      and cast to (NOT NULL)
     * @param <RETURN_TYPE> The type to test against, and cast to
     * @return              A collection containing only objects of type
     *                      RETURN_TYPE
     * @throws TypeConversionException
     *                      If values contains objects that are not of
     *                      type RETURN_TYPE and filter is false then this
     *                      exception is thrown
     */
    public static <RETURN_TYPE> Collection<RETURN_TYPE> testType(
                                          @NotNull Collection<Object> values,
                                          @NotNull Class<RETURN_TYPE> typeClass
                                          ) throws TypeConversionException {
        notNull("values",values);
        notNull("typeClass",typeClass);
        Collection<RETURN_TYPE> filteredCollection = filterType(values,typeClass);
        if (filteredCollection.size() != values.size()) {
            throw new TypeConversionException(
                    "Collection type check failed: "+
                    (values.size() - filteredCollection.size()) +
                    "elements were not of type "+typeClass.getName());
        }
        return filteredCollection;
    }

    /**
     * Type safe conversion of a collection to a type, done by removing
     * any elements of the input collection that are not of the
     * correct type.
     * @param values        The collection to test and cast (NOT NULL)
     * @param typeClass     The class for the type to test against,
     *                      and cast to (NOT NULL)
     * @param <RETURN_TYPE> The type to test against, and cast to
     * @return              A collection containing only objects of type
     *                      RETURN_TYPE
     */
    public static <RETURN_TYPE> Collection<RETURN_TYPE> filterType(
                                       @NotNull Collection<Object> values,
                                       @NotNull Class<RETURN_TYPE> typeClass) {
        notNull("values",values);
        notNull("typeClass",typeClass);
        //a new collection instead of removing items from the passed in
        //collection
        Collection<RETURN_TYPE>toReturn
                                   = new ArrayList<RETURN_TYPE>(values.size());
        for(Object value: values) {
            //only add if it is of the right type
            if (typeClass.isInstance(value)) {
                toReturn.add(typeClass.cast(value));
            }
        }
        return toReturn;
    }

    /**
     * <p>Creates a Collection&lt;Object&gt; out of the startValues.
     * startValues could be either a collection or a single item.</p>
     * <p>If startValues is a collection it is cast to Collection and
     * returned.</p>
     * <p>If startValues is a single value then it is wrapped in a
     * new Collection before being returned.</p>
     * @param startValues    The object to cast or wrap
     * @return  A collection containing the startValue/startValues. If
     *          startValues was null then an empty Collection object is
     *          returned.
     */
    public static Collection<Object> toCollection(Object startValues) {
        if (startValues == null) {
            return new ArrayList<Object>(1);
        }
        if (startValues instanceof Collection) {
            //check all of the entries are of the transportType

            //All collections are Collection<Object>
            //so if this is an instance of a Collection
            //then this cast is valid
            //noinspection unchecked
            return (Collection<Object>) startValues;
        }
        return singleItem(startValues);
    }

    /**
     * Convert a single object into a collection of one item
     */
    public static <RETURN_TYPE> List<RETURN_TYPE> singleItem(RETURN_TYPE content) {
        //single item wrap into a collection
        List<RETURN_TYPE> singleItemCollection = new ArrayList<RETURN_TYPE>(1);
        singleItemCollection.add(content);
        return singleItemCollection;
    }

    //Exception for the testType method
    public static class TypeConversionException extends Exception {
        TypeConversionException(String message) {
            super(message);
        }
    }
}
