//////////////////////////////////////////////////////////////////////////////
// StringMapperRegistry.java
//////////////////////////////////////////////////////////////////////////////

package granite.foundation.core.mapper;

import strata.foundation.core.mapper.IMapper;
import strata.foundation.core.mapper.MapperRegistry;

public
class StringMapperRegistry
    extends MapperRegistry
{
    public
    StringMapperRegistry() { super(); }

    @Override
    public <D,S> StringMapperRegistry
    register(Class<D> destType,Class<S> srcType,IMapper<D,S> mapper)
    {
        checkMappingKey(destType,srcType);
        return (StringMapperRegistry)super.register(destType,srcType,mapper);
    }

    @Override
    public <D,S> StringMapperRegistry
    unregister(Class<D> destType,Class<S> srcType)
    {
        return (StringMapperRegistry)super.unregister(destType,srcType);
    }

    public static StringMapperRegistry
    of()
    {
        return new StringMapperRegistry();
    }

    private static <D,S> void
    checkMappingKey(Class<D> dest,Class<S> src)
        throws IllegalArgumentException
    {
        if (String.class.isAssignableFrom(src) || String.class.isAssignableFrom(dest))
            return;

        throw
            new IllegalArgumentException(
                "Mapping source or destination must be String");
    }

}

//////////////////////////////////////////////////////////////////////////////
