//  ##########################################################################
//  # File Name: IUserDomainEvent.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Domain.Shared;

namespace Granite.Domain.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IUserDomainEvent:
        IDomainEvent<IUser> {}
}

//  ##########################################################################
