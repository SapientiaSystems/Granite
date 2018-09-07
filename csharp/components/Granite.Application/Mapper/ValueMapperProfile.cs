//  ##########################################################################
//  # File Name: ValueMapperProfile.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;
using AutoMapper;
using Strata.Foundation.Value;

namespace Granite.Application.Mapper
{
    using Profile = AutoMapper.Profile;

    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// Defines mappings for value objects.
    /// </summary>
    ///  
    public
    class ValueMapperProfile:
        Profile
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        ValueMapperProfile()
        {
            CreateMap<string,HashedString>()
                .ConvertUsing(src => new HashedString(src));

            CreateMap<string,EmailAddress>()
                .ConvertUsing(src => new EmailAddress(src));

             CreateMap<EmailAddress,string>()
                .ConvertUsing(src => src.ToString());

             CreateMap<string,HashedString>()
                .ConvertUsing(src => new HashedString(src));

             CreateMap<string,EmailAddress>()
                .ConvertUsing(src => new EmailAddress(src));

             CreateMap<EmailAddress,string>()
                .ConvertUsing(src => src.ToString());
        }
    }
}

//  ##########################################################################
