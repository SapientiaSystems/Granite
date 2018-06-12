//  ##########################################################################
//  # File Name: SessionClosedEvent.cs
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
    class SessionClosedEvent:
        AbstractDomainEvent<ISession>,
        ISessionDomainEvent
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        SessionClosedEvent(ISession source):
            base("SessionClosed",source) {}
    }
}

//  ##########################################################################
