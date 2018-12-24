//  ##########################################################################
//  # File Name: AuthorizedProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Shared;
using Granite.Application.Tenant;
using Granite.Common.Access;
using Granite.Domain.Session;
using Granite.Domain.User;
using Granite.ProvidedService.Transfer;
using Strata.Application.Service;
using Strata.Domain.UnitOfWork;

namespace Granite.Application.Service
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public abstract
    class AuthorizedProcessor:
        AbstractProcessor
    {
        private ISessionRepository itsRepository;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected
        AuthorizedProcessor(
            IUnitOfWorkProvider      provider,
            ISessionRepository       repository): 
            base(provider)
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
