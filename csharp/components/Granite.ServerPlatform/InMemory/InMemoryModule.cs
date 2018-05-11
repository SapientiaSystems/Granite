//  ##########################################################################
//  # File Name: InMemoryModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Persistence;
using Granite.Domain.User;
using Strata.Foundation.Injection;
using Strata.Persistence.InMemory;
using Strata.Persistence.UnitOfWork;
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
                                    new UserNameEqualsPredicate() ) );

                        return provider;
                    } );
        }
    }
}

//  ##########################################################################
