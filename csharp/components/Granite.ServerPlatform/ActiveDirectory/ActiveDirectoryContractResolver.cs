//  ##########################################################################
//  # File Name: ActiveDirectoryContractResolver.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Microsoft.Azure.ActiveDirectory.GraphClient;
using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;
using System;
using System.Reflection;

namespace Granite.ServerPlatform.ActiveDirectory
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class ActiveDirectoryContractResolver:
        CamelCasePropertyNamesContractResolver
    {
        public new static readonly ActiveDirectoryContractResolver Instance = 
            new ActiveDirectoryContractResolver();

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        protected override JsonProperty 
        CreateProperty(
            MemberInfo          member,
            MemberSerialization memberSerialization)
        {
            JsonProperty property = 
                base.CreateProperty(member,memberSerialization);

            if (MustNotBeSerialized(property))
                property.ShouldSerialize = instance => { return false; };

            return property;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private bool
        MustNotBeSerialized(JsonProperty property)
        {
            Type declaringType = property.DeclaringType;
            string propertyName = property.PropertyName;

            return
                (declaringType == typeof(DirectoryObject) &&
                propertyName == "createdOnBehalfOf") ||
                (declaringType == typeof(User) && 
                propertyName == "refreshTokensValidFromDateTime");
        }
    }
}

//  ##########################################################################
