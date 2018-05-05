//  ##########################################################################
//  # File Name: IEmailSender.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.Collections.Generic;

namespace Granite.Common.Utility
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface IEmailSender
    {
        void
        GenerateAndSend(
            string                     from,
            string                     to,
            IDictionary<string,string> parameters);
    }
}

//  ##########################################################################
