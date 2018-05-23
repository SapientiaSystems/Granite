//  ##########################################################################
//  # File Name: Authorized.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;
using System.Web.Http;
using System.Web.Http.Controllers;

namespace Granite.Common.Authorization
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class Authorized:
        AuthorizeAttribute
    {
        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        protected override bool
        IsAuthorized(HttpActionContext context)
        {
            return false;
        }
    }
}

//  ##########################################################################
