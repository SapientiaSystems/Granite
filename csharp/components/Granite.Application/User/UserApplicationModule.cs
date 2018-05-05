//  ##########################################################################
//  # File Name: UtilityModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Utility;
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
            base("UtilityModule") {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override void 
        Initialize()
        {
            Bind<IUserProcessor>()
                .ToType<UserProcessor>()
                .WithScope<RequestScope>();
            Bind<IUserService>()
                .ToType<UserService>()
                .WithScope<RequestScope>();
        }
    }
}

//  ##########################################################################
