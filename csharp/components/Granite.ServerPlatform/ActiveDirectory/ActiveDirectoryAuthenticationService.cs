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
        ActiveDirectoryAuthenticationService(string authority)
        {
            itsContext = new AuthenticationContext(authority);
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
                                request.Username,
                                request.Password));


                    return
                        new AuthenticateReply()
                        {
                            AccessToken = result.AccessToken,
                            AccessTokenType = result.AccessTokenType
                        };
                }
                catch (AdalException e)
                {
                    if (MustThrow(attempt,e))
                        throw;

                        Thread.Sleep(3000);
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
