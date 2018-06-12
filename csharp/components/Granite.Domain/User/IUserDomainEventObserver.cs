//  ##########################################################################
//  # File Name: IUserDomainEventObserver.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Domain.Shared;
using System;

namespace Granite.Domain.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IUserDomainEventObserver:
        IDomainEventObserver<IUserDomainEvent> {}
}

//  ##########################################################################
