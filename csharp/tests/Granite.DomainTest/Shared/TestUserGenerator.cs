//  ##########################################################################
//  # File Name: TestUserGenerator.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;
using System.Collections.Generic;
using System.Text;
using Granite.Domain.Role;
using Granite.Domain.Tenant;
using Granite.Domain.User;
using Strata.Foundation.Value;
using SystemEx.Injection;

namespace Granite.Domain.Shared
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class TestUserGenerator:
        ITestUserGenerator
    {
        private Random                             itsRandom;
        private IList<string>                      itsFirstNames;
        private IList<string>                      itsLastNames;
        private string                             itsPasswordChars;
        private IList<string>                      itsEmailServers;
        private IList<string>                      itsStreets;
        private IList<Tuple<string,string,string>> itsCities;
        private IList<string>                      itsAreaCodes;
        private IUserFactory                       itsFactory;
        private ITenantRepository                  itsTenants;
        private IRoleRepository                    itsRoles;

        [Inject]
        public
        TestUserGenerator(IUserFactory factory,ITenantRepository tenants,IRoleRepository roles)
        {
            itsRandom = new Random();
            itsFirstNames =
                new List<string>()
                {
                    "John",
                    "Michael",
                    "David",
                    "Brian",
                    "James",
                    "Mark",
                    "Lisa",
                    "Jane",
                    "Terry",
                    "Phil",
                    "Greg",
                    "Alice",
                    "Robert",
                    "Andy",
                    "Carol",
                    "Zack",
                    "Steve",
                    "Richard",
                    "Shelly",
                    "Vera"
                };
            itsLastNames =
                new List<string>()
                {
                    "Smith",
                    "Jones",
                    "Cooper",
                    "Miller",
                    "Tailor",
                    "Wright",
                    "Porter",
                    "Cobbler",
                    "Hawker",
                    "Abbot",
                    "Baker",
                    "Brewer",
                    "Butcher",
                    "Becker",
                    "Carter",
                    "Carpenter",
                    "Fowler",
                    "Weaver",
                    "Wagner",
                    "Vogler"
                };
            itsPasswordChars =
                "AaBbCcDdEeFfGgHhIiJjKkLlMmOoPpQqRrSsTtUuVvWwXxYyZz" +
                "1234567890" +
                "!@#$%";
                

            itsEmailServers =
                new List<string>()
                {
                    "@gmail.com",
                    "@protonmail.com",
                    "@yahoo.com",
                    "@hotmail.com",
                    "@mail.com"
                };
            itsStreets =
                new List<string>()
                {
                    "Fern Ave",
                    "Main St",
                    "Shadybrook Circle",
                    "Avenue C",
                    "Sycamore Lane",
                    "5th Ave",
                    "Greenwood St",
                    "Slater Circle",
                    "Avenue A",
                    "Pinenut Lane"
                };
            itsCities =
                new List<Tuple<string,string,string>>()
                {
                    Tuple.Create("Torrance","Ca","90501"),
                    Tuple.Create("Cerritos","Ca","90703"),
                    Tuple.Create("Los Angeles","Ca","90017"),
                    Tuple.Create("San Francisco","Ca","94111"),
                    Tuple.Create("San Diego","Ca","92121"),
                    Tuple.Create("Denver","Co","80223"),
                    Tuple.Create("Portland","Or","97229"),
                    Tuple.Create("Seattle","Wa","98115"),
                    Tuple.Create("Chicago","Il","60620"),
                    Tuple.Create("New York","Ny","10001")
                };
            itsAreaCodes =
                new List<string>()
                {
                    "310",
                    "212",
                    "415",
                    "909",
                    "869",
                    "562",
                    "714",
                    "324",
                    "303",
                    "719"
                };
            itsFactory = factory;
            itsTenants = tenants;
            itsRoles = roles;
        }

        public string 
        GenerateUserName()
        {
            string first = itsFirstNames[itsRandom.Next(0,itsFirstNames.Count)];
            string last = itsLastNames[itsRandom.Next(0,itsLastNames.Count)];
            string separator = itsRandom.Next(0,2) == 0 ? string.Empty : ".";

            return first + separator + last;
        }

        public string 
        GeneratePassword()
        {
            StringBuilder builder = new StringBuilder();

            for (int i = 0;i < 10;++i)
                builder.Append(
                    itsPasswordChars[itsRandom.Next(0,itsPasswordChars.Length)]);

            return builder.ToString();
        }

        public string 
        GenerateExternalUserId()
        {
            return itsRandom.Next(11111111,99999999).ToString();
        }

        public EmailAddress 
        GenerateEmail()
        {
            return
                new EmailAddress(
                    GenerateUserName() +
                    itsEmailServers[itsRandom.Next(0,itsEmailServers.Count)]);
        }

        public PersonName 
        GenerateName()
        {
            return
                new PersonName(
                    itsFirstNames[itsRandom.Next(0,itsFirstNames.Count)],
                    itsLastNames[itsRandom.Next(0,itsLastNames.Count)]);
        }

        public PostalAddress 
        GenerateAddress()
        {
            int cityIndex = itsRandom.Next(0,itsCities.Count);

            return
                new PostalAddress()
                {
                    StreetAddress = 
                        itsRandom.Next(111,99999).ToString() + " " +
                        itsStreets[itsRandom.Next(0,itsStreets.Count)],
                    City = itsCities[cityIndex].Item1,
                    State = itsCities[cityIndex].Item2,
                    PostalCode = itsCities[cityIndex].Item3,
                    CountryCode = "USA"
                };
        }

        public PhoneNumber 
        GeneratePhone()
        {
            StringBuilder builder = new StringBuilder();

            builder
                .Append(itsAreaCodes[itsRandom.Next(0,itsAreaCodes.Count)])
                .Append('.')
                .Append(itsRandom.Next(200,999))
                .Append('.')
                .Append(itsRandom.Next(1000,9999));

            return new PhoneNumber(builder.ToString());
        }

        public IUser 
        GenerateUser()
        {
            string first = itsFirstNames[itsRandom.Next(0,itsFirstNames.Count)];
            string last = itsLastNames[itsRandom.Next(0,itsLastNames.Count)];
            string separator = itsRandom.Next(0,2) == 0 ? string.Empty : ".";
            string userName = first + separator + last;
            string password = GeneratePassword();
            string email = userName + itsEmailServers[itsRandom.Next(0,itsEmailServers.Count)];
            HashedString hashed = new HashedString(password);

            ITenant tenant = itsTenants.Insert(new StandardTenant(userName,hashed));
            IUser user =
                itsFactory.CreateUser(
                    tenant,
                    userName,
                    hashed,
                    new EmailAddress(email),
                    itsRoles.GetRoles(new string[] { "GraniteGuest" }));

            ((StandardUser)user).PlainTextPassword = password;
            return user;
        }
    }
}

//  ##########################################################################
