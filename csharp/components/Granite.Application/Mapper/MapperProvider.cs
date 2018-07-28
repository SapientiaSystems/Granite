//  ##########################################################################
//  # File Name: MapperProvider.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using AutoMapper;
using Granite.Domain.Payment;
using Granite.Domain.Profile;
using Granite.Domain.Session;
using Granite.Domain.User;
using Granite.ProvidedService.Payment;
using Granite.ProvidedService.Profile;
using Granite.ProvidedService.Registration;
using Granite.ProvidedService.Session;
using Granite.ProvidedService.User;
using Granite.RequiredService.Payment;
using Strata.Foundation.Value;
using SystemEx.Injection;

namespace Granite.Application.Mapper
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class MapperProvider:
        IProvider<IMapper>
    {
        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public IMapper 
        Get()
        {
            return
                new MapperConfiguration(
                    config =>
                    {
                        CreateValueMappings(config);
                        CreateUserMappings(config);
                        CreateSessionMappings(config);
                        CreateProfileMappings(config);
                        CreatePlanMappings(config);
                        CreateAccountMappings(config);
                        CreateSubscriptionMappings(config);
                        CreateRegistrationMappings(config);
                    }).CreateMapper();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual void
        CreateValueMappings(IMapperConfigurationExpression config)
        {
            config
                .CreateMap<string,HashedString>()
                .ConvertUsing(src => new HashedString(src));

            config
                .CreateMap<string,EmailAddress>()
                .ConvertUsing(src => new EmailAddress(src));

            config
                .CreateMap<EmailAddress,string>()
                .ConvertUsing(src => src.ToString());
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual void
        CreateUserMappings(IMapperConfigurationExpression config)
        {
            config.CreateMap<CreateUserData,IUser>();

            config
                .CreateMap<IUser,UserData>()
                .ConvertUsing<UserConverter>();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual void
        CreateSessionMappings(IMapperConfigurationExpression config)
        {
            config
                .CreateMap<ISession,SessionData>()
                .ForMember(
                    dest => dest.SessionId,
                    opt => opt.MapFrom(src => src.PrimaryId));
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual void 
        CreateProfileMappings(IMapperConfigurationExpression config)
        {
            config
                .CreateMap<CreateOrUpdateProfileData,IProfile>()
                .ReverseMap();
            config.CreateMap<IUser,IProfile>();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual void
        CreatePlanMappings(IMapperConfigurationExpression config)
        {
            config
                .CreateMap<
                    CreatePlanRequest,
                    CreateExternalPlanRequest>();
            config.CreateMap<CreateExternalPlanReply,IPlan>();
            config.CreateMap<CreateExternalPlanReply,CreatePlanReply>();
            config.CreateMap<IPlan,PlanData>();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual void
        CreateAccountMappings(IMapperConfigurationExpression config)
        {
            config
                .CreateMap<
                    CreateAccountRequest,
                    CreateExternalAccountRequest>();
            config.CreateMap<CreateExternalAccountReply,IAccount>();
            config.CreateMap<IAccount,AccountData>();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual void
        CreateSubscriptionMappings(IMapperConfigurationExpression config)
        {
            config
                .CreateMap<
                    CreateSubscriptionRequest,
                    CreateExternalSubscriptionRequest>();
            config.CreateMap<CreateExternalSubscriptionReply,ISubscription>();
            config.CreateMap<ISubscription,SubscriptionData>();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual void
        CreateRegistrationMappings(IMapperConfigurationExpression config)
        {
            config
                .CreateMap<RegisterRequest,CreateUserRequest>();

            config
                .CreateMap<CreateUserReply,RegisterReply>()
                .ForMember(
                    dest => dest.SuccessMessage,
                    opt => opt.ResolveUsing(
                        new MessageAppender(r => r.SuccessMessage)))
                .ForMember(
                    dest => dest.FailureMessage,
                    opt => opt.ResolveUsing(
                        new MessageAppender(r => r.FailureMessage)));

            config
                .CreateMap<
                    RegisterRequest,
                    CreateProfileRequest>();

            config
                .CreateMap<
                    CreateProfileReply,
                    RegisterReply>()
                    .ForMember(
                        dest => dest.SuccessMessage,
                        opt => opt.ResolveUsing(
                            new MessageAppender(r => r.SuccessMessage)))
                    .ForMember(
                        dest => dest.FailureMessage,
                        opt => opt.ResolveUsing(
                            new MessageAppender(r => r.FailureMessage)));

            config
                .CreateMap<
                    CreateAccountReply,
                    RegisterReply>()
                    .ForMember(
                        dest => dest.SuccessMessage,
                        opt => opt.ResolveUsing(
                            new MessageAppender(r => r.SuccessMessage)))
                    .ForMember(
                        dest => dest.FailureMessage,
                        opt => opt.ResolveUsing(
                            new MessageAppender(r => r.FailureMessage)));

            config
                .CreateMap<
                    RegisterRequest,
                    CreateSubscriptionRequest>();


            config
                .CreateMap<
                    CreateSubscriptionReply,
                    RegisterReply>()
                    .ForMember(
                        dest => dest.SuccessMessage,
                        opt => opt.ResolveUsing(
                            new MessageAppender(r => r.SuccessMessage)))
                    .ForMember(
                        dest => dest.FailureMessage,
                        opt => opt.ResolveUsing(
                            new MessageAppender(r => r.FailureMessage)));
        }
    }
}

//  ##########################################################################
