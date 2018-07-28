//  ##########################################################################
//  # File Name: StandardAccount.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Domain.Shared;
using System;

namespace Granite.Domain.Payment
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class StandardAccount:
        AbstractEntity<
            Guid,
            IAccount,
            IAccountDomainEvent,
            IAccountDomainEventObserver>,
        IAccount
    {

        public virtual Guid
        AccountId
        {
            get { return PrimaryId; }
            set { PrimaryId = value; }
        }

        public virtual Guid   TenantId { get; set; }
        public virtual Guid   UserId { get; set; }

        public virtual string ExternalAccountId { get; set; }
        public virtual string ExternalPaymentMethodId { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        StandardAccount()
        {

        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        protected override IAccount
        GetSelf()
        {
            return this;
        }
    }
}

//  ##########################################################################
