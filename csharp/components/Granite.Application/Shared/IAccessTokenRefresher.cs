//  ##########################################################################
//  # File Name: IAccessTokenRefresher.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Transfer;

namespace Granite.Application.Shared
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IAccessTokenRefresher
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        void
        RefreshIfNeeded(GraniteReply reply);
    }
}

//  ##########################################################################
