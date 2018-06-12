//  ##########################################################################
//  # File Name: SessionOpenedEvent.cs
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
    class SessionOpenedEvent:
        AbstractDomainEvent<ISession>,
        ISessionDomainEvent
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        SessionOpenedEvent(ISession source):
            base("SessionOpened",source) {}
    }
}

//  ##########################################################################
