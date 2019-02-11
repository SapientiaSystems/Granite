//  ##########################################################################
//  # File Name: ProfileServiceTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Profile;
using Granite.ProvidedService.Session;
using Ninject;
using NUnit.Framework;
using Strata.Foundation.Bootstrap;
using Strata.Foundation.Value;
using System;
using System.Collections.Generic;

namespace Granite.Application.Profile
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [Category("Integration")]
    [TestFixture]
    public abstract
    class ProfileServiceTest
    {
        private IProfileService            itsTarget;
        private ISet<DeleteProfileRequest> itsRemovals;
        private ISessionService            itsSessionService;
        private SessionData                itsSession;

        [SetUp]
        public virtual void
        SetUp()
        {
            IKernel              container = CreateContainer();
            IStartStopController starter = container.Get<IStartStopController>();
            LoginReply           reply = null;

            starter.StartApplication();

            itsTarget = container.Get<IProfileService>();
            itsSessionService = container.Get<ISessionService>();

            itsRemovals = new HashSet<DeleteProfileRequest>();

            reply =
                itsSessionService.Login(
                    new LoginRequest()
                    {
                        CorrelationId = Guid.NewGuid(),
                        UserName = "test",
                        Password = "test"
                    });

            Assert.NotNull(reply);
            Assert.NotNull(reply.NewSession);

            itsSession = reply.NewSession;
        }

        [TearDown]
        public virtual void
        TearDown()
        {
            foreach (DeleteProfileRequest request in itsRemovals)
            {
                DeleteProfileReply reply = itsTarget.DeleteProfile(request);

                if (!reply.IsSuccessful)
                    throw new Exception(reply.FailureMessage);
            }
            
            itsSessionService.Logout(
                new LogoutRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    SessionId = itsSession.SessionId,
                    AccessToken = itsSession.AccessToken
                });
            itsTarget = null;
        }

        public static object[] CreateProfileTestCases =
        {
            new CreateProfileRequest()
            {
                CorrelationId = Guid.NewGuid(),
                ProfileToCreate = 
                    new CreateOrUpdateProfileData()
                    {
                        Name = 
                            new PersonName()
                            {
                                FirstName = "Test",
                                LastName  = "Test"
                            },
                        JobTitle = "Big Boss",
                        Address = 
                            new PostalAddress()
                            {
                                StreetAddress = "123 Main St",
                                City = "Los Angeles",
                                State = "CA",
                                PostalCode = "90017",
                                CountryCode = "USA"
                            },
                        Phone = new PhoneNumber("310.555.1234"),
                        Email = new EmailAddress("test@test.com")
                    }
            },
        };
        [TestCaseSource("CreateProfileTestCases")]
        public virtual void
        TestCreateProfile(CreateProfileRequest request)
        {
            DoTestCreateProfile(request);
        }

        public static object[] UpdateProfileTestCases =
        {
            Tuple.Create( 
                new CreateProfileRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    ProfileToCreate =
                        new CreateOrUpdateProfileData()
                        {
                            Name =
                                new PersonName()
                                {
                                    FirstName = "Test",
                                    LastName  = "Test"
                                },
                            JobTitle = "Big Boss",
                            Address =
                                new PostalAddress()
                                {
                                    StreetAddress = "123 Main St",
                                    City = "Los Angeles",
                                    State = "CA",
                                    PostalCode = "90017",
                                    CountryCode = "USA"
                                },
                            Phone = new PhoneNumber("310.555.1234"),
                            Email = new EmailAddress("test@test.com")
                        }
                },
                new UpdateProfileRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    ProfileToUpdate =
                        new CreateOrUpdateProfileData()
                        {
                            Name =
                                new PersonName()
                                {
                                    FirstName = "Test1",
                                    LastName  = "Test2"
                                },
                            JobTitle = "CEO",
                            Address =
                                new PostalAddress()
                                {
                                    StreetAddress = "4567 Maple Ave",
                                    City = "Long Beach",
                                    State = "CA",
                                    PostalCode = "90708",
                                    CountryCode = "USA"
                                },
                            Phone = new PhoneNumber("310.555.1234"),
                            Email = new EmailAddress("test1@test.com")
                        }

                })
        };
        [TestCaseSource("UpdateProfileTestCases")]
        public virtual void
        TestUpdateProfile(Tuple<CreateProfileRequest,UpdateProfileRequest> input)
        {
            UpdateProfileRequest request = input.Item2;
            UpdateProfileReply   reply = null;
            ProfileData          profile = null;

            request = Initialize(request,DoTestCreateProfile(input.Item1));

            reply = itsTarget.UpdateProfile(request);
            Assert.NotNull(reply);
            Assert.IsTrue(reply.IsSuccessful,reply.FailureMessage);

            profile = reply.UpdatedProfile;
            Assert.NotNull(profile);

            itsRemovals.Add(
                new DeleteProfileRequest()
                {
                    SessionId = itsSession.SessionId,
                    AccessToken = itsSession.AccessToken,
                    ProfileIdToDelete = profile.ProfileId
                });

            Assert.AreEqual(request.CorrelationId,reply.CorrelationId);

            if (string.IsNullOrEmpty(profile.UserName))
                Assert.AreEqual(profile.Email.ToString(),profile.UserName);
            else
                Assert.AreEqual(profile.UserName,profile.UserName);

            if (profile.Email != null)
                Assert.AreEqual(profile.Email.ToString(),profile.Email.ToString());

        }

        [Test]
        public virtual void
        TestGetProfileCurrentUser()
        {
            DoTestCreateProfile(
                new CreateProfileRequest()
                {
                    CorrelationId = Guid.NewGuid(),
                    ProfileToCreate =
                            new CreateOrUpdateProfileData()
                            {
                                Name =
                                    new PersonName()
                                    {
                                        FirstName = "Test",
                                        MiddleName = null,
                                        LastName = "Test"
                                    },
                                JobTitle = "Big Boss",
                                Address = null,
                                /*
                                    new PostalAddress()
                                    {
                                        StreetAddress = "123 Main St",
                                        City = "Los Angeles",
                                        State = "CA",
                                        PostalCode = "90017",
                                        CountryCode = "USA"
                                    }, */
                                Phone = new PhoneNumber("310.555.1234"),
                                Email = new EmailAddress("test@test.com")
                            }
                });
            GetProfileReply reply =
                itsTarget.GetProfile(
                    new GetProfileRequest()
                    {
                        SessionId = itsSession.SessionId,
                        AccessToken = itsSession.AccessToken,
                        GetProfileOfCurrentUser = true
                    });

            Assert.NotNull(reply);
            Assert.IsTrue(reply.IsSuccessful,reply.FailureMessage);

            if (reply.IsSuccessful)
                Assert.NotNull(reply.RequestedProfile);
        }

        private CreateProfileReply
        DoTestCreateProfile(CreateProfileRequest request)
        {
            CreateProfileReply reply = null;
            ProfileData newProfile = null;

            request.SessionId = itsSession.SessionId;
            request.AccessToken = itsSession.AccessToken;
            request.ProfileToCreate.UserId = itsSession.UserId;

            reply = itsTarget.CreateProfile(request);
            Assert.NotNull(reply);
            Assert.IsTrue(reply.IsSuccessful,reply.FailureMessage);

            newProfile = reply.NewProfile;
            Assert.NotNull(newProfile);

            itsRemovals.Add(
                new DeleteProfileRequest()
                {
                    SessionId = itsSession.SessionId,
                    AccessToken = itsSession.AccessToken,
                    ProfileIdToDelete = newProfile.ProfileId
                });

            Assert.AreEqual(request.CorrelationId,reply.CorrelationId);

            if (string.IsNullOrEmpty(newProfile.UserName))
                Assert.AreEqual(newProfile.Email.ToString(),newProfile.UserName);
            else
                Assert.AreEqual(newProfile.UserName,newProfile.UserName);

            if (newProfile.Email != null)
                Assert.AreEqual(newProfile.Email.ToString(),newProfile.Email.ToString());

            return reply;
        }

        protected UpdateProfileRequest
        Initialize(UpdateProfileRequest dest,CreateProfileReply src)
        {
            dest.SessionId = itsSession.SessionId;
            dest.AccessToken = itsSession.AccessToken;
            dest.ProfileToUpdate.UserId = src.NewProfile.UserId;

            return dest;
        }

        protected abstract IKernel 
        CreateContainer();
    }
}

//  ##########################################################################
