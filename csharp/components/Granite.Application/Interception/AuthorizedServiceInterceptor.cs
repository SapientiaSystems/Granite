//  ##########################################################################
//  # File Name: AuthorizedServiceInterceptor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Shared;
using Granite.Application.Tenant;
using Granite.Common.Access;
using Granite.ProvidedService.Transfer;
using Ninject.Extensions.Interception;
using Strata.Application.Decoration;
using Strata.Application.Interception;

namespace Granite.Application.Interception
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class AuthorizedServiceInterceptor:
        UnitOfWorkInterceptor
    {

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///
        public
        AuthorizedServiceInterceptor() {}

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///
        public
        AuthorizedServiceInterceptor(int maxRetries):
            base(maxRetries) {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///
        protected override void
        DoBefore(IInvocation invocation)
        {
            Authorize                authorizer    = null;
            IAccessTokenProvider     tokenProvider = null;
            ISessionIdProvider       idProvider    = null;
            ITenantManager           manager       = null;
            IClientIpAddressRecorder recorder      = null;

            authorizer    = invocation.GetAuthorizer();
            tokenProvider = invocation.GetAccessTokenProvider();
            idProvider    = invocation.GetSessionIdProvider();
            manager       = invocation.GetTenantManager();

            manager.ActivateTenant(tokenProvider,idProvider);

            if (CanAuthorize(authorizer,tokenProvider))
            {
                recorder = authorizer.GetClientIdAddressRecorder();

                recorder.RecordClientIpAddress();
                authorizer.AuthorizeUser(tokenProvider.AccessToken);
            }

            base.DoBefore(invocation);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///
        protected override void
        DoProceed(IInvocation invocation)
        {
            GraniteRequest        request   = null;
            GraniteReply          reply     = null;
            IAccessTokenRefresher refresher = null;

            request   = invocation.GetRequest<GraniteRequest>();
            refresher = invocation.GetAccessTokenRefresher();

            base.DoProceed(invocation);

            reply = invocation.GetReply<GraniteReply>();

            if (reply != null)
            {
                if (request != null)
                {
                    reply.CorrelationId = request.CorrelationId;
                    reply.SessionId = request.SessionId;
                }

                refresher.RefreshIfNeeded(reply);
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
            ITenantManager       manager = invocation.GetTenantManager();

            manager.DeactivateTenant(tokenProvider,idProvider);
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
