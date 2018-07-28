//  ##########################################################################
//  # File Name: IRegistrationProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Registration;
using System;

namespace Granite.Application.Registration
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IRegistrationProcessor
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        RegisterReply
        Register(RegisterRequest request);
    }
}

//  ##########################################################################
