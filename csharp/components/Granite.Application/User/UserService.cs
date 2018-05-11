//  ##########################################################################
//  # File Name: UserService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.User;
using Strata.Foundation.Logging;
using Strata.Persistence.UnitOfWork;
using Strata.Service.Core;
using System;
using SystemEx.Injection;

namespace Granite.Application.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class UserService:
        AbstractService,
        IUserService
    {
        private IUserProcessor itsProcessor;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        [Inject]
        public
        UserService(
            IUnitOfWorkProvider    provider,
            ILogger                logger,
            IUserProcessor         processor):
            base(provider,logger)
        {
            itsProcessor = processor;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public StartRegistrationReply 
        StartRegistration(StartRegistrationRequest request)
        {
            return
                Execute(
                    (StartRegistrationRequest r) =>
                        { return itsProcessor.StartRegistration(r); },
                    request );
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public FinishRegistrationReply 
        FinishRegistration(FinishRegistrationRequest request)
        {
            return
                Execute(
                    (FinishRegistrationRequest r) =>
                        { return itsProcessor.FinishRegistration(r); },
                    request);
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
            return
                Execute(
                    (RemoveRequest r) =>
                        { return itsProcessor.Remove(r); },
                    request);
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
