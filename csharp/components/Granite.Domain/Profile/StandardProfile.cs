//  ##########################################################################
//  # File Name: StandardProfile.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;
using Strata.Domain.Shared;
using Strata.Foundation.Value;

namespace Granite.Domain.Profile
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class StandardProfile:
        AbstractEntity<Guid>,
        IProfile
    {

        public virtual Guid          UserId { get; set; }
        public virtual string        UserName { get; set; }

        public virtual PersonName    Name { get; set; }
        public virtual EmailAddress  Email { get; set; }
        public virtual PhoneNumber   Phone { get; set; }
        public virtual PostalAddress Address { get; set; }

        public virtual string        JobTitle { get; set; }
    }
}

//  ##########################################################################
