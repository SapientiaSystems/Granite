//  ##########################################################################
//  # File Name: ProfileService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Service;
using Granite.Application.Shared;
using Granite.Application.Tenant;
using Granite.Common.Access;
using Granite.Domain.Session;
using Granite.ProvidedService.Profile;
using Strata.Application.Messaging;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Logging;
using SystemEx.Injection;

namespace Granite.Application.Profile
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class ProfileService:
        AuthorizedService,
        IProfileService
    {
        private IProfileProcessor itsProcessor;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        ProfileService(
            IUnitOfWorkProvider      provider,
            IActionQueue             queue,
            ILogger                  logger,
            ITenantManager           manager,
            IAccessTokenRefresher    refresher,
            IClientIpAddressRecorder recorder,
            ISessionRepository       sessions,
            IProfileProcessor        processor):
            base(provider,queue,logger,manager,refresher,recorder,sessions)
        {
            itsProcessor = processor;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public CreateProfileReply 
        CreateProfile(CreateProfileRequest request)
        {
            return itsProcessor.CreateProfile(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public UpdateProfileReply 
        UpdateProfile(UpdateProfileRequest request)
        {
            return itsProcessor.UpdateProfile(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public DeleteProfileReply 
        DeleteProfile(DeleteProfileRequest request)
        {
            return itsProcessor.DeleteProfile(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public GetProfileReply 
        GetProfile(GetProfileRequest request)
        {
            return itsProcessor.GetProfile(request);
        }
    }
}

//  ##########################################################################
