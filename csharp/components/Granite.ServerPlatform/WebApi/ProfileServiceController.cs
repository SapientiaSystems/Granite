//  ##########################################################################
//  # File Name: ProfileServiceController.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Profile;
using Ninject;
using System.Web.Http;

namespace Granite.ServerPlatform.WebApi
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [RoutePrefix("v1/profile")]
    public
    class ProfileServiceController:
        ApiController,
        IProfileService
    {
        private IProfileService itsImp;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        [Inject]
        public
        ProfileServiceController(IProfileService imp)
        {
            itsImp = imp;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("create")]
        [HttpPost]
        public CreateProfileReply 
        CreateProfile(CreateProfileRequest request)
        { 
            return itsImp.CreateProfile(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("update")]
        [HttpPut]
        public UpdateProfileReply 
        UpdateProfile(UpdateProfileRequest request)
        {
            return itsImp.UpdateProfile(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("delete")]
        [HttpDelete]
        public DeleteProfileReply
        DeleteProfile(DeleteProfileRequest request)
        {
            return itsImp.DeleteProfile(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("get")]
        [HttpPost]
        public GetProfileReply 
        GetProfile(GetProfileRequest request)
        {
            return itsImp.GetProfile(request);
        }
    }
}

//  ##########################################################################
