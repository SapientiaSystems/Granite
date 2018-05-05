//  ##########################################################################
//  # File Name: IEmailGenerator.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################


using Strata.Foundation.Email;
using System.Collections.Generic;

namespace Granite.Common.Utility
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface IEmailGenerator
    {
        string
        Generate(IDictionary<string,string> parameters);
    }
}

//  ##########################################################################
