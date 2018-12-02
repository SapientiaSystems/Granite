//  ##########################################################################
//  # File Name: SessionMapperProfile.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Session;
using Granite.ProvidedService.Session;

namespace Granite.Application.Mapper
{
    using Profile = AutoMapper.Profile;

    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// Defines mappings for session related objects.
    /// </summary>
    ///  
    public
    class SessionMapperProfile:
        Profile
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        SessionMapperProfile()
        {
            CreateMap<ISession,SessionData>()
                .ForMember(
                    dest => dest.SessionId,
                    opt => opt.MapFrom(src => src.PrimaryId));

            CreateMap<GetOrLoginRequest,LoginRequest>();

            CreateMap<LoginReply,GetOrLoginReply>()
                .ForMember(
                    dest => dest.Session,
                    opt => opt.MapFrom(src => src.NewSession));
        }
    }
}

//  ##########################################################################
