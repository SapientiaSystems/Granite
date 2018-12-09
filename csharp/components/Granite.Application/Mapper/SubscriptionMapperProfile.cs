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
            // Create
            CreateMap<
                CreateSubscriptionRequest,
                CreateExternalSubscriptionRequest>();
            CreateMap<
                CreateExternalSubscriptionReply,
                CreateSubscriptionReply>();
            CreateMap<CreateExternalSubscriptionReply,ISubscription>()
                .As<StandardSubscription>();
            CreateMap<ISubscription,CreateSubscriptionReply>()
                .ForMember(
                    dest => dest.NewSubscription,
                    opt => opt.MapFrom(src => src));
            CreateMap<IAccount,CreateExternalSubscriptionRequest>();

            // Update
            CreateMap<
                UpdateSubscriptionRequest,
                UpdateExternalSubscriptionRequest>();
            CreateMap<ISubscription,UpdateExternalSubscriptionRequest>();
            CreateMap<
                UpdateExternalSubscriptionReply,
                UpdateSubscriptionReply>();
            CreateMap<UpdateExternalSubscriptionReply,ISubscription>()
                .As<StandardSubscription>();
            CreateMap<ISubscription,UpdateSubscriptionReply>()
                .ForMember(
                    dest => dest.UpdatedSubscription,
                    opt => opt.MapFrom(src => src));

            // Delete
            CreateMap<
                DeleteSubscriptionRequest,
                DeleteExternalSubscriptionRequest>();
            CreateMap<
                DeleteExternalSubscriptionReply,
                DeleteSubscriptionReply>();

            // Get
            CreateMap<GetSubscriptionRequest,GetSubscriptionReply>();
            CreateMap<ISubscription,GetSubscriptionReply>()
                .ForMember(
                    dest => dest.RequestedSubscription,
                    opt => opt.MapFrom(src => src));
            CreateMap<IPlan,GetSubscriptionReply>()
                .ForMember(
                    dest => dest.RequestedSubscription,
                    opt => opt.MapFrom(src => src));

            // Common
            CreateMap<InitialSubscriptionData,ISubscription>()
                .As<StandardSubscription>();
            CreateMap<ISubscription,SubscriptionData>();
            CreateMap<IPlan,SubscriptionData>();

        }
    }
}

//  ##########################################################################
