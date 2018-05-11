//  ##########################################################################
//  # File Name: MockEmailSender.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;
using System.Collections.Generic;

namespace Granite.Common.Utility
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class MockEmailSender:
        IEmailSender
    {
        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public void 
        GenerateAndSend(
            string                     from,
            string                     to,
            IDictionary<string,string> parameters)
        {
        }
    }
}

//  ##########################################################################
