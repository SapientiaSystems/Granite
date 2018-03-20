//  ##########################################################################
//  # File Name: SessionApplicationModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Session;
using Strata.Foundation.Injection;

namespace Granite.Application.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class SessionApplicationModule:
        AbstractModule
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        public
        SessionApplicationModule() :
            base("SessionApplicationModule") {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override void 
        Initialize()
        {
            Bind<ISessionService>()
                .ToType<SessionService>()
                .WithScope<RequestScope>();
        }
    }
}

//  ##########################################################################
