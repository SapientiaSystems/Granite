//  ##########################################################################
//  # File Name: MessageAppender.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using AutoMapper;
using Granite.ProvidedService.Transfer;
using System;

namespace Granite.Application.Mapper
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class MessageAppender:
        IValueResolver<GraniteReply,GraniteReply,string>
    {
        private Func<GraniteReply,string> itsProperty;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///
        public
        MessageAppender(Func<GraniteReply,string> property)
        {
            itsProperty = property;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///
        public string 
        Resolve(
            GraniteReply      source,
            GraniteReply      destination,
            string            member,
            ResolutionContext context)
        {
            string src  = itsProperty.Invoke(source);
            string dest = itsProperty.Invoke(destination);

            if (!string.IsNullOrEmpty(dest))
                if (!string.IsNullOrEmpty(src))
                    return dest + "#" + src;
                else
                    return dest;

            return src;
        }
    }
}

//  ##########################################################################
