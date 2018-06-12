//  ##########################################################################
//  # File Name: AuthorizedServiceDecoratorFactory.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Shared;
using Granite.Application.Tenant;
using Strata.Application.Decoration;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Logging;
using System;

namespace Granite.Application.Decoration
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class AuthorizedServiceDecoratorFactory:
        ServiceDecoratorFactory<AuthorizedServiceDecorator>
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        AuthorizedServiceDecoratorFactory(int maxRetries): 
            base(null,maxRetries) {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///
        protected override AuthorizedServiceDecorator 
        GetDecorator()
        {
            if (Container == null)
                throw
                    new NullReferenceException(
                        "Container must be set before use");
            return
                new AuthorizedServiceDecorator(
                    Container.GetInstance<IUnitOfWorkProvider>(),
                    Container.GetInstance<ILogger>(),
                    Container.GetInstance<ITenantManager>(),
                    Container.GetInstance<IAccessTokenRefresher>(),
                    MaxRetries);
        }
    }
}

//  ##########################################################################
