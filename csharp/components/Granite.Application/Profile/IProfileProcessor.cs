//  ##########################################################################
//  # File Name: IProfileProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Profile;

namespace Granite.Application.Profile
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface IProfileProcessor
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        CreateProfileReply
        CreateProfile(CreateProfileRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        UpdateProfileReply
        UpdateProfile(UpdateProfileRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        DeleteProfileReply
        DeleteProfile(DeleteProfileRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        GetProfileReply
        GetProfile(GetProfileRequest request);
    }
}

//  ##########################################################################
