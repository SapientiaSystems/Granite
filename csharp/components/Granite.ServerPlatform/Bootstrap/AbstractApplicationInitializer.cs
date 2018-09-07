//  ##########################################################################
//  # File Name: AbstractApplicationInitializer.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Domain.UnitOfWork;
using System;

namespace Granite.ServerPlatform.Bootstrap
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// Base class of all application initializers. 
    /// </summary>
    ///  
    public abstract
    class AbstractApplicationInitializer:
        IApplicationInitializer
    {
        protected IUnitOfWorkProvider Provider { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Creates a new <c>AbstractApplicationInitializer</c> with the 
        /// specified <c>IUnitOfWorkProvider</c>.
        /// </summary>
        ///
        /// <param name="provider">provides the unit of work</param>
        ///
        public 
        AbstractApplicationInitializer(IUnitOfWorkProvider provider)
        {
            Provider = provider;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///
        public virtual void 
        Initialize()
        {
            IUnitOfWork unitOfWork = null;

            try
            {
                unitOfWork = Provider.GetUnitOfWork();
                DoInitialize();
                unitOfWork.Commit();
            }
            catch (Exception)
            {
                if (unitOfWork != null)
                    unitOfWork.Rollback();

                throw;
            }
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Does the work of initializing the application. 
        /// Must be implemented by concrete subclasses of 
        /// <c>AbstractApplicationInitializer</c>.
        /// </summary>
        ///
        protected abstract void
        DoInitialize();
    }
}

//  ##########################################################################
