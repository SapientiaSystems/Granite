//  ##########################################################################
//  # File Name: SubscriptionMapperProfile.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Payment;
using Granite.ProvidedService.Payment;
using Granite.RequiredService.Payment;

namespace Granite.Application.Mapper
{
    using Profile = AutoMapper.Profile;

    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// Defines mappings for subscription related objects.
    /// </summary>
    ///  
    public
    class SubscriptionMapperProfile:
        Profile
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        SubscriptionMapperProfile()
        {
            CreateMap<
                CreateSubscriptionRequest,
                CreateExternalSubscriptionRequest>();
            CreateMap<CreateExternalSubscriptionReply,ISubscription>();
            CreateMap<ISubscription,SubscriptionData>();
        }
    }
}

//  ##########################################################################
