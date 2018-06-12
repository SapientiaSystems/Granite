//  ##########################################################################
//  # File Name: UserService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.User;
using SystemEx.Injection;

namespace Granite.Application.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class UserService:
        IUserService
    {
        private IUserProcessor itsProcessor;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public 
        UserService(IUserProcessor processor)
        {
            itsProcessor = processor;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public CreateUserReply 
        CreateUser(CreateUserRequest request)
        {
            return itsProcessor.CreateUser(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public VerifyUserReply
        VerifyUser(VerifyUserRequest request)
        {
            return itsProcessor.VerifyUser(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public UpdateUserReply
        UpdateUser(UpdateUserRequest request)
        {
            return itsProcessor.UpdateUser(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public DeleteUserReply 
        DeleteUser(DeleteUserRequest request)
        {
            return itsProcessor.DeleteUser(request);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public GetUserReply
        GetUser(GetUserRequest request)
        {
            return itsProcessor.GetUser(request);
        }
    }
}

//  ##########################################################################
