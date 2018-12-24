//  ##########################################################################
//  # File Name: PersistenceModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Ninject.Injection;
using Strata.Domain.UnitOfWork;
using Ninject.Activation;
using System;

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
        /// <inheritDoc/>
        ///  
        public override void
        Load()
        {
            Bind<IUnitOfWorkProvider>()
                .ToMethod(GetUnitOfWorkProviderProvider())
                .InDefaultScope();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected abstract Func<IContext,IUnitOfWorkProvider>
        GetUnitOfWorkProviderProvider();
    }
}

//  ##########################################################################
