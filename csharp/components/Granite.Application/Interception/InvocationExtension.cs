//  ##########################################################################
//  # File Name: InvocationExtension.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Application.Shared;
using Granite.Application.Tenant;
using Granite.Common.Access;
using Ninject.Extensions.Interception;
using System;
using System.Reflection;

namespace Granite.Application.Interception
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public static
    class InvocationExtension
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public static Authorize
        GetAuthorizer(this IInvocation invocation)
        {
            MethodInfo method = invocation.Request.Method;
            Type       type = invocation.Request.Target.GetType();
            Authorize  authorizer =
                           method.GetCustomAttribute<Authorize>();

            if (authorizer != null)
                return authorizer;

            authorizer = type.GetCustomAttribute<Authorize>();

            if (authorizer != null)
                return authorizer;

            foreach (Type interfaceType in type.GetInterfaces())
            {
                authorizer =
                    interfaceType.GetCustomAttribute<Authorize>();

                if (authorizer != null)
                    return authorizer;
            }

            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public static IAccessTokenProvider
        GetAccessTokenProvider(this IInvocation invocation)
        {
            foreach (object argument in invocation.Request.Arguments)
                if (argument is IAccessTokenProvider)
                    return (IAccessTokenProvider)argument;

            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public static ISessionIdProvider
        GetSessionIdProvider(this IInvocation invocation)
        {
            foreach (object argument in invocation.Request.Arguments)
                if (argument is ISessionIdProvider)
                    return (ISessionIdProvider)argument;

            if (invocation.ReturnValue is ISessionIdProvider)
                return (ISessionIdProvider)invocation.ReturnValue;

            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public static ITenantManager
        GetTenantManager(this IInvocation invocation)
        {
            object target = invocation.Request.Target;

            if (target is IAuthorizationPropertySupplier)
                return ((IAuthorizationPropertySupplier)target).Manager;

            throw
                new InvalidCastException(
                    "Service must implement IAuthorizationPropertiesSupplier");
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public static IAccessTokenRefresher
        GetAccessTokenRefresher(this IInvocation invocation)
        {
            object target = invocation.Request.Target;

            if (target is IAuthorizationPropertySupplier)
                return ((IAuthorizationPropertySupplier)target).Refresher;

            throw 
                new InvalidCastException(
                    "Service must implement IAuthorizationPropertiesSupplier");
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public static IClientIpAddressRecorder
        GetClientIpAddressRecorder(this IInvocation invocation)
        {
            object target = invocation.Request.Target;

            if (target is IAuthorizationPropertySupplier)
                return ((IAuthorizationPropertySupplier)target).Recorder;

            throw
                new InvalidCastException(
                    "Service must implement IAuthorizationPropertiesSupplier");
        }

    }
}

//  ##########################################################################
