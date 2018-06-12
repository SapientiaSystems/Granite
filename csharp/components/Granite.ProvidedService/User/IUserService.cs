//  ##########################################################################
//  # File Name: IUserService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Access;
using System.ServiceModel;

namespace Granite.ProvidedService.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [ServiceContract]
    public
    interface IUserService
    {
        [OperationContract]
        [Authorize(Roles="GraniteGuest")]
        CreateUserReply
        CreateUser(CreateUserRequest request);

        [OperationContract]
        [Authorize(Roles="GraniteGuest")]
        VerifyUserReply
        VerifyUser(VerifyUserRequest request);

        [OperationContract]
        [Authorize(Roles="GraniteUser")]
        UpdateUserReply
        UpdateUser(UpdateUserRequest request);

        [OperationContract]
        [Authorize(Roles="GraniteAdministrator")]
        DeleteUserReply
        DeleteUser(DeleteUserRequest request);

        [OperationContract]
        [Authorize]
        GetUserReply
        GetUser(GetUserRequest request);
    }
}

//  ##########################################################################
