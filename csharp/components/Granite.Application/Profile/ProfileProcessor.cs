//  ##########################################################################
//  # File Name: ProfileProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Service;
using Granite.Domain.Profile;
using Granite.Domain.Session;
using Granite.ProvidedService.Profile;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Logging;
using SystemEx.Injection;

namespace Granite.Application.Profile
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class ProfileProcessor:
        AuthorizedService,
        IProfileProcessor
    {
        private IProfileRepository itsProfileRepository;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        ProfileProcessor(
            IUnitOfWorkProvider provider,
            ILogger             logger,
            ISessionRepository  sessionRepository,
            IProfileRepository  profileRepository):
            base(provider,logger,sessionRepository)
        {
            itsProfileRepository = profileRepository;   
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public CreateProfileReply
        CreateProfile(CreateProfileRequest request)
        {
            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public UpdateProfileReply
        UpdateProfile(UpdateProfileRequest request)
        {
            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public DeleteProfileReply
        DeleteProfile(DeleteProfileRequest request)
        {
            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public GetProfileReply
        GetProfile(GetProfileRequest request)
        {
            return null;
        }


    }
}

//  ##########################################################################
