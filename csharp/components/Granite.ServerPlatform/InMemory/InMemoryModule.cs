//  ##########################################################################
//  # File Name: InMemoryModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Persistence;
using Granite.Domain.User;
using Strata.Foundation.Injection;
using Strata.Domain.InMemory;
using Strata.Domain.UnitOfWork;
using SystemEx.Injection;
using Granite.Domain.Tenant;
using System;
using Granite.Domain.Group;

namespace Granite.ServerPlatform.InMemory
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class InMemoryModule:
        PersistenceModule
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        InMemoryModule(): 
            base("InMemoryModule") {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        protected override IProvider<IUnitOfWorkProvider> 
        GetUnitOfWorkProviderProvider()
        {
            return 
                new FuncBasedProvider<IUnitOfWorkProvider>(
                    () =>
                    {
                        InMemoryUnitOfWorkProvider provider =
                            new InMemoryUnitOfWorkProvider();

                        provider
                            .InsertReplicator(new TenantReplicator())
                            .InsertRetriever<Guid,ITenant>(TenantRepository.GetKey)
                            .InsertNamedQuery(
                                new InMemoryNamedQuery<IUser>(
                                    provider,
                                    "User.GetUniqueWithExternalUserId",
                                    new ExternalUserIdEqualsPredicate()))
                            .InsertNamedQuery(
                                new InMemoryNamedQuery<IUser>(
                                    provider,
                                    "User.GetUniqueWithUserName",
                                    new UserNameEqualsPredicate()))
                            .InsertNamedQuery(
                                new InMemoryNamedQuery<IUser>(
                                    provider,
                                    "User.HasUniqueWithExternalUserId",
                                    new ExternalUserIdEqualsPredicate()))
                            .InsertNamedQuery(
                                new InMemoryNamedQuery<IUser>(
                                    provider,
                                    "User.HasUniqueWithUserName",
                                    new UserNameEqualsPredicate()))
                            .InsertNamedQuery(
                                new InMemoryNamedQuery<IGroup>(
                                    provider,
                                    "Group.GetGroupsFor",
                                    new ContainsUserIdPredicate() ) );

                        return provider;
                    } );
        }
    }
}

//  ##########################################################################
