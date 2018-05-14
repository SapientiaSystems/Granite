//  ##########################################################################
//  # File Name: IUserService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

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
        CreateUserReply
        CreateUser(CreateUserRequest request);

        [OperationContract]
        VerifyUserReply
        VerifyUser(VerifyUserRequest request);

        [OperationContract]
        UpdateUserReply
        UpdateUser(UpdateUserRequest request);

        [OperationContract]
        DeleteUserReply
        DeleteUser(DeleteUserRequest request);

        [OperationContract]
        GetUserReply
        GetUser();
    }
}

//  ##########################################################################
