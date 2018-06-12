//  ##########################################################################
//  # File Name: RequireHttps.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;
using System.Net;
using System.Net.Http;
using System.Web.Http.Controllers;
using System.Web.Http.Filters;

namespace Granite.ServerPlatform.WebApi
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class RequireHttps: 
        AuthorizationFilterAttribute
    {
        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override void 
        OnAuthorization(HttpActionContext actionContext)
        {
            if (actionContext == null)
                throw new ArgumentNullException("actionContext");

            if (actionContext.Request.RequestUri.Scheme != Uri.UriSchemeHttps)
                HandleNonHttpsRequest(actionContext);
            else
                base.OnAuthorization(actionContext);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual void 
        HandleNonHttpsRequest(HttpActionContext actionContext)
        {
            actionContext.Response = 
                new HttpResponseMessage(HttpStatusCode.Forbidden);
            actionContext.Response.ReasonPhrase = "SSL Required";
        }
    }
}

//  ##########################################################################
