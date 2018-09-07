//  ##########################################################################
//  # File Name: RegistrationMapperProfile.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Payment;
using Granite.ProvidedService.Profile;
using Granite.ProvidedService.Registration;
using Granite.ProvidedService.User;

namespace Granite.Application.Mapper
{
    using Profile = AutoMapper.Profile;

    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// Defines mappings for registration related objects.
    /// </summary>
    ///  
    public
    class RegistrationMapperProfile:
        Profile
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        RegistrationMapperProfile()
        {
            CreateMap<RegisterRequest,CreateUserRequest>();

            CreateMap<CreateUserReply,RegisterReply>()
                .ForMember(
                    dest => dest.SuccessMessage,
                    opt => opt.ResolveUsing(
                        new MessageAppender(r => r.SuccessMessage)))
                .ForMember(
                    dest => dest.FailureMessage,
                    opt => opt.ResolveUsing(
                        new MessageAppender(r => r.FailureMessage)));

            CreateMap<RegisterRequest,CreateProfileRequest>();

            CreateMap<CreateProfileReply,RegisterReply>()
                .ForMember(
                    dest => dest.SuccessMessage,
                    opt => opt.ResolveUsing(
                        new MessageAppender(r => r.SuccessMessage)))
                .ForMember(
                    dest => dest.FailureMessage,
                    opt => opt.ResolveUsing(
                        new MessageAppender(r => r.FailureMessage)));

            CreateMap<CreateAccountReply,RegisterReply>()
                .ForMember(
                    dest => dest.SuccessMessage,
                    opt => opt.ResolveUsing(
                        new MessageAppender(r => r.SuccessMessage)))
                .ForMember(
                    dest => dest.FailureMessage,
                    opt => opt.ResolveUsing(
                        new MessageAppender(r => r.FailureMessage)));

            CreateMap<RegisterRequest,CreateSubscriptionRequest>();

            CreateMap<CreateSubscriptionReply,RegisterReply>()
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
