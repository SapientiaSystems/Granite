//  ##########################################################################
//  # File Name: IExternalUserService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################


namespace Granite.RequiredService.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IExternalUserService
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        CreateExternalUserReply
        CreateExternalUser(CreateExternalUserRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        UpdateExternalUserReply
        UpdateExternalUser(UpdateExternalUserRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        DeleteExternalUserReply
        DeleteExternalUser(DeleteExternalUserRequest request);
    }
}

//  ##########################################################################
