//  ##########################################################################
//  # File Name: IConstraintDomainEventObserver.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Domain.Shared;

namespace Granite.Domain.Constraint
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface IConstraintDomainEventObserver:
        IDomainEventObserver<IConstraintDomainEvent>
    {
    }
}

//  ##########################################################################
