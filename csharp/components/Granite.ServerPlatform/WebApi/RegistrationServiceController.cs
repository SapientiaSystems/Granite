//  ##########################################################################
//  # File Name: RegistrationServiceController.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Registration;
using Ninject;
using System.Web.Http;

namespace Granite.ServerPlatform.WebApi
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [RoutePrefix("v1/registration")]
    public
    class RegistrationServiceController:
        ApiController,
        IRegistrationService
    {
        private IRegistrationService itsImp;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        [Inject]
        public
        RegistrationServiceController(IRegistrationService imp)
        {
            itsImp = imp;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("register")]
        [HttpPost]
        public RegisterReply 
        Register(RegisterRequest request)
        {
            return itsImp.Register(request);
        }
    }
}

//  ##########################################################################
