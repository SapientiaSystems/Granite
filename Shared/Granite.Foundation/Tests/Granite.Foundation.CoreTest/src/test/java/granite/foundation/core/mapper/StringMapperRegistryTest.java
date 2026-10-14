//////////////////////////////////////////////////////////////////////////////
// StringMapperRegistryTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import strata.foundation.core.mapper.IMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("CommitStage")
public
class StringMapperRegistryTest
{
    private StringMapperRegistry target;

    @BeforeEach
    public void
    setUp()
    {
        target = StringMapperRegistry.of();
    }

    @Test
    public void
    testOfReturnsNonNullInstance()
    {
        assertNotNull(StringMapperRegistry.of());
    }

    @Test
    public void
    testOfReturnsNewInstanceEachCall()
    {
        StringMapperRegistry first  = StringMapperRegistry.of();
        StringMapperRegistry second = StringMapperRegistry.of();

        assertNotSame(first,second);
    }

    @Test
    public void
    testConstructorProducesUsableInstance()
    {
        StringMapperRegistry registry = new StringMapperRegistry();

        assertFalse(registry.has(Integer.class,String.class));
    }

    @Test
    public void
    testRegisterStoresMapperWithStringSource()
    {
        IMapper<Integer,String> mapper = Integer::valueOf;

        target.register(Integer.class,String.class,mapper);

        assertTrue(target.has(Integer.class,String.class));
    }

    @Test
    public void
    testRegisterStoresMapperWithStringDestination()
    {
        IMapper<String,Integer> mapper = Object::toString;

        target.register(String.class,Integer.class,mapper);

        assertTrue(target.has(String.class,Integer.class));
    }

    @Test
    public void
    testRegisterReturnsThisForChaining()
    {
        IMapper<Integer,String> mapper = Integer::valueOf;

        StringMapperRegistry returned =
            target.register(Integer.class,String.class,mapper);

        assertSame(target,returned);
    }

    @Test
    public void
    testRegisterThrowsWhenNeitherSourceNorDestinationIsString()
    {
        IMapper<Integer,Long> mapper = Long::intValue;

        assertThrows(
            IllegalArgumentException.class,
            () -> target.register(Integer.class,Long.class,mapper));
    }

    @Test
    public void
    testRegisterOverwritesPreviousMapper()
    {
        IMapper<Integer,String> first  = s -> 1;
        IMapper<Integer,String> second = s -> 2;

        target.register(Integer.class,String.class,first);
        target.register(Integer.class,String.class,second);

        Optional<IMapper<Integer,String>> actual =
            target.get(Integer.class,String.class);

        assertTrue(actual.isPresent());
        assertSame(second,actual.get());
    }

    @Test
    public void
    testUnregisterRemovesMapping()
    {
        target.register(Integer.class,String.class,Integer::valueOf);

        target.unregister(Integer.class,String.class);

        assertFalse(target.has(Integer.class,String.class));
        assertTrue(target.get(Integer.class,String.class).isEmpty());
    }

    @Test
    public void
    testUnregisterReturnsThisForChaining()
    {
        target.register(Integer.class,String.class,Integer::valueOf);

        StringMapperRegistry returned =
            target.unregister(Integer.class,String.class);

        assertSame(target,returned);
    }

    @Test
    public void
    testUnregisterOfAbsentMappingIsNoOp()
    {
        assertDoesNotThrow(
            () -> target.unregister(Integer.class,String.class));
        assertFalse(target.has(Integer.class,String.class));
    }

    @Test
    public void
    testUnregisterAllRemovesEveryMapping()
    {
        target.register(Integer.class,String.class,Integer::valueOf);
        target.register(String.class,Integer.class,Object::toString);

        target.unregisterAll();

        assertFalse(target.has(Integer.class,String.class));
        assertFalse(target.has(String.class,Integer.class));
    }

