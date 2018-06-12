//  ##########################################################################
//  # File Name: AuthorizedServiceDecorator.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Castle.DynamicProxy;
using Granite.Application.Shared;
using Granite.Application.Tenant;
using Granite.Common.Access;
using Granite.ProvidedService.Transfer;
using Strata.Application.Decoration;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Logging;

namespace Granite.Application.Decoration
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class AuthorizedServiceDecorator:
        ServiceDecorator
    {
        private ITenantManager        itsManager;
        private IAccessTokenRefresher itsRefresher;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///
        public
        AuthorizedServiceDecorator() {}

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///
        public
        AuthorizedServiceDecorator(
            IUnitOfWorkProvider   provider,
            ILogger               logger,
            ITenantManager        manager,
            IAccessTokenRefresher refresher,
            int                   maxRetries):
            base(provider,logger,maxRetries)
        {
            itsManager   = manager;
            itsRefresher = refresher;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///
        protected override void
        DoBefore(IInvocation invocation)
        {
            Authorize            authorizer    = null;
            IAccessTokenProvider tokenProvider = null;
            ISessionIdProvider   idProvider    = null;

            authorizer    = invocation.GetAuthorizer();
            tokenProvider = invocation.GetAccessTokenProvider();
            idProvider    = invocation.GetSessionIdProvider();

            itsManager.ActivateTenant(tokenProvider,idProvider);

            if (CanAuthorize(authorizer,tokenProvider))
                authorizer.AuthorizeUser(tokenProvider.AccessToken);

            base.DoBefore(invocation);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///
        protected override void
        DoProceed(IInvocation invocation)
        {
            GraniteRequest request = invocation.GetRequest<GraniteRequest>();
            GraniteReply   reply   = null;

            base.DoProceed(invocation);
            reply = invocation.GetReply<GraniteReply>(); 

            if (reply != null)
            {
                itsRefresher.RefreshIfNeeded(reply);

                if (request != null)
                {
                    reply.CorrelationId = request.CorrelationId;
                    reply.SessionId = request.SessionId;
                }
            }
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///
        protected override void
        DoAfter(IInvocation invocation)
        {
            GraniteRequest       request = 
                                     invocation.GetRequest<GraniteRequest>();
            GraniteReply         reply = invocation.GetReply<GraniteReply>();
            IAccessTokenProvider tokenProvider =
                                     invocation.GetAccessTokenProvider();
            ISessionIdProvider   idProvider =
                                     invocation.GetSessionIdProvider();

            if (reply != null && request != null)
            {
                reply.SessionId = request.SessionId;
                itsRefresher.RefreshIfNeeded(reply);
            }

            itsManager.DeactivateTenant(tokenProvider,idProvider);
            base.DoAfter(invocation);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual bool
        CanAuthorize(Authorize authorizer,IAccessTokenProvider provider)
        {
            return authorizer != null && provider != null;
        }
    }
}

//  ##########################################################################
