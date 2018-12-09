//  ##########################################################################
//  # File Name: PaymentProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using AutoMapper;
using Granite.Application.Service;
using Granite.Common.Access;
using Granite.Domain.Payment;
using Granite.Domain.Session;
using Granite.Domain.Shared;
using Granite.Domain.User;
using Granite.ProvidedService.Payment;
using Granite.RequiredService.Payment;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Logging;
using System;
using System.Linq;
using SystemEx.Injection;

namespace Granite.Application.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class PaymentProcessor:
        AuthorizedService,
        IPaymentProcessor
    {
        private IExternalPaymentService     itsService;
        private IAccountRepository          itsAccountRepository;
        private IPlanRepository             itsPlanRepository;
        private ISubscriptionRepository     itsSubscriptionRepository;
        private IUserRepository             itsUserRepository;
        private IMapper                     itsMapper;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        PaymentProcessor(
            IUnitOfWorkProvider         provider,
            ILogger                     logger,
            ISessionRepository          sessions,
            IExternalPaymentService     service,
            IAccountRepository          accountRepository,
            IPlanRepository             planRepository,
            ISubscriptionRepository     subscriptionRepository,
            IUserRepository             userRepository,
            IMapper                     mapper):
            base(provider,logger,sessions)
        {
            itsService                = service;
            itsAccountRepository      = accountRepository;
            itsPlanRepository         = planRepository;
            itsSubscriptionRepository = subscriptionRepository;
            itsUserRepository         = userRepository;
            itsMapper                 = mapper;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public CreatePlanReply
        CreatePlan(CreatePlanRequest request)
        {
            CreateExternalPlanReply reply =
                itsService.CreateExternalPlan(
                    itsMapper.Map<CreateExternalPlanRequest>(request));
            IPlan plan = null;

            if (!reply.IsSuccessful)
                return
                    new CreatePlanReply()
                    {
                        IsSuccessful = reply.IsSuccessful,
                        ExceptionType = reply.ExceptionType,
                        FailureMessage = reply.FailureMessage
                    };

            plan =
                itsPlanRepository.Insert(
                    itsMapper.Map(reply,new StandardPlan()));

            if (plan == null)
                throw
                    new ApplicationException(
                        "Plan was not persisted correctly.");

            return
                new CreatePlanReply()
                {
                    IsSuccessful = true,
                    SuccessMessage = "Plan has been created",
                    NewPlan = itsMapper.Map<PlanData>(plan)
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public DeletePlanReply
        DeletePlan(DeletePlanRequest request)
        {
            IPlan                   plan = GetPlanToDelete(request);
            DeleteExternalPlanReply externalReply = null;

            if (plan != null)
                return
                    new DeletePlanReply()
                    {
                        IsSuccessful = true,
                        SuccessMessage = "plan does not exist"
                    };

            externalReply =
                itsService.DeleteExternalPlan(
                    itsMapper.Map(
                        plan,
                        itsMapper.Map<DeleteExternalPlanRequest>(
                            request)));

            if (!externalReply.IsSuccessful)
                return itsMapper.Map<DeletePlanReply>(externalReply);

            itsPlanRepository.Remove(plan);

            return
                new DeletePlanReply()
                {
                    IsSuccessful = true,
                    DeletedPlanId = plan.PrimaryId,
                    SuccessMessage = "plan has been deleted"
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public CreateAccountReply
        CreateAccount(CreateAccountRequest request)
        {
            CreateExternalAccountReply reply =
                itsService.CreateExternalAccount(
                    itsMapper.Map<CreateExternalAccountRequest>(request));
            IAccount                   account = null;
            AccountData                accountData = null;
            InitialCreditCardData      card = null;

            if (!reply.IsSuccessful)
                return
                    new CreateAccountReply()
                    {
                        IsSuccessful = reply.IsSuccessful,
                        ExceptionType = reply.ExceptionType,
                        FailureMessage = reply.FailureMessage
                    };

            account =
                itsAccountRepository.Insert(
                    itsMapper.Map(reply,new StandardAccount()));

            if (account == null)
                throw
                    new ApplicationException(
                        "Account was not persisted correctly.");

            accountData = itsMapper.Map<AccountData>(account);
            card = request.AccountToCreate.CreditCardToCreate;
            accountData.CreditCard = reply.CreditCard;
            accountData.PaymentMethod = "Credit Card";

            return
                new CreateAccountReply()
                {
                    IsSuccessful = true,
                    SuccessMessage = "Account has been created",
                    NewAccount = accountData
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public UpdateAccountReply 
        UpdateAccount(UpdateAccountRequest request)
        {
            IAccount                   account = GetAccountToUpdate(request);
            UpdateExternalAccountReply externalReply = null;
            AccountData                accountData = new AccountData();

            if (account == null)
                return
                    new UpdateAccountReply()
                    {
                        IsSuccessful = false,
                        FailureMessage = "Account not found"
                    };

            externalReply = 
                itsService.UpdateExternalAccount(
                    itsMapper.Map(
                        account,
                        itsMapper.Map<UpdateExternalAccountRequest>(
                            request)));

            if (!externalReply.IsSuccessful)
                return itsMapper.Map<UpdateAccountReply>(externalReply);

            account = itsMapper.Map(externalReply,account);
            account = itsAccountRepository.Update(account);
            accountData = itsMapper.Map<AccountData>(account);
            accountData.CreditCard = externalReply.CreditCard;

            return
                new UpdateAccountReply()
                {
                    IsSuccessful = true,
                    SuccessMessage = externalReply.SuccessMessage,
                    UpdatedAccount = accountData
                };  
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public DeleteAccountReply
        DeleteAccount(DeleteAccountRequest request)
        {
            IAccount                   account = GetAccountToDelete(request);
            DeleteExternalAccountReply externalReply = null;

            if (account != null)
                return
                    new DeleteAccountReply()
                    {
                        IsSuccessful = true,
                        SuccessMessage = "account does not exist"
                    };

            externalReply =
                itsService.DeleteExternalAccount(
                    itsMapper.Map(
                        account,
                        itsMapper.Map<DeleteExternalAccountRequest>(
                            request)));

            if (!externalReply.IsSuccessful)
                return itsMapper.Map<DeleteAccountReply>(externalReply);

            itsAccountRepository.Remove(account);

            return
                new DeleteAccountReply()
                {
                    IsSuccessful = true,
                    DeletedAccountId = account.PrimaryId,
                    SuccessMessage = "account has been deleted"
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public GetAccountReply 
        GetAccount(GetAccountRequest request)
        {
            IAccount                account = null;
            GetExternalAccountReply externalReply = null;
            AccountData             accountData = new AccountData();

            CheckPermission(request);
            account = GetAccountToReturn(request);

            if (account == null)
                return
                    new GetAccountReply()
                    {
                        IsSuccessful = false,
                        FailureMessage = "account not found"
                    };

            externalReply =
                itsService.GetExternalAccount(
                    itsMapper.Map(
                        account,
                        itsMapper.Map<GetExternalAccountRequest>(
                            request)));

            accountData = itsMapper.Map<AccountData>(account);
            accountData.CreditCard = externalReply.CreditCard;

            return
                new GetAccountReply()
                {
                    IsSuccessful = true,
                    RequestedAccount = accountData,
                    SuccessMessage = "returned requested account"
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public CreateSubscriptionReply 
        CreateSubscription(CreateSubscriptionRequest request)
        {
            CreateExternalSubscriptionRequest externalRequest = null;
            CreateExternalSubscriptionReply   externalReply = null;
            IUser                             user = GetUserFrom(request);
            IAccount                          account = GetAccount(request);
            ISubscription                     subscription = null;
            IPlan                             plan = null;
            InitialSubscriptionData           input = null;
            SubscriptionData                  output = null;

            input = request.SubscriptionToCreate;
            plan = 
                itsPlanRepository.GetUniqueWithPlanName(
                    input.SubscriptionType);

            if (plan == null)
                throw
                    new ApplicationException(
                        "Plan could not be retrieved correctly.");

            externalRequest = 
                itsMapper.Map<CreateExternalSubscriptionRequest>(request);
            externalRequest.ExternalAccountId = account.ExternalAccountId;
            externalRequest
                .SubscriptionToCreate
                .SubscriptionType = plan.ExternalPlanId;

            externalReply = 
                itsService.CreateExternalSubscription(externalRequest);

            if (!externalReply.IsSuccessful)
                return
                    new CreateSubscriptionReply()
                    {
                        IsSuccessful = externalReply.IsSuccessful,
                        ExceptionType = externalReply.ExceptionType,
                        FailureMessage = externalReply.FailureMessage
                    };

            subscription = itsMapper.Map(externalReply,new StandardSubscription());
            subscription.ExternalPlanId = plan.ExternalPlanId;
            subscription.PlanId = plan.PlanId;
            subscription.UserId = user.UserId;
            subscription.AccountId = account.AccountId;
            subscription = itsSubscriptionRepository.Insert(subscription);

            if (subscription == null)
                throw
                    new ApplicationException(
                        "Subscription was not persisted correctly.");

            output = itsMapper.Map<SubscriptionData>(subscription);
            output.PlanName = plan.PlanName;

            return
                new CreateSubscriptionReply()
                {
                    IsSuccessful = true,
                    SuccessMessage = "Subscription has been created",
                    NewSubscription = output
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public UpdateSubscriptionReply 
        UpdateSubscription(UpdateSubscriptionRequest request)
        {
            ISubscription                     subscription = null;
            IPlan                             plan = null;
            UpdateExternalSubscriptionRequest externalRequest = null;
            UpdateExternalSubscriptionReply   externalReply = null;
            SubscriptionData                  updated = null;

            subscription = 
                itsSubscriptionRepository.GetUnique(
                    request
                        .CurrentSubscription
                        .SubscriptionId);

            if (subscription == null)
                return
                    new UpdateSubscriptionReply()
                    {
                        IsSuccessful = false,
                        FailureMessage = "Subscription not found"
                    };

            plan = 
                itsPlanRepository.GetUniqueWithPlanName(
                    request
                        .NewSubscription
                        .SubscriptionType);

            if (plan == null)
                return
                    new UpdateSubscriptionReply()
                    {
                        IsSuccessful = false,
                        FailureMessage = "Plan not found"
                    };

            subscription.PlanId = plan.PlanId;
            subscription.ExternalPlanId = plan.ExternalPlanId;

            externalRequest =               
                itsMapper.Map(
                    subscription,
                    itsMapper.Map<UpdateExternalSubscriptionRequest>(request));
            externalReply = 
                itsService.UpdateExternalSubscription(externalRequest);

            subscription.ExternalPlanId = externalReply.ExternalPlanId;
            subscription.ExternalSubscriptionId = externalReply.ExternalSubscriptionId;
            subscription = itsSubscriptionRepository.Update(subscription);

            updated = itsMapper.Map<SubscriptionData>(subscription);
            updated.PlanName = plan.PlanName;

            return
                new UpdateSubscriptionReply()
                {
                    IsSuccessful = true,
                    SuccessMessage = "Subscription updated",
                    UpdatedSubscription = updated
                };

        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public DeleteSubscriptionReply
        DeleteSubscription(DeleteSubscriptionRequest request)
        {
            ISubscription                     subscription = null;
            DeleteExternalSubscriptionRequest externalRequest = null;
            DeleteExternalSubscriptionReply   externalReply = null;

            subscription =
                itsSubscriptionRepository.GetUnique(
                    request.SubscriptionIdOfSubscriptionToDelete);

            if (subscription == null)
                return
                    new DeleteSubscriptionReply()
                    {
                        IsSuccessful = true,
                        SuccessMessage = "Subscription does not exist"
                    };

            externalRequest =
                itsMapper.Map(
                    subscription,
                    itsMapper.Map<
                        DeleteExternalSubscriptionRequest>(request));

            return
                itsMapper.Map<DeleteSubscriptionReply>(externalRequest);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public GetSubscriptionReply 
        GetSubscription(GetSubscriptionRequest request)
        {
            ISubscription subscription = GetSubscriptionToReturn(request);
            IPlan         plan         = null;

            if (subscription == null)
                return
                    new GetSubscriptionReply()
                    {
                        IsSuccessful = false,
                        FailureMessage = "Requested subscription not found"
                    };

            plan = itsPlanRepository.GetUnique(subscription.PlanId);

            if (plan == null)
                return itsMapper.Map<GetSubscriptionReply>(subscription);

            return
                itsMapper.Map(
                    plan,
                    itsMapper.Map<GetSubscriptionReply>(subscription));
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public MakePaymentReply 
        MakePayment(MakePaymentRequest request)
        {
            throw new NotImplementedException();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public MakeRefundReply 
        MakeRefund(MakeRefundRequest request)
        {
            throw new NotImplementedException();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected IPlan
        GetPlanToDelete(DeletePlanRequest request)
        {
            if (request.PlanIdOfPlanToDelete != Guid.Empty)
                return
                    itsPlanRepository.GetUnique(
                        request.PlanIdOfPlanToDelete);

            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected IAccount
        GetAccountToUpdate(UpdateAccountRequest request)
        {
            UpdateAccountData update = request.AccountToUpdate;

            if (update == null)
                throw
                    new NullReferenceException(
                        "Failed to update: account data was null.");

            if (update.AccountIdOfAccountToUpdate != Guid.Empty)
                return 
                    itsAccountRepository.GetUnique(
                        update.AccountIdOfAccountToUpdate);

            if (update.UserIdOfAccountToUpdate != Guid.Empty)
            {
                IUser user =
                    itsUserRepository.GetUnique(
                        update.UserIdOfAccountToUpdate);

                return itsAccountRepository.GetUniqueFor(user);
            }

            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected IAccount
        GetAccountToDelete(DeleteAccountRequest request)
        {

            if (request.AccountIdOfAccountToDelete != Guid.Empty)
                return
                    itsAccountRepository.GetUnique(
                        request.AccountIdOfAccountToDelete);

            if (request.UserIdOfAccountToDelete != Guid.Empty)
            {
                IUser user =
                    itsUserRepository.GetUnique(
                        request.UserIdOfAccountToDelete);

                return itsAccountRepository.GetUniqueFor(user);
            }

            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected void
        CheckPermission(GetAccountRequest request)
        {
            IUser user = GetUserFrom(request);

            if (user == null)
                throw
                    new AuthorizationException(
                        "Invalid user: could not find user from request");

            if (request.GetAccountOfCurrentUser)
            {
                if (!user.IsInRole("GraniteUser"))
                    throw
                        new AuthorizationException(
                            "User is not in permissable role: GraniteUser");

                return;
            }

            if (!user.IsInRole("GraniteAdministrator"))
                throw
                    new AuthorizationException(
                        "User is not in permissable role: " +
                        "GraniteAdministrator");


        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected IAccount
        GetAccountToReturn(GetAccountRequest request)
        {
            if (request.GetAccountOfCurrentUser)
            {
                IUser user = GetUserFrom(request);

                if (user != null)
                    return itsAccountRepository.GetUniqueFor(user);
            }

            if (request.AccountIdOfAccountToGet != Guid.Empty)
                return
                    itsAccountRepository.GetUnique(
                        request.AccountIdOfAccountToGet);

            if (request.UserIdOfAccountToGet != Guid.Empty)
            {
                IUser user =
                    itsUserRepository.GetUnique(
                        request.UserIdOfAccountToGet);

                if (user != null)
                    return itsAccountRepository.GetUniqueFor(user);
            }

            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected IAccount
        GetAccount(CreateSubscriptionRequest request)
        {
            IUser user = GetUserFrom(request);

            return itsAccountRepository.GetUniqueFor(user);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected ISubscription
        GetSubscriptionToReturn(GetSubscriptionRequest request)
        {
            if (request.GetSubscriptionOfCurrentUser)
            {
                IUser user = GetUserFrom(request);

                if (user != null)
                    return 
                        itsSubscriptionRepository.GetUniqueFor(user);
            }

            if (request.AccountIdOfSubscriptionToGet != Guid.Empty)
            {
                IAccount account = 
                    itsAccountRepository.GetUnique(
                        request.AccountIdOfSubscriptionToGet);

                if (account != null)
                    return
                        itsSubscriptionRepository
                            .GetAllFor((IAccountIdProvider)account)
                            .FirstOrDefault();
            }

            if (request.UserIdOfSubscriptionToGet != Guid.Empty)
            {
                IUser user =
                    itsUserRepository.GetUnique(
                        request.UserIdOfSubscriptionToGet);

                if (user != null)
                    return 
                        itsSubscriptionRepository
                            .GetAllFor(user)
                            .FirstOrDefault();
            }

            if (request.SubscriptionIdOfSubscriptionToGet != Guid.Empty)
                return
                    itsSubscriptionRepository.GetUnique(
                        request.SubscriptionIdOfSubscriptionToGet);

            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual string
        ObfuscateCardNumber(string cardNumber)
        {
            return 
                new string('*',cardNumber.Length - 4) + 
                cardNumber.Substring(cardNumber.Length - 4);
        }

    }
}

//  ##########################################################################
