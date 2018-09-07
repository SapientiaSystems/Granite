//  ##########################################################################
//  # File Name: PlanMapperProfile.cs
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
    /// Defines mappings for plan related objects.
    /// </summary>
    ///  
    public
    class PlanMapperProfile:
        Profile
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        PlanMapperProfile()
        {
            CreateMap<CreatePlanRequest,CreateExternalPlanRequest>();
            CreateMap<CreateExternalPlanReply,IPlan>();
            CreateMap<CreateExternalPlanReply,CreatePlanReply>();
            CreateMap<IPlan,PlanData>();
        }
    }
}

//  ##########################################################################
