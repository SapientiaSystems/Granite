//  ##########################################################################
//  # File Name: IProfile.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.Shared;
using Strata.Domain.Shared;
using Strata.Foundation.Value;
using System;

namespace Granite.Domain.Profile
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    interface IProfile:
        IEntity<Guid>,
        IUserIdProvider
    {
        new Guid      UserId { get; set; }
        string        UserName { get; set; }

        PersonName    Name { get; set; }
        EmailAddress  Email { get; set; }
        PhoneNumber   Phone { get; set; }
        PostalAddress Address { get; set; }

        string        JobTitle { get; set; }
    }
}

//  ##########################################################################
