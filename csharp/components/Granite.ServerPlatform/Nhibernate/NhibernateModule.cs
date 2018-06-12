//  ##########################################################################
//  # File Name: NhibernateModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Persistence;
using Strata.Foundation.Injection;
using Strata.Nhibernate.UnitOfWork;
using Strata.Domain.UnitOfWork;
using System;
using SystemEx.Injection;

namespace Granite.ServerPlatform.Nhibernate
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class NhibernateModule:
        PersistenceModule
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        NhibernateModule():
            base("NhibernateModule") {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        protected override IProvider<IUnitOfWorkProvider> 
        GetUnitOfWorkProviderProvider()
        {
            return
                new FuncBasedProvider<IUnitOfWorkProvider>(
                    () => new NhibernateUnitOfWorkProvider(
                        new string[] {"Granite.ServerPlatform"} ) );
        }
    }
}

//  ##########################################################################
