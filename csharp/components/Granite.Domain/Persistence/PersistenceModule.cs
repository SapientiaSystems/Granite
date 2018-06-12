//  ##########################################################################
//  # File Name: PersistenceModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Injection;
using Strata.Domain.UnitOfWork;
using SystemEx.Injection;

namespace Granite.Domain.Persistence
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public abstract
    class PersistenceModule:
        AbstractModule
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected
        PersistenceModule(string name): 
            base(name) {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override void
        Initialize()
        {
            Bind<IUnitOfWorkProvider>()
                .ToProvider(GetUnitOfWorkProviderProvider())
                .WithScope(AbstractModule.DefaultScope);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected abstract IProvider<IUnitOfWorkProvider>
        GetUnitOfWorkProviderProvider();
    }
}

//  ##########################################################################
