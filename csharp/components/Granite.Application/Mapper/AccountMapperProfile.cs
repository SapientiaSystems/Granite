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
            CreateMap<CreateAccountRequest,CreateExternalAccountRequest>();
            CreateMap<CreateExternalAccountReply,IAccount>();
            CreateMap<IAccount,AccountData>();
        }
    }
}

//  ##########################################################################
