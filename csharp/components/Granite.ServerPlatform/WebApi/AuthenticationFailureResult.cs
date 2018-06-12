//  ##########################################################################
//  # File Name: AuthenticationFailureResult.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.Net;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using System.Web.Http;

namespace Granite.ServerPlatform.WebApi
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class AuthenticationFailureResult: 
        IHttpActionResult
    {
        public string             ReasonPhrase { get; private set; }
        public HttpRequestMessage Request { get; private set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        AuthenticationFailureResult(
            string             reasonPhrase,
            HttpRequestMessage request)
        {
            ReasonPhrase = reasonPhrase;
            Request = request;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public Task<HttpResponseMessage> 
        ExecuteAsync(CancellationToken cancellationToken)
        {
            return Task.FromResult(Execute());
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private HttpResponseMessage 
        Execute()
        {
            HttpResponseMessage response = 
                new HttpResponseMessage(HttpStatusCode.Unauthorized);

            response.RequestMessage = Request;
            response.ReasonPhrase = ReasonPhrase;
            return response;
        }
    }
}

//  ##########################################################################
