//  ##########################################################################
//  # File Name: UserDomainModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Injection;

namespace Granite.Domain.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class UserDomainModule:
        AbstractModule
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
            Bind<IUserRepository>()
                .ToType<UserRepository>()
                .WithScope<RequestScope>();
        }
    }
}

//  ##########################################################################
