//  ##########################################################################
//  # File Name: ContentNegotiator.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Net.Http.Formatting;
using System.Net.Http.Headers;

namespace Granite.ServerPlatform.WebApi
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// $comments$
    /// </summary>
    ///  
    public
    class ContentNegotiator:
        DefaultContentNegotiator
    {

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Creates a new <c>ContentNegotiator</c> instance.
        /// </summary>
        /// 
        public
        ContentNegotiator()
        {
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override ContentNegotiationResult
        Negotiate(
            Type type,
            HttpRequestMessage request,
            IEnumerable<MediaTypeFormatter> formatters)
        {
            MediaTypeHeaderValue contentType =
                request
                    .Content
                    .Headers
                    .ContentType;

            if (contentType == null || contentType.MediaType == null)
                return base.Negotiate(type,request,formatters);

            if (contentType.MediaType.Equals("application/xml"))
                return
                    new ContentNegotiationResult(
                        new XmlMediaTypeFormatter(),
                        new MediaTypeHeaderValue("application/xml"));

            if (contentType.MediaType.Equals("application/json"))
                return
                    new ContentNegotiationResult(
                        new JsonMediaTypeFormatter(),
                        new MediaTypeHeaderValue("application/json"));


            return base.Negotiate(type,request,formatters);
        }
    }
}

//  ##########################################################################
