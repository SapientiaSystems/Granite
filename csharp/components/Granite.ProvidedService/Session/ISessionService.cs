//  ##########################################################################
//  # File Name: ISessionService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Access;
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
        [Authorize(Roles="GraniteGuest")]
        LogoutReply
        Logout(LogoutRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        [Authorize(Roles = "GraniteGuest")]
        KeepAliveReply
        KeepAlive(KeepAliveRequest request);
    }
}

//  ##########################################################################
