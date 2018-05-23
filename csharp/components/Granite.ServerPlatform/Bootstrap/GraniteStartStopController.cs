//  ##########################################################################
//  # File Name: GraniteStartStopController.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.User;
using Granite.ProvidedService.User;
using Strata.Foundation.Bootstrap;
using Strata.Foundation.Injection;
using System;
using SystemEx.Injection;

namespace Granite.ServerPlatform.Bootstrap
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class GraniteStartStopController:
        IStartStopController
    {
        protected IContainer Container { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public 
        GraniteStartStopController(IContainer container)
        {
            Container = container;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public void 
        StartApplication()
        {
            IUserRepository userRepository =
                Container.GetInstance<IUserRepository>();
            IRoleRepository roleRepository =
                Container.GetInstance<IRoleRepository>();
            IUser guest = null;

            if (userRepository.HasUniqueWithUserName("guest"))
                return;

            guest =
                new StandardUser()
                {
                    ExternalUserId = "guest",
                    UserName = "guest",
                    Status = UserStatus.VERIFIED,
                    Created = DateTime.Now,
                    LastModified = DateTime.Now
                };
            guest.SetRoles(
                roleRepository.GetRoles(new string[] { "GraniteUser" }));

            userRepository.Insert(guest);

        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public void 
        StopApplication()
        {
        }
    }
}

//  ##########################################################################
