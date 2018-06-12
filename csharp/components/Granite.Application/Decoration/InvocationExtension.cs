//  ##########################################################################
//  # File Name: InvocationExtension.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Castle.DynamicProxy;
using Granite.Common.Access;
using System;
using System.Reflection;

namespace Granite.Application.Decoration
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
            MethodInfo method = invocation.Method;
            Type       type = invocation.TargetType;
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
            foreach (object argument in invocation.Arguments)
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
            foreach (object argument in invocation.Arguments)
                if (argument is ISessionIdProvider)
                    return (ISessionIdProvider)argument;

            if (invocation.ReturnValue is ISessionIdProvider)
                return (ISessionIdProvider)invocation.ReturnValue;

            return null;
        }

    }
}

//  ##########################################################################
