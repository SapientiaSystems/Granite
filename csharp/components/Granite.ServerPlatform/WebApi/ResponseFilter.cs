//  ##########################################################################
//  # File Name: ResponseFilter.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.Web.Http.Filters;

namespace Granite.ServerPlatform.WebApi
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class ResponseFilter:
        ActionFilterAttribute
    {
        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override void 
        OnActionExecuted(HttpActionExecutedContext context)
        {
            context
                .Response
                .Headers
                .Add("GraniteToken",string.Empty);
        }
    }
}

//  ##########################################################################
