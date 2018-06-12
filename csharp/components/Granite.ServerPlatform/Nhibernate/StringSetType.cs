//  ##########################################################################
//  # File Name: StringSetType.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Nhibernate.Mapping;

namespace Granite.ServerPlatform.Nhibernate
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class StringSetType:
        MappableSetType<string,StringSet> {}
}

//  ##########################################################################
