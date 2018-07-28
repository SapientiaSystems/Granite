//  ##########################################################################
//  # File Name: StripePlanProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;
using Granite.Common.Value;
using Granite.ProvidedService.Payment;
using Granite.RequiredService.Payment;
using Strata.Application.Service;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Logging;
using Strata.Foundation.Value;
using Stripe;
using SystemEx.Injection;

namespace Granite.ServerPlatform.Stripe
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class StripePlanProcessor:
        AbstractService,
        IExternalPlanProcessor
    {

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        [Inject]
        public
        StripePlanProcessor(
            IUnitOfWorkProvider provider,
            ILogger             logger): 
            base(provider,logger) {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public CreateExternalPlanReply
        CreateExternalPlan(CreateExternalPlanRequest request)
        {
            StripePlanService  service = new StripePlanService();
            StripePlan         plan = null;
            CreatePlanData     planData = request.PlanToCreate;

            plan =
                service.Create(
                    new StripePlanCreateOptions()
                    {
                        Name = planData.PlanName,
                        Interval = ToString(planData.IntervalType),
                        IntervalCount = planData.IntervalCount,
                        Currency = planData.Currency,
                        Amount = ToCents(planData.Amount)
                    });

            PushRollbackAction(
                () => { service.Delete(plan.Id); });

            return
                new CreateExternalPlanReply()
                {
                    PlanName = plan.Name,
                    ExternalPlanId = plan.Id,
                    IntervalType = planData.IntervalType,
                    IntervalCount = plan.IntervalCount,
                    Currency = plan.Currency,
                    Amount = planData.Amount
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public DeleteExternalPlanReply
        DeleteExternalPlan(DeleteExternalPlanRequest request)
        {
            StripePlanService service = new StripePlanService();
            StripePlan snapshot = null;
            StripeDeleted deleted = null;

            snapshot = service.Get(request.ExternalPlanId);
            deleted = service.Delete(request.ExternalPlanId);

            if (!deleted.Deleted)
                return 
                    new DeleteExternalPlanReply()
                    {
                        IsSuccessful = false,
                        FailureMessage = "Could not delete plan."
                    };

            return 
                new DeleteExternalPlanReply()
                {
                    IsSuccessful = true,
                    SuccessMessage = "Deleted plan."
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        private string
        ToString(PaymentIntervalKind kind)
        {
            switch (kind)
            {
                case PaymentIntervalKind.MONTH:
                    return "month";

                case PaymentIntervalKind.YEAR:
                    return "year";

                case PaymentIntervalKind.DAY:
                    return "day";

                case PaymentIntervalKind.WEEK:
                    return "week";
            }

            throw new ArgumentOutOfRangeException("kind");
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        private int
        ToCents(decimal amount)
        {
            return (int)(amount * 100);
               
        }
    }
}

//  ##########################################################################
