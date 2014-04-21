package org.jcvi.jira.plugins.utils.typemapper;

import com.atlassian.jira.util.NotNull;

import javax.lang.model.element.NestingKind;
import java.util.*;

/**
 * Contains some simple uses of the TypeMapper interface
 * User: pedworth
 * Date: 10/31/11
 * Time: 1:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class TypeMapperUtils {
    /**
     * Converts all of the elements of first List of type V into type T
     * and returns the converted objects in a List of the same type as
     * the output of the mapper
     *
     * @param mapper        The object that carries out the conversion, not null
     * @param startValues   The objects to be mapped. If null an empty collection is returned
     * @param <T> Type of the Elements in the returned collection
     * @param <V> Type of the Elements in the original collection
     * @return  An ArrayList of type T
     */
    @NotNull
    public static <T,V> List<T> mapList(@NotNull TypeMapper<V, T> mapper,
                                        List<V> startValues) {
        if (mapper == null) {
            throw new NullPointerException("A mapper is required");
        }
        if (startValues == null) {
            return null;
        }
        List<T> output = new ArrayList<T>();
        for(V element : startValues) {
            output.add(mapper.convert(element));
        }
        return output;
    }

    /**
     * Converts all of the elements of the Map Object using a TypeMapper for
     * the keys and a second TypeMapper for the values. The most common use
     * will be to convert an untyped map into a String,String map.
     * @param <KN>  Type for the keys in the returned map
     * @param <VN>  Type for the values in the returned map
     * @param <KO>  Type of the keys in the input map
     * @param <VO>  Type of the values in the input map
     */
    public static <KO,VO,KN,VN> Map<KN,VN> mapMap(
                                       @NotNull TypeMapper<KO, KN> keyMapper,
                                       @NotNull TypeMapper<VO, VN> valueMapper,
                                       Map<KO,VO> inputMap) {
        if (keyMapper == null ||
            valueMapper == null) {
            throw new NullPointerException("A key mapper and a value mapper are required");
        }
        if (inputMap == null) {
            return null;
        }
        Map<KN,VN> outputMap = new HashMap<KN, VN>();
        for (KO oldKey : inputMap.keySet()) {
            VO oldValue = inputMap.get(oldKey);
            outputMap.put(keyMapper.convert(oldKey),
                          valueMapper.convert(oldValue));
        }
        return outputMap;
    }

    /**
     * Converts all of the elements of first collection of type V into type T
     * and returns the converted objects in first new collection.
     *
     * @param mapper        The object that carries out the conversion, not null
     * @param startValues   The objects to be mapped. If null an empty collection is returned
     * @param <T> Type of the Elements in the returned collection
     * @param <V> Type of the Elements in the original collection
     * @return  An ArrayList of type T
     */
    @NotNull
    public static <T,V> Collection<T> mapUnorderedCollection(@NotNull TypeMapper<V,T> mapper,
                                                    Collection<V> startValues) {
        List<V> input = null;

        if (startValues != null) {
            if (startValues instanceof List) {
                input = (List<V>)startValues;
            } else {
                //wrap it in a list
                input = new ArrayList<V>();
                input.addAll(startValues);
            }
        }
        return mapList(mapper,input);
    }


    public static class NopMapper<T> implements TypeMapper<T,T> {
        @Override
        public T convert(T value) {
            return value; //null implementation
        }
    }

    public static class StringMapper<T> implements TypeMapper<T,String> {
        @Override
        public String convert(T value) {
            return value.toString();
        }
    }

    /**
     * Not really a TypeMapper but it is needed so often is should be
     * a static methods of something.
     * @param object    The object to call toString on
     * @return  Null if the object is null otherwise the result of object.toString()
     */
    public static String safeToString(Object object) {
        if (object != null) {
            return object.toString();
        }
        return null;
    }
}
