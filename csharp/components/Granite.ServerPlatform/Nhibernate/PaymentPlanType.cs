//  ##########################################################################
//  # File Name: PaymentPlanType.cs
//  # Copyright: 2011-2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Value;
using Granite.Domain.Payment;
using NHibernate;
using NHibernate.Engine;
using NHibernate.Type;
using NHibernate.UserTypes;
using Strata.Foundation.Value;
using System;
using System.Data.Common;

namespace Granite.ServerPlatform.Nhibernate
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// User defined type Nhibernate mapper for <c>PaymentPlan</c> objects.
    /// </summary>
    /// 
    public
    class PaymentPlanType:
        ICompositeUserType
    {
        public string[] 
        PropertyNames
        {
            get
            {
                return 
                    new string[] 
                    {
                        "InitialPaymentDate",
                        "IntervalType",
                        "IntervalCount",
                        "Currency",
                        "Amount"
                    };
            }
        }

        public IType[] 
        PropertyTypes
        {
            get
            {
                return new IType[] 
                {
                    NHibernateUtil.DateTime,
                    NHibernateUtil.Enum(typeof(PaymentIntervalKind)),
                    NHibernateUtil.Int32,
                    NHibernateUtil.String,
                    NHibernateUtil.Decimal
                };
            }
        }

        public Type 
        ReturnedClass
        {
            get { return typeof(PaymentPlan); }
        }

        public bool
        IsMutable
        {
            get { return true; }
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Creates a new <c>PaymentPlanType</c> instance.
        /// </summary>
        /// 
        public
        PaymentPlanType() {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public object 
        GetPropertyValue(object component,int property)
        {
            PaymentPlan plan = (PaymentPlan)component;

            if (plan == null)
                return null;

            switch (property)
            {
                case 0:
                    return plan.InitialPaymentDate;
                case 1:
                    return plan.IntervalType;
                case 2:
                    return plan.IntervalCount;
                case 3:
                    return plan.Currency;
                case 4:
                    return plan.Amount;
                default:
                    throw new HibernateException("unknown property");
            }
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public void 
        SetPropertyValue(object component,int property,object value)
        {
            PaymentPlan address = (PaymentPlan)component;

            if (address == null)
                return;

            switch (property)
            {
                case 0:
                    address.InitialPaymentDate = (DateTime)value;
                    break;
                case 1:
                    address.IntervalType = (PaymentIntervalKind)value;
                    break;
                case 2:
                    address.IntervalCount = (int)value;
                    break;
                case 3:
                    address.Currency = (string)value;
                    break;
                case 4:
                    address.Amount = (decimal)value;
                    break;
                default:
                    throw new HibernateException("unknown property");
            }

        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public new bool
        Equals(object x,object y) 
        {
            if (ReferenceEquals(x,y))
                return true;
        
            if ( x == null || y == null )
                return false;
        
            return x.Equals( y );
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public int 
        GetHashCode(object x) 
        {
            if ( x == null )
                return 0;
        
            return x.GetHashCode();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public object
        NullSafeGet(
            DbDataReader reader,
            String[] names,
            ISessionImplementor session,
            object owner)
        {
            string initialPaymentDateColumn = names[0];
            string intervalTypeColumn = names[1];
            string intervalCountColumn = names[2];
            string currencyColumn = names[3];
            string amountColumn = names[4];
            object initialPaymentDate = null;
            object intervalType = null;
            object intervalCount = null;
            object currency = null;
            object amount = null;

            if (reader == null)
                return null;

            initialPaymentDate =
                NHibernateUtil
                    .DateTime
                    .NullSafeGet(
                        reader,
                        initialPaymentDateColumn,
                        session,
                        owner);
            intervalType =
                NHibernateUtil
                    .Enum(typeof(PaymentIntervalKind))
                    .NullSafeGet(
                        reader,
                        intervalTypeColumn,
                        session,
                        owner);
            intervalCount =
                NHibernateUtil
                    .Int32
                    .NullSafeGet(
                        reader,
                        intervalCountColumn,
                        session,
                        owner);
            currency =
                NHibernateUtil
                    .String
                    .NullSafeGet(
                        reader,
                        currencyColumn,
                        session,
                        owner);
            amount =
                NHibernateUtil
                    .Decimal
                    .NullSafeGet(
                        reader,
                        amountColumn,
                        session,
                        owner);

            return
                initialPaymentDate == null && intervalType == null && intervalCount == null &&
                currency == null && amount == null
                    ? null
                    : new PaymentPlan()
                    {
                        InitialPaymentDate = (DateTime)initialPaymentDate,
                        IntervalType = (PaymentIntervalKind)intervalType,
                        IntervalCount = (int)intervalCount,
                        Currency = currency?.ToString(),
                        Amount = (decimal)amount
                    };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public void 
        NullSafeSet(
            DbCommand           st,
            object              value,
            int                 index,
            bool[]              settable,
            ISessionImplementor session) 
        {
            PaymentPlan name = (PaymentPlan)value;

            if (value == null)
            {
                NHibernateUtil.DateTime.NullSafeSet(st,null,index,session);
                NHibernateUtil.Enum(typeof(PaymentIntervalKind)).NullSafeSet(st,null,index+1,session);
                NHibernateUtil.Int32.NullSafeSet(st,null,index+2,session);
                NHibernateUtil.String.NullSafeSet(st,null,index+3,session);
                NHibernateUtil.Decimal.NullSafeSet(st,null,index+4,session);
                return;
            }

            NHibernateUtil.DateTime.NullSafeSet(st,name.InitialPaymentDate,index,session);
            NHibernateUtil.Enum(typeof(PaymentIntervalKind)).NullSafeSet(st,name.IntervalType,index+1,session);
            NHibernateUtil.Int32.NullSafeSet(st,name.IntervalCount,index+2,session);
            NHibernateUtil.String.NullSafeSet(st,name.Currency,index+3,session);
            NHibernateUtil.Decimal.NullSafeSet(st,name.Amount,index+4,session);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public object 
        DeepCopy(object value) 
        {
            return (PaymentPlan)value;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public object 
        Disassemble(
            object              value,
            ISessionImplementor session)
        {
            return (PaymentPlan)value;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public object 
        Assemble(
            object              cached,
            ISessionImplementor session,
            object              owner) 
        {
            return (PaymentPlan)cached;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public object 
        Replace(
            object              original,
            object              target,
            ISessionImplementor session,
            object              owner) 
        {
            return (PaymentPlan)original;
        }

    }
}

//  ##########################################################################
