//  ##########################################################################
//  # File Name: IUserProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.User;

namespace Granite.Application.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IUserProcessor
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        CreateUserReply
        CreateUser(CreateUserRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        VerifyUserReply
        VerifyUser(VerifyUserRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        UpdateUserReply
        UpdateUser(UpdateUserRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        DeleteUserReply
        DeleteUser(DeleteUserRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        GetUserReply
        GetUser(GetUserRequest request);
    }
}

//  ##########################################################################
