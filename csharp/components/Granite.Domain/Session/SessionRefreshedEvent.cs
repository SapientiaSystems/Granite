//  ##########################################################################
//  # File Name: SessionRefreshedEvent.cs
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
    class SessionRefreshedEvent:
        AbstractDomainEvent<ISession>,
        ISessionDomainEvent
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        SessionRefreshedEvent(ISession source):
            base("SessionRefreshed",source) {}
    }
}

//  ##########################################################################
