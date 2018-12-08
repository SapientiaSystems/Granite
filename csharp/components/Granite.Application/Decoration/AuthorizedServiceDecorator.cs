//  ##########################################################################
//  # File Name: AuthorizedServiceDecorator.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Castle.DynamicProxy;
using Granite.Application.Shared;
using Granite.Application.Tenant;
using Granite.Common.Access;
using Granite.Common.Injection;
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
        private ITenantManager           itsManager;
        private IAccessTokenRefresher    itsRefresher;
        private IClientIpAddressRecorder itsRecorder;

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
            IUnitOfWorkProvider      provider,
            ILogger                  logger,
            ITenantManager           manager,
            IAccessTokenRefresher    refresher,
            IClientIpAddressRecorder recorder,
            int                      maxRetries):
            base(provider,logger,maxRetries)
        {
            itsManager   = manager;
            itsRefresher = refresher;
            itsRecorder  = recorder;
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

            itsRecorder.RecordClientIpAddress();
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
                if (request != null)
                {
                    reply.CorrelationId = request.CorrelationId;
                    reply.SessionId = request.SessionId;
                }

                itsRefresher.RefreshIfNeeded(reply);
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
