//  ##########################################################################
//  # File Name: VerificationNotice.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;
using System.Collections.Generic;

namespace Granite.Common.Verification
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class VerificationNotice
    {
        public MessageKind                MessageType { get; set; }
        public string                     To { get; set; }
        public string                     From { get; set; }
        public IDictionary<string,string> Parameters { get; set; }
    }
}

//  ##########################################################################
