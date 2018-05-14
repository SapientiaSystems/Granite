//  ##########################################################################
//  # File Name: UserServiceController.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.User;
using System;
using System.Web.Http;
using System.Web.Http.Cors;
using System.Web.Mvc;
using SystemEx.Injection;

namespace Granite.ServerPlatform.WebApi
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [System.Web.Http.RoutePrefix("v1/user")]
    [RequireHttps]
    [EnableCors("*","*","*")]
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
        UserServiceController(IUserService imp)
        {
            itsImp = imp;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("create")]
        [RequireHttps]
        public CreateUserReply 
        CreateUser(CreateUserRequest request)
        {
            CreateUserReply reply = null;

            request.ReceivedTimestamp = DateTime.UtcNow.Ticks/10000;
            reply = itsImp.CreateUser(request);

            return reply;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("verify")]
        [RequireHttps]
        public VerifyUserReply 
        VerifyUser(VerifyUserRequest request)
        {
            return itsImp.VerifyUser(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("update")]
        public UpdateUserReply 
        UpdateUser(UpdateUserRequest request)
        {
            return itsImp.UpdateUser(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("delete")]
        public DeleteUserReply
        DeleteUser(DeleteUserRequest request)
        {
            return itsImp.DeleteUser(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        [System.Web.Http.Route("get")]
        public GetUserReply 
        GetUser()
        {
            return itsImp.GetUser();
        }
    }
}

//  ##########################################################################
