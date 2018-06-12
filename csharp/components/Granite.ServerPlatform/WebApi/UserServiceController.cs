//  ##########################################################################
//  # File Name: UserServiceController.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Injection;
using Granite.ProvidedService.User;
using System.Web.Http;
using SystemEx.Injection;

namespace Granite.ServerPlatform.WebApi
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [RoutePrefix("v1/user")]
    public
    class UserServiceController:
        ApiController,
        IUserService
    {
        private IUserService itsImp;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        [Inject]
        public
        UserServiceController()
        {
            itsImp =
                Global
                    .Container
                    .GetInstance<IUserService>();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("create")]
        public CreateUserReply 
        CreateUser(CreateUserRequest request)
        { 
            return itsImp.CreateUser(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("verify")]
        public VerifyUserReply 
        VerifyUser(VerifyUserRequest request)
        {
            return itsImp.VerifyUser(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("update")]
        public UpdateUserReply 
        UpdateUser(UpdateUserRequest request)
        {
            return itsImp.UpdateUser(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("delete")]
        public DeleteUserReply
        DeleteUser(DeleteUserRequest request)
        {
            return itsImp.DeleteUser(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [Route("get")]
        [HttpPost]
        public GetUserReply 
        GetUser(GetUserRequest request)
        {
            return itsImp.GetUser(request);
        }
    }
}

//  ##########################################################################
