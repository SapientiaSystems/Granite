//  ##########################################################################
//  # File Name: UserVerifiedDomainEvent.cs
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
    class UserVerifiedDomainEvent:
        AbstractDomainEvent<IUser>,
        IUserDomainEvent
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        UserVerifiedDomainEvent(IUser source): 
            base("UserVerified",source) {}
    }
}

//  ##########################################################################
