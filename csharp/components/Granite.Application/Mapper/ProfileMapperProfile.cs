//  ##########################################################################
//  # File Name: ProfileMapperProfile.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Profile;
using Granite.Domain.User;
using Granite.ProvidedService.Profile;

namespace Granite.Application.Mapper
{
    using Profile = AutoMapper.Profile;

    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// Defines mappings for profile related objects.
    /// </summary>
    ///  
    public
    class ProfileMapperProfile:
        Profile
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        ProfileMapperProfile()
        {
            CreateMap<CreateOrUpdateProfileData,IProfile>()
                .ReverseMap();

            CreateMap<IUser,IProfile>();
        }
    }
}

//  ##########################################################################
