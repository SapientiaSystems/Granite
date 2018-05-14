//  ##########################################################################
//  # File Name: IVerificationMessageGenerator.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################


using Strata.Foundation.Email;
using System.Collections.Generic;

namespace Granite.Common.Utility
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IVerificationMessageGenerator
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        string
        GenerateForNewUserEmail(IDictionary<string,string> parameters);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        string
        GenerateForUpdatedUserEmail(IDictionary<string,string> parameters);
    }
}

//  ##########################################################################
