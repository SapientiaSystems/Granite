//  ##########################################################################
//  # File Name: IAuthenticationService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.ServiceModel;

namespace Granite.RequiredService.Authentication
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [ServiceContract]
    public
    interface IAuthenticationService
    {
        AuthenticateReply
        Authenticate(AuthenticateRequest request);
    }
}

//  ##########################################################################
