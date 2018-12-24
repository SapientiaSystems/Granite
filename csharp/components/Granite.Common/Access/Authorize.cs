//  ##########################################################################
//  # File Name: Authorize.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Injection;
using Ninject;
using System;
using System.Collections.Generic;
using System.Linq;

namespace Granite.Common.Access
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [AttributeUsage(
        AttributeTargets.Interface | 
        AttributeTargets.Class|
        AttributeTargets.Method)]
    public
    class Authorize:
        Attribute
    {
        public string Roles { get; set; }

        private IAuthorizer itsAuthorizer;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        Authorize()
        {
            Roles = string.Empty;
            itsAuthorizer =
                Global
                    .Container
                    .Get<IAuthorizer>();
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public virtual void
        AuthorizeUser(string accessToken)
        {
            itsAuthorizer.AuthorizeUser(accessToken,GetRoles());
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public virtual IClientIpAddressRecorder
        GetClientIdAddressRecorder()
        {
            return itsAuthorizer.Recorder;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        protected virtual string[]
        GetRoles()
        {
            ISet<string> roles = new HashSet<string>();

            foreach (string role in Roles.Split(','))
            {
                string trimmed = role.Trim();

                if (!string.IsNullOrEmpty(trimmed))
                    roles.Add(trimmed);
            }

            return roles.ToArray();
        }
    }
}

//  ##########################################################################
