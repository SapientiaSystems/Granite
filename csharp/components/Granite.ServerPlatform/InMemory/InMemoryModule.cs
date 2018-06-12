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
                            .InsertNamedQuery(
                                new InMemoryNamedQuery<IUser>(
                                    provider,
                                    "NewUser.GetUniqueWithExternalUserId",
                                    new ExternalUserIdEqualsPredicate()))
                            .InsertNamedQuery(
                                new InMemoryNamedQuery<IUser>(
                                    provider,
                                    "NewUser.GetUniqueWithUserName",
                                    new UserNameEqualsPredicate()))
                            .InsertNamedQuery(
                                new InMemoryNamedQuery<IUser>(
                                    provider,
                                    "NewUser.HasUniqueWithExternalUserId",
                                    new ExternalUserIdEqualsPredicate()))
                            .InsertNamedQuery(
                                new InMemoryNamedQuery<IUser>(
                                    provider,
                                    "NewUser.HasUniqueWithUserName",
                                    new UserNameEqualsPredicate() ) );

                        return provider;
                    } );
        }
    }
}

//  ##########################################################################
