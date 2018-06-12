//  ##########################################################################
//  # File Name: StringSetType.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Nhibernate.Mapping;
using System;

namespace Granite.ServerPlatform.Nhibernate
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class GuidSetType:
        MappableSetType<Guid,GuidSet> {}
}

//  ##########################################################################
