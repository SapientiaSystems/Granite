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
        public IClientIpAddressRecorder Recorder { get; set; }

        private ISessionRepository      itsSessions;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        Authorizer(
            ISessionRepository       sessions,
            IClientIpAddressRecorder recorder)
        {
            itsSessions = sessions;
            Recorder = recorder;
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

            ValidateClientIpAddress(session);

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
        private void
        ValidateClientIpAddress(ISession session)
        {
            if (!session.HasClientIpAddress)
                return;

            if (!Recorder.HasClientIpAddress)
                throw
                    new AuthorizationException(
                        "Authorization failed: " +
                        "could not obtain client ip address from request.");

            if (session.ClientIpAddress != Recorder.ClientIpAddress)
                throw
                    new AuthorizationException(
                        string.Format(
                            "Authorization failed: " +
                            "request's client ip address {0} " +
                            "does not match session's client ip address {1}",
                            Recorder.ClientIpAddress,
                            session.ClientIpAddress));
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
