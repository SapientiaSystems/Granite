//  ##########################################################################
//  # File Name: StandardRole.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################


using System.Collections.Generic;

namespace Granite.Domain.User
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
        public
        StandardRole(string roleName,params IRole[] includedRoles)
        {
            RoleName      = roleName;
            IncludedRoles = new HashSet<IRole>(includedRoles);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public bool 
        Includes(IRole role)
        {
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
