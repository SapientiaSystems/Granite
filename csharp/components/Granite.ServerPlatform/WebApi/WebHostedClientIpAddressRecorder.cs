//  ##########################################################################
//  # File Name: WebHostedClientIpAddressRecorder.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Access;
using System.Linq;
using System.Net;
using System.Web;

namespace Granite.ServerPlatform.WebApi
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class WebHostedClientIpAddressRecorder:
        IClientIpAddressRecorder
    {
        public string ClientIpAddress { get; protected set; }
        public bool   HasClientIpAddress { get; protected set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        WebHostedClientIpAddressRecorder()
        {
            ClientIpAddress = null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void 
        RecordClientIpAddress()
        {
            ClientIpAddress    = GetClientIpAddressFromCurrentRequest();
            HasClientIpAddress = !string.IsNullOrEmpty(ClientIpAddress);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual string
        GetClientIpAddressFromCurrentRequest()
        {
            string sourceIpAddress = null;

            if (HttpContext.Current == null)
                return null;

            sourceIpAddress = 
                HttpContext
                    .Current
                    .Request
                    .ServerVariables["HTTP_X_FORWARDED_FOR"];

            if (!string.IsNullOrEmpty(sourceIpAddress))
                sourceIpAddress =
                    sourceIpAddress
                        .Split(',')
                        .Last()
                        .Trim();
            else
                sourceIpAddress =
                    HttpContext
                        .Current
                        .Request
                        .ServerVariables["REMOTE_ADDR"];

            if (string.IsNullOrEmpty(sourceIpAddress))
                return null;

            if (sourceIpAddress.Equals("::1"))
                sourceIpAddress = "127.0.0.1";

            return
                sourceIpAddress
                    .Split(':')
                    .First()
                    .Trim();
        }
    }
}

//  ##########################################################################
