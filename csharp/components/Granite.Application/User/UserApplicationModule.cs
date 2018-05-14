//  ##########################################################################
//  # File Name: UserApplicationModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.User;
using Strata.Foundation.Injection;

namespace Granite.Application.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class UserApplicationModule:
        AbstractModule
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        public
        UserApplicationModule():
            base("UserApplicationModule") {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override void 
        Initialize()
        {
            Bind<IUserProcessor>()
                .ToType<UserProcessor>()
                .WithScope(DefaultScope);
            Bind<IUserService>()
                .ToType<UserService>()
                .WithScope(DefaultScope);
        }
    }
}

//  ##########################################################################
