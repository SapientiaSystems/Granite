//  ##########################################################################
//  # File Name: ActiveDirectoryUserServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.RequiredService.User;
using NUnit.Framework;
using System.Configuration;

namespace Granite.ServerPlatform.ActiveDirectory
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [Ignore("deprecated")]
    [TestFixture]
    public
    class ActiveDirectoryUserServiceTest:
        ExternalUserServiceTest
    {
        [SetUp]
        public override void 
        SetUp()
        {
            base.SetUp();
        }

        [TearDown]
        public override void
        TearDown()
        {
            base.TearDown();
        }

        protected override IExternalUserService 
        CreateTarget()
        {
            string clientId = ConfigurationManager.AppSettings["ClientId"];
            string appKey   = ConfigurationManager.AppSettings["AppKey"];
            string tenantId = ConfigurationManager.AppSettings["TenantId"];

            return
                new ActiveDirectoryUserService( 
                    new ActiveDirectoryGraphClient(clientId,appKey,tenantId));
        }
    }
}

//  ##########################################################################
