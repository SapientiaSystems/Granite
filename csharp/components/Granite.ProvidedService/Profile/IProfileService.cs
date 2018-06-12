//  ##########################################################################
//  # File Name: IProfileService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Access;
using System.ServiceModel;

namespace Granite.ProvidedService.Profile
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [ServiceContract]
    public
    interface IProfileService
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        [Authorize(Roles = "GraniteUser")]
        CreateProfileReply
        CreateProfile(CreateProfileRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        [Authorize(Roles = "GraniteUser")]
        UpdateProfileReply
        UpdateProfile(UpdateProfileRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        [Authorize(Roles = "GraniteAdministrator")]
        DeleteProfileReply
        DeleteProfile(DeleteProfileRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [OperationContract]
        [Authorize(Roles = "GraniteUser")]
        GetProfileReply
        GetProfile(GetProfileRequest request);
    }
}

//  ##########################################################################
