//  ##########################################################################
//  # File Name: ActiveDirectoryAuthenticationService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.RequiredService.Authentication;
using Microsoft.IdentityModel.Clients.ActiveDirectory;
using System;
using System.Threading;

namespace Granite.ServerPlatform.ActiveDirectory
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class ActiveDirectoryAuthenticationService:
         IAuthenticationService
    {
        private AuthenticationContext itsContext;
        private string                itsResource;
        private string                itsClientId;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        ActiveDirectoryAuthenticationService(string clientId,string tenantId)
        {
            itsContext =
                new AuthenticationContext(
                    "https://login.microsoftonline.com/" + tenantId);
            itsResource = Globals.AadGraphResourceId;
            itsClientId = clientId;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public AuthenticateReply 
        Authenticate(AuthenticateRequest request)
        {
            AuthenticationResult result = null;

            for (int attempt=0;attempt<3;++attempt)
            {
                try
                {
                    result =
                        itsContext.AcquireToken(
                            itsResource,
                            itsClientId,
                            new UserCredential(
                                request.UserName,
                                request.Password));


                    return
                        new AuthenticateReply()
                        {
                            AccessToken = result.AccessToken,
                            AccessTokenType = result.AccessTokenType,
                        };
                }
                catch (AdalException e)
                {
                    if (MustThrow(attempt,e))
                        throw;

                    Thread.Sleep(1000);
                }
            }

            throw new InvalidOperationException();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private bool 
        MustThrow(int attempt,AdalException e)
        {
            return 
                attempt >= 3 || 
                e.ErrorCode != "temporarily_unavailable";
        }

    }
}

//  ##########################################################################
