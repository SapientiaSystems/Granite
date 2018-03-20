//  ##########################################################################
//  # File Name: InMemoryModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Persistence;
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
                    () => new InMemoryUnitOfWorkProvider() );
        }
    }
}

//  ##########################################################################
