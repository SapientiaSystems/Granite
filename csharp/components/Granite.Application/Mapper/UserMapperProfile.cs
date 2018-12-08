//  ##########################################################################
//  # File Name: UserMapperProfile.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.User;
using Granite.ProvidedService.User;

namespace Granite.Application.Mapper
{
    using Profile = AutoMapper.Profile;

    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// Defines mappings for user related objects.
    /// </summary>
    ///  
    public
    class UserMapperProfile:
        Profile
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        UserMapperProfile()
        {
            CreateMap<InitialUserData,IUser>();

            CreateMap<IUser,UserData>()
                .ConvertUsing<UserConverter>();
        }
    }
}

//  ##########################################################################
