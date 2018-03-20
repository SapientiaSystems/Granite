//  ##########################################################################
//  # File Name: ActiveDirectoryAuthenticationService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.RequiredService.Authentication;
using Microsoft.IdentityModel.Clients.ActiveDirectory;
using System;
using System.Threading;
using System.Threading.Tasks;

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
            Task<AuthenticationResult> result = null;

            for (int attempt=0;attempt<3;++attempt)
            {
                try
                {
                    result =
                        itsContext.AcquireTokenAsync(
                            itsResource,
                            itsClientId,
                            new UserPasswordCredential(
                                request.Username,
                                request.Password));

                    result.Wait();

                    return
                        new AuthenticateReply()
                        {
                            AccessToken = result.Result.AccessToken,
                            AccessTokenType = result.Result.AccessTokenType
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
