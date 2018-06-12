//  ##########################################################################
//  # File Name: ProfileRepository.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Domain.NamedQuery;
using Strata.Domain.Repository;
using Strata.Domain.UnitOfWork;
using System;
using SystemEx.Injection;

namespace Granite.Domain.Profile
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class ProfileRepository:
        AbstractRepository<Guid,IProfile>,
        IProfileRepository
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        ProfileRepository(IUnitOfWorkProvider provider):
            base(provider) {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual IProfile
        GetUniqueFor(IUserIdProvider provider)
        {
            INamedQuery<IProfile> query =
                GetNamedQuery("Profile.GetUniqueFor");

            query.SetInput("userId",provider.UserId);
            return query.GetUnique();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual IProfile 
        GetUniqueWithUserName(string userName)
        {
            INamedQuery<IProfile> query = 
                GetNamedQuery("Profile.GetUniqueWithUserName");

            query.SetInput("userName",userName);
            return query.GetUnique();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual bool
        HasUniqueFor(IUserIdProvider provider)
        {
            INamedQuery<IProfile> query =
                GetNamedQuery("Profile.HasUniqueFor");

            query.SetInput("userId",provider.UserId);
            return query.HasUnique();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual bool
        HasUniqueWithUserName(string userName)
        {
            INamedQuery<IProfile> query =
                GetNamedQuery("Profile.HasUniqueWithUserName");

            query.SetInput("userName",userName);
            return query.HasUnique();
        }
    }
}

//  ##########################################################################
