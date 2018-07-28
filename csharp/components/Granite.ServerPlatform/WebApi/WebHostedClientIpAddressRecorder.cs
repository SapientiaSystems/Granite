//  ##########################################################################
//  # File Name: WebHostedClientIpAddressRecorder.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Access;
using System.Linq;
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
                return
                    sourceIpAddress
                        .Split(',')
                        .Last()
                        .Trim();

            return
                HttpContext
                    .Current
                    .Request
                    .ServerVariables["REMOTE_ADDR"];

        }
    }
}

//  ##########################################################################
