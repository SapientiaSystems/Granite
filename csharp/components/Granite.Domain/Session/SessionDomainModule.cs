//  ##########################################################################
//  # File Name: SessionDomainModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Injection;

namespace Granite.Domain.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class SessionDomainModule:
        AbstractModule
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        SessionDomainModule() :
            base("SessionDomainModule") {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override void 
        Initialize()
        {
            Bind<ISessionRepository>()
                .ToType<SessionRepository>()
                .WithScope(DefaultScope);
        }
    }
}

//  ##########################################################################
