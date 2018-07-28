//  ##########################################################################
//  # File Name: IAccountDomainEvent.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Domain.Shared;
using System;

namespace Granite.Domain.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface IAccountDomainEvent:
        IDomainEvent<IAccount>
    {
    }
}

//  ##########################################################################
