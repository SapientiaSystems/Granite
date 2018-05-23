//  ##########################################################################
//  # File Name: ISessionService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.ServiceModel;

namespace Granite.ProvidedService.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [ServiceContract]
    public
    interface ISessionService
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        LoginReply
        Login(LoginRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        LogoutReply
        Logout(LogoutRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        KeepAliveReply
        KeepAlive(KeepAliveRequest request);
    }
}

//  ##########################################################################
