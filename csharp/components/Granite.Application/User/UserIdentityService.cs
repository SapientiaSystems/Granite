//  ##########################################################################
//  # File Name: UserIdentityService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Shared;
using Granite.ProvidedService.User;
using Strata.Foundation.Logging;
using Strata.Persistence.UnitOfWork;
using System;
using System.Threading;
using SystemEx.Injection;

namespace Granite.Application.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class UserIdentityService:
        AbstractService,
        IUserIdentityService
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        [Inject]
        public
        UserIdentityService(IUnitOfWorkProvider provider,ILogger logger):
            base(provider,logger) {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public StartRegistrationReply 
        StartRegistration(StartRegistrationRequest request)
        {
            StartRegistrationReply reply =
                new StartRegistrationReply();

            Thread.Sleep(1);
            return reply;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public FinishRegistrationReply 
        FinishRegistration(FinishRegistrationRequest request)
        {
            FinishRegistrationReply reply =
                new FinishRegistrationReply();

            return reply;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public UpdateReply 
        Update(UpdateRequest request)
        {
            throw new NotImplementedException();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public RemoveReply
        Remove(RemoveRequest request)
        {
            throw new NotImplementedException();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public GetCurrentReply 
        GetCurrent()
        {
            throw new NotImplementedException();
        }
    }
}

//  ##########################################################################
