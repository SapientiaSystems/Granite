//  ##########################################################################
//  # File Name: AuthorizedService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Session;
using Granite.Domain.User;
using Granite.ProvidedService.Transfer;
using Strata.Application.Service;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Logging;

namespace Granite.Application.Service
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public abstract
    class AuthorizedService:
        AbstractService
    {
        private ISessionRepository itsRepository;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected
        AuthorizedService(
            IUnitOfWorkProvider provider,
            ILogger             logger,
            ISessionRepository  repository): 
            base(provider,logger)
        {
            itsRepository = repository;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual ISession
        GetSessionFrom(GraniteRequest request)
        {
            if (string.IsNullOrEmpty(request.AccessToken))
                return null;

            return itsRepository.GetUniqueWithAccessToken(request.AccessToken);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual IUser
        GetUserFrom(GraniteRequest request)
        {
            ISession session = GetSessionFrom(request);

            if (session == null)
                return null;

            return session.User;
        }
    }
}

//  ##########################################################################
