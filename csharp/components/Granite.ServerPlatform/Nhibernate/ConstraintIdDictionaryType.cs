//  ##########################################################################
//  # File Name: ConstraintIdDictionaryType.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Constraint;
using Strata.Nhibernate.Mapping;

namespace Granite.ServerPlatform.Nhibernate
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class ConstraintIdDictionaryType:
        MappableDictionaryType<string,string,ConstraintIdDictionary> {}

}

//  ##########################################################################
