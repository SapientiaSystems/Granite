//  ##########################################################################
//  # File Name: ISessionDomainEvent.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Domain.Shared;

namespace Granite.Domain.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface ISessionDomainEvent:
        IDomainEvent<ISession> {}
}

//  ##########################################################################
