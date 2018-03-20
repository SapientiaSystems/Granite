//  ##########################################################################
//  # File Name: IUserIdentityService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System.ServiceModel;

namespace Granite.ProvidedService.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [ServiceContract]
    public
    interface IUserIdentityService
    {
        [OperationContract]
        StartRegistrationReply
        StartRegistration(StartRegistrationRequest request);

        [OperationContract]
        FinishRegistrationReply
        FinishRegistration(FinishRegistrationRequest request);

        [OperationContract]
        UpdateReply
        Update(UpdateRequest request);

        [OperationContract]
        RemoveReply
        Remove(RemoveRequest request);

        [OperationContract]
        GetCurrentReply
        GetCurrent();
    }
}

//  ##########################################################################
