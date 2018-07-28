//  ##########################################################################
//  # File Name: StandardRole.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################


using Strata.Foundation.Utility;
using System.Collections.Generic;

namespace Granite.Domain.Role
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class StandardRole:
        IRole
    {
        public string                     RoleName { get;protected set; }
        public IReadOnlyCollection<IRole> IncludedRoles { get;protected set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        internal
        StandardRole(string roleName)
        {
            RoleName = roleName;
            IncludedRoles = new HashSet<IRole>();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        internal
        StandardRole(StandardRole other)
        {
            RoleName = other.RoleName;
            IncludedRoles = new HashSet<IRole>(other.IncludedRoles);
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        StandardRole(string roleName,params IRole[] includedRoles)
        {
            RoleName      = roleName;
            IncludedRoles = new HashSet<IRole>(includedRoles);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public ICopyable
        MakeCopy()
        {
            return new StandardRole(this);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public bool 
        Includes(IRole role)
        {
            if (role == null)
                return false;

            if (RoleName.Equals(role.RoleName))
                return true;

            foreach (IRole included in IncludedRoles)
                if (included.Includes(role))
                    return true;

            return false;
        }
    }
}

//  ##########################################################################
