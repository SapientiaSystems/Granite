//  ##########################################################################
//  # File Name: IRegistrationService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Access;
using System.ServiceModel;

namespace Granite.ProvidedService.Registration
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [ServiceContract]
    public
    interface IRegistrationService
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        [Authorize(Roles = "GraniteGuest")]
        RegisterReply
        Register(RegisterRequest request);
    }
}

//  ##########################################################################
