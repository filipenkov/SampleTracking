package org.jcvi.jira.plugins.utils.typemapper;

/**
 * User: pedworth
 * Date: 11/16/11
 * <p>Wraps two mappers where forwardMapper maps from A to B and reverse
 * mapper maps from B to A. Ideally
 * convertForward(convertBackward(in)) == in == convertBackward(convertForward(in))
 * </p>
 */
public class TypeMapperPair<A,B> {
    private final TypeMapper<A,B> forward;
    private final TypeMapper<B,A> backward;
    private final Class<A> start;
    private final Class<B> result;

    public TypeMapperPair(Class<A> startClass,
                          Class<B> resultClass,
                          TypeMapper<A,B> forwardMapper,
                          TypeMapper<B,A> backwardMapper) {
        this.forward  = forwardMapper;
        this.backward = backwardMapper;
        this.start    = startClass;
        this.result   = resultClass;
    }

    public Class<A> getStartClass() {
        return start;
    }

    public Class<B> getResultClass() {
        return result;
    }

    public B convertForward(A value) {
        //this test is here as when the mapper is being
        //used it may not always be 'protected' by Generics
        testType(start,value);
        return forward.convert(value);
    }

    public A convertBackward(B value) {
        testType(result,value);
        return backward.convert(value);
    }

    //throws an exception if there is a problem with
    //the passed in type
    private void testType(Class type, Object toTest) {
        if (toTest == null) {
            return;
        }
        if (!type.isInstance(toTest)) {
            throw new ClassCastException("This mapper takes Objects of type "+
                    type.getName()+" but the input was of type of type "+
                    toTest.getClass().getName());
        }
    }
}
