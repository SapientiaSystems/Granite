//  ##########################################################################
//  # File Name: RegistrationService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Service;
using Granite.Application.Shared;
using Granite.Application.Tenant;
using Granite.Common.Access;
using Granite.Domain.Session;
using Granite.ProvidedService.Registration;
using Strata.Application.Messaging;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Logging;
using SystemEx.Injection;

namespace Granite.Application.Registration
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class RegistrationService:
        AuthorizedService,
        IRegistrationService
    {
        private IRegistrationProcessor itsProcessor;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        RegistrationService(
            IUnitOfWorkProvider      provider,
            IActionQueue             queue,
            ILogger                  logger,
            ITenantManager           manager,
            IAccessTokenRefresher    refresher,
            IClientIpAddressRecorder recorder,
            ISessionRepository       sessions,
            IRegistrationProcessor   processor):
            base(provider,queue,logger,manager,refresher,recorder,sessions)
        {
            itsProcessor = processor;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public RegisterReply 
        Register(RegisterRequest request)
        {
            return itsProcessor.Register(request);
        }
    }
}

//  ##########################################################################
