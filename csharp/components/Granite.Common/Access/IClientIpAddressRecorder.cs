//  ##########################################################################
//  # File Name: IClientIpAddressRecorder.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;

namespace Granite.Common.Access
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface IClientIpAddressRecorder
    {
        string ClientIpAddress { get; }
        bool   HasClientIpAddress { get; }

        void
        RecordClientIpAddress();
    }
}

//  ##########################################################################
