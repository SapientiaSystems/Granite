//  ##########################################################################
//  # File Name: UserMapperTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Microsoft.Azure.ActiveDirectory.GraphClient;
using Newtonsoft.Json;
using NUnit.Framework;
using Strata.Foundation.Mapper;
using System.Collections.Generic;

namespace Granite.ServerPlatform.ActiveDirectory
{
    using ActiveDirectoryUser = Microsoft.Azure.ActiveDirectory.GraphClient.User;

    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [Ignore("Obsolete")]
    [TestFixture]
    public
    class UserMapperTest
    {
        private IObjectMapper<ActiveDirectoryUser,string> itsTarget;

        [SetUp]
        public void
        SetUp()
        {
            itsTarget = 
                new JsonObjectMapper<ActiveDirectoryUser>(
                    new JsonSerializer()
                    {
                        ContractResolver = new ActiveDirectoryContractResolver(),
                        NullValueHandling = NullValueHandling.Include,
                        DateFormatHandling = DateFormatHandling.IsoDateFormat,
                        DateTimeZoneHandling = DateTimeZoneHandling.Utc,
                        FloatFormatHandling = FloatFormatHandling.DefaultValue,
                        Formatting = Formatting.None,
                        TypeNameHandling = TypeNameHandling.None,
                        TypeNameAssemblyFormatHandling =
                            TypeNameAssemblyFormatHandling.Simple,
                        StringEscapeHandling = StringEscapeHandling.Default
                    });
        }

        [TearDown]
        public void
        TearDown()
        {
            itsTarget = null;
        }

        [Test]
        public void
        TestToObject()
        {
            string payload 
                = @"{
  'odata.metadata': 'https://graph.windows.net/ezvaluatoridentitymanager.onmicrosoft.com/$metadata#directoryObjects/Microsoft.DirectoryServices.UserIdentity/@Element',
            'odata.type': 'Microsoft.DirectoryServices.UserIdentity',
  'objectType': 'UserIdentity',
  'objectId': '80aaa77a-e89c-4e22-8b1f-b5cc12cc90ad',
  'deletionTimestamp': null,
  'accountEnabled': true,
  'ageGroup': null,
  'assignedLicenses': [],
  'assignedPlans': [],
  'city': 'Los Angeles',
  'companyName': null,
  'consentProvidedForMinor': null,
  'country': 'USA',
  'creationType': 'LocalAccount',
  'department': null,
  'dirSyncEnabled': null,
  'displayName': 'John Doe',
  'employeeId': null,
  'facsimileTelephoneNumber': null,
  'givenName': 'John',
  'immutableId': null,
  'isCompromised': null,
  'jobTitle': null,
  'lastDirSyncTime': null,
  'legalAgeGroupClassification': null,
  'mail': null,
  'mailNickname': 'jdoe',
  'mobile': null,
  'onPremisesDistinguishedName': null,
  'onPremisesSecurityIdentifier': null,
  'otherMails': [],
  'passwordPolicies': 'DisablePasswordExpiration',
  'passwordProfile': null,
  'physicalDeliveryOfficeName': null,
  'postalCode': '90211',
  'preferredLanguage': null,
  'provisionedPlans': [],
  'provisioningErrors': [],
  'proxyAddresses': [],
  'refreshTokensValidFromDateTime': '2018-04-25T01:09:29.9910683Z',
  'showInAddressList': null,
  'signInNames': [
    {
      'type': 'userName',
      'value': 'johndoe'
    }
  ],
  'sipProxyAddress': null,
  'state': 'California',
  'streetAddress': '123 Main Street',
  'surname': 'Doe',
  'telephoneNumber': '213-555-1234',
  'usageLocation': null,
  'userIdentities': [],
  'userPrincipalName': '93cdd0ac-3f63-47a5-b916-535c979d0650@ezvaluatoridentitymanager.onmicrosoft.com',
  'userType': 'Member'
}";
            ActiveDirectoryUser actual = itsTarget.ToObject<ActiveDirectoryUser>(payload);

            Assert.AreEqual("80aaa77a-e89c-4e22-8b1f-b5cc12cc90ad",actual.ObjectId);
        }

        [Test]
        public void
        TestToPayload()
        {
            ActiveDirectoryUser expected =
                new ActiveDirectoryUser()
                {
                    SignInNames = 
                        new List<SignInName>()
                            {
                                new SignInName()
                                {
                                    Type = "userName",
                                    Value = "adamsmith"
                                }
                            },
                    // Required settings
                    DisplayName = "adamsmith",
                    PasswordProfile =
                        new PasswordProfile()
                        {
                            Password = "foobar!@#$%^&",
                            ForceChangePasswordNextLogin = false
                        },
                    MailNickname = "adamsmith",
                    AccountEnabled = true,
                    //JobTitle = request.JobTitle,
                    //StreetAddress = request.Address.Address,
                    //City = request.Address.City,
                    //State = request.Address.State,
                    //Country = request.Address.CountryCode,
                    //PostalCode = request.Address.PostalCode,
                    TelephoneNumber = "562-555-1234"
                };
            string payload = itsTarget.ToPayload(expected);

            //Assert.AreEqual("",payload);
        }
    }
}

//  ##########################################################################
