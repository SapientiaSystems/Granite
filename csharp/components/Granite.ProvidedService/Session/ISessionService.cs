//  ##########################################################################
//  # File Name: ISessionService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.ServiceModel;

namespace Granite.ProvidedService.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [ServiceContract]
    public
    interface ISessionService
    {
        [OperationContract]
        LoginReply
        Login(LoginRequest request);

        [OperationContract]
        LogoutReply
        Logout(LogoutRequest request);
    }
}

//  ##########################################################################
