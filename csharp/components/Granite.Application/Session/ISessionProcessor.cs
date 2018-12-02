//  ##########################################################################
//  # File Name: ISessionProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Session;

namespace Granite.Application.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface ISessionProcessor
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        LoginReply
        Login(LoginRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        LogoutReply
        Logout(LogoutRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        GetOrLoginReply
        GetOrLogin(GetOrLoginRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        KeepAliveReply
        KeepAlive(KeepAliveRequest request);
    }
}

//  ##########################################################################
