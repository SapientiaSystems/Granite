//  ##########################################################################
//  # File Name: RegistrationServiceController.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Injection;
using Granite.ProvidedService.Registration;
using System.Web.Http;
using SystemEx.Injection;

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
        RegistrationServiceController()
        {
            itsImp = 
                Global
                    .Container
                    .GetInstance<IRegistrationService>();
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
