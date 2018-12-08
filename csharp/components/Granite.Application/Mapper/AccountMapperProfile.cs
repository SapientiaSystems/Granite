//  ##########################################################################
//  # File Name: AccountMapperProfile.cs
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
    /// Defines mappings for account related objects.
    /// </summary>
    ///  
    public
    class AccountMapperProfile:
        Profile
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        AccountMapperProfile()
        {
            // Create
            CreateMap<CreateAccountRequest,CreateExternalAccountRequest>();
            CreateMap<CreateExternalAccountReply,IAccount>()
                .As<StandardAccount>();
            CreateMap<CreateExternalAccountReply,CreateAccountReply>()
                .ForPath(
                    dest => dest.NewAccount.CreditCard,
                    opt => opt.MapFrom( src => src.CreditCard ) );
            CreateMap<IAccount,CreateAccountReply>()
                .ForMember(
                    dest => dest.NewAccount,
                    opt => opt.MapFrom(src => src));

            // Update
            CreateMap<UpdateAccountRequest,UpdateExternalAccountRequest>()
                .ForMember(
                    dest => dest.Description,
                    opt => opt.MapFrom(src => src.AccountToUpdate.Description))
                .ForMember(
                    dest => dest.Email,
                    opt => opt.MapFrom(src => src.AccountToUpdate.Email))
                .ForMember(
                    dest => dest.CreditCardToCreate,
                    opt => opt.MapFrom(src => src.AccountToUpdate.CreditCardToCreate))
                .ForMember(
                    dest => dest.CreditCardToUpdate,
                    opt => opt.MapFrom(src => src.AccountToUpdate.CreditCardToUpdate))
                .ForMember(
                    dest => dest.ReplaceCreditCard,
                    opt => opt.MapFrom( src => src.AccountToUpdate.ReplaceCreditCard));

            CreateMap<IAccount,UpdateExternalAccountRequest>();
            CreateMap<UpdateExternalAccountReply,IAccount>()
                .As<StandardAccount>();
            CreateMap<UpdateExternalAccountReply,UpdateAccountReply>()
                .ForPath(
                    dest => dest.UpdatedAccount.CreditCard,
                    opt => opt.MapFrom(src => src.CreditCard));
            CreateMap<IAccount,UpdateAccountReply>()
                .ForMember(
                    dest => dest.UpdatedAccount,
                    opt => opt.MapFrom(src => src));            

            // Delete
            CreateMap<DeleteAccountRequest,DeleteExternalAccountRequest>();
            CreateMap<DeleteExternalAccountReply,DeleteAccountReply>();

            // Get
            CreateMap<GetAccountRequest,GetExternalAccountRequest>();
            CreateMap<IAccount,GetExternalAccountRequest>();
            CreateMap<GetExternalAccountReply,IAccount>();
            CreateMap<GetExternalAccountReply,GetAccountReply>()
                .ForPath(
                    dest => dest.RequestedAccount.CreditCard,
                    opt => opt.MapFrom(src => src.CreditCard));
            CreateMap<IAccount,GetAccountReply>()
                .ForMember(
                    dest => dest.RequestedAccount,
                    opt => opt.MapFrom(src => src));

            // Common
            CreateMap<IAccount,AccountData>();
        }
    }
}

//  ##########################################################################
