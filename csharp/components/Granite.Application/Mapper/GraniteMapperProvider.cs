//  ##########################################################################
//  # File Name: GraniteMapperProvider.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using AutoMapper;
using Ninject.Activation;
using System.Collections.Generic;

namespace Granite.Application.Mapper
{
    using Profile = AutoMapper.Profile;

    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// Provides the mapper that translates Granite transfer objects to 
    /// Granite domain objects and vice versa.
    /// </summary>
    ///  
    public
    class GraniteMapperProvider:
        Provider<IMapper>
    {
        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        protected override IMapper 
        CreateInstance(IContext context)
        {
            return
                new MapperConfiguration(
                    config =>
                    {
                        foreach (Profile profile in GetMapperProfiles())
                            config.AddProfile(profile);

                    }).CreateMapper();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual ICollection<Profile>
        GetMapperProfiles()
        {
            return
                new List<Profile>()
                {
                    new ValueMapperProfile(),
                    new UserMapperProfile(),
                    new SessionMapperProfile(),
                    new ProfileMapperProfile(),
                    new PlanMapperProfile(),
                    new AccountMapperProfile(),
                    new SubscriptionMapperProfile(),
                    new RegistrationMapperProfile()
                };
        }
    }
}

//  ##########################################################################
