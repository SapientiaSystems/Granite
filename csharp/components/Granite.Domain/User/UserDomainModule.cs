//  ##########################################################################
//  # File Name: UserDomainModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Injection;
using System.Collections.Generic;
using SystemEx.Injection;

namespace Granite.Domain.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class UserDomainModule<T>:
        AbstractModule
        where T: IProvider<IList<IRole>>
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        UserDomainModule() :
            base("UserDomainModule") {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override void 
        Initialize()
        {
            Bind<IProvider<IList<IRole>>>()
                .ToType<T>()
                .WithScope<SingletonScope>();

            Bind<IRoleRepository>()
                .ToType<RoleRepository>()
                .WithScope<SingletonScope>();

            Bind<IUserRepository>()
                .ToType<UserRepository>()
                .WithScope(DefaultScope);
        }
    }
}

//  ##########################################################################
