//  ##########################################################################
//  # File Name: Authorizer.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Access;
using Granite.Domain.Session;
using System.Text;
using SystemEx.Injection;

namespace Granite.Application.Access
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class Authorizer:
        IAuthorizer
    {
        private ISessionRepository itsSessions;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        Authorizer(ISessionRepository sessions)
        {
            itsSessions = sessions;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public void 
        AuthorizeUser(string accessToken,string[] roles)
        {
            ISession session =
                itsSessions.GetUniqueWithAccessToken(accessToken);

            if (session == null)
                throw
                    new AuthorizationException(
                        "Authorization failed: access token is invalid.");

            if (!session.IsOpen)
                throw
                    new AuthorizationException(
                        "Authorization failed: session has been closed.");

            if (roles.Length > 0)
            {
                bool matchedRole = false;

                foreach (string role in roles)
                    if (session.User.IsInRole(role))
                        matchedRole = true;

                if (!matchedRole)
                    throw
                        new AuthorizationException(
                            "Authorization failed: " +
                            "user not in any of the specified roles: " +
                            ToString(roles));
            }
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private string
        ToString(string[] input)
        {
            StringBuilder builder = null;

            if (input.Length == 0)
                return string.Empty;

            builder = new StringBuilder();
            builder.Append(input[0]);

            for (int i = 1;i < input.Length;++i)
                builder
                    .Append(',')
                    .Append(input[i]);

            return builder.ToString();
        }
    }
}

//  ##########################################################################
