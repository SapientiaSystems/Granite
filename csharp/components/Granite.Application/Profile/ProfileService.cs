//  ##########################################################################
//  # File Name: ProfileService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Service;
using Granite.Domain.Session;
using Granite.ProvidedService.Profile;
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
        IProfileService
    {
        private IProfileProcessor itsProcessor;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        ProfileService(IProfileProcessor processor)
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