    @Test
    public void
    testUnregisterAllOnEmptyRegistryIsNoOp()
    {
        assertDoesNotThrow(() -> target.unregisterAll());
        assertFalse(target.has(Integer.class,String.class));
    }

    @Test
    public void
    testHasReturnsFalseWhenAbsent()
    {
        assertFalse(target.has(Integer.class,String.class));
    }

    @Test
    public void
    testHasReturnsTrueWhenRegistered()
    {
        target.register(Integer.class,String.class,Integer::valueOf);

        assertTrue(target.has(Integer.class,String.class));
    }

    @Test
    public void
    testGetReturnsRegisteredMapper()
    {
        IMapper<Integer,String> mapper = Integer::valueOf;

        target.register(Integer.class,String.class,mapper);

        Optional<IMapper<Integer,String>> actual =
            target.get(Integer.class,String.class);

        assertTrue(actual.isPresent());
        assertSame(mapper,actual.get());
    }

    @Test
    public void
    testGetReturnsEmptyWhenAbsent()
    {
        Optional<IMapper<Integer,String>> actual =
            target.get(Integer.class,String.class);

        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void
    testGetMapperFunctionsCorrectly()
    {
        target.register(Integer.class,String.class,Integer::valueOf);

        Optional<IMapper<Integer,String>> mapper =
            target.get(Integer.class,String.class);

        assertTrue(mapper.isPresent());
        assertEquals(Integer.valueOf(42),mapper.get().map("42"));
    }

    @Test
    public void
    testTypeDirectionalityIsDistinct()
    {
        IMapper<Integer,String> stringToInteger = Integer::valueOf;
        IMapper<String,Integer> integerToString = Object::toString;

        target.register(Integer.class,String.class,stringToInteger);
        target.register(String.class,Integer.class,integerToString);

        Optional<IMapper<Integer,String>> forward =
            target.get(Integer.class,String.class);
        Optional<IMapper<String,Integer>> reverse =
            target.get(String.class,Integer.class);

        assertTrue(forward.isPresent());
        assertTrue(reverse.isPresent());
        assertSame(stringToInteger,forward.get());
        assertSame(integerToString,reverse.get());
    }

    @Test
    public void
    testUnregisterOneDirectionLeavesOtherIntact()
    {
        target.register(Integer.class,String.class,Integer::valueOf);
        target.register(String.class,Integer.class,Object::toString);

        target.unregister(Integer.class,String.class);

        assertFalse(target.has(Integer.class,String.class));
        assertTrue(target.has(String.class,Integer.class));
    }

    @Test
    public void
    testFluentChainOfOperations()
    {
        IMapper<Integer,String> stringToInteger = Integer::valueOf;
        IMapper<String,Integer> integerToString = Object::toString;

        StringMapperRegistry returned =
            target
                .register(Integer.class,String.class,stringToInteger)
                .register(String.class,Integer.class,integerToString)
                .unregister(Integer.class,String.class);

        assertSame(target,returned);
        assertFalse(target.has(Integer.class,String.class));
        assertTrue(target.has(String.class,Integer.class));
    }

    @Test
    public void
    testInstancesHaveIndependentState()
    {
        StringMapperRegistry    other  = StringMapperRegistry.of();
        IMapper<Integer,String> mapper = Integer::valueOf;

        target.register(Integer.class,String.class,mapper);

        assertTrue(target.has(Integer.class,String.class));
        assertFalse(other.has(Integer.class,String.class));
        assertTrue(other.get(Integer.class,String.class).isEmpty());
    }

    @Test
    public void
    testUnregisterOnOtherInstanceDoesNotAffectThisInstance()
    {
        StringMapperRegistry other = StringMapperRegistry.of();

        target.register(Integer.class,String.class,Integer::valueOf);
        other.register(Integer.class,String.class,s -> 99);

        other.unregister(Integer.class,String.class);

        assertTrue(target.has(Integer.class,String.class));
        assertFalse(other.has(Integer.class,String.class));
    }
}

//////////////////////////////////////////////////////////////////////////////
