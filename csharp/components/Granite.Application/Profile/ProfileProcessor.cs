//  ##########################################################################
//  # File Name: ProfileProcessor.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using AutoMapper;
using Granite.Application.Service;
using Granite.Common.Access;
using Granite.Domain.Profile;
using Granite.Domain.Session;
using Granite.Domain.User;
using Granite.ProvidedService.Profile;
using Granite.ProvidedService.Transfer;
using Strata.Domain.UnitOfWork;
using Strata.Foundation.Logging;
using System;
using SystemEx.Injection;

namespace Granite.Application.Profile
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class ProfileProcessor:
        AuthorizedService,
        IProfileProcessor
    {
        private IProfileRepository itsProfileRepository;
        private IUserRepository    itsUserRepository;
        private IMapper            itsMapper;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        ProfileProcessor(
            IUnitOfWorkProvider provider,
            ILogger             logger,
            ISessionRepository  sessionRepository,
            IProfileRepository  profileRepository,
            IUserRepository     userRepository,
            IMapper             mapper):
            base(provider,logger,sessionRepository)
        {
            itsProfileRepository = profileRepository;
            itsUserRepository    = userRepository;
            itsMapper            = mapper;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public CreateProfileReply
        CreateProfile(CreateProfileRequest request)
        {
            IUser                     user    = null;
            IProfile                  profile = null;
            CreateOrUpdateProfileData profileData = request.ProfileToCreate;

            CheckPermission(request,profileData);
            user = GetUser(profileData);

            if (user == null)
                return
                    new CreateProfileReply()
                    {
                        IsSuccessful = false,
                        FailureMessage =
                            string.Format(
                                "Could not create profile for user id {0}: " +
                                "user does not exist.",
                                profileData.UserId)
                    };

            if (itsProfileRepository.HasUniqueFor(user))
                return
                    new CreateProfileReply()
                    {
                        IsSuccessful = false,
                        FailureMessage =
                            string.Format(
                                "Could not create profile for user id {0}: " +
                                "a profile already exists.",
                                profileData.UserId)
                    };

            profile = new StandardProfile();
            profile = itsMapper.Map(user,itsMapper.Map(profileData,profile));            
            profile = itsProfileRepository.Insert(profile);

            return
                new CreateProfileReply()
                {
                    IsSuccessful = true,
                    SuccessMessage = "Profile has been created.",
                    NewProfile = itsMapper.Map(profile,new ProfileData())
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public UpdateProfileReply
        UpdateProfile(UpdateProfileRequest request)
        {
            IUser user = null;
            IProfile profile = null;
            CreateOrUpdateProfileData profileData = request.ProfileToUpdate;

            CheckPermission(request,profileData);
            user = GetUser(profileData);

            if (user == null)
                return
                    new UpdateProfileReply()
                    {
                        IsSuccessful = false,
                        FailureMessage =
                            string.Format(
                                "Could not update profile for user id {0}: " +
                                "user does not exist.",
                                profileData.UserId)
                    };

            profile = itsProfileRepository.GetUniqueFor(user);

            if (profile == null)
                return
                    new UpdateProfileReply()
                    {
                        IsSuccessful = false,
                        FailureMessage =
                            string.Format(
                                "Could not update profile for user id {0}: " +
                                "profile not found.",
                                profileData.UserId)
                    };

            profile =
                itsMapper.Map(
                    profileData,
                    profile,
                    IgnoreNullProperties());

            profile = itsProfileRepository.Update(profile);

            return
                new UpdateProfileReply()
                {
                    IsSuccessful = true,
                    SuccessMessage = "Profile has been updated.",
                    UpdatedProfile = itsMapper.Map(profile,new ProfileData())
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public DeleteProfileReply
        DeleteProfile(DeleteProfileRequest request)
        {
            IProfile profile = GetProfileToDelete(request);

            if (profile != null)
                return
                    new DeleteProfileReply()
                    {
                        IsSuccessful = true,
                        SuccessMessage = "profile does not exist"
                    };

            itsProfileRepository.Remove(profile);

            return 
                new DeleteProfileReply()
                {
                    IsSuccessful = true,
                    DeletedProfileId = profile.PrimaryId,
                    SuccessMessage = "profile has been deleted"
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public GetProfileReply
        GetProfile(GetProfileRequest request)
        {
            IProfile profile = null;

            CheckPermission(request);
            profile = GetProfileToReturn(request);

            if (profile == null)
                return
                    new GetProfileReply()
                    {
                        IsSuccessful = false,
                        FailureMessage = "profile not found"
                    };

            return 
                new GetProfileReply()
                {
                    IsSuccessful = true,
                    RequestedProfile = 
                        itsMapper.Map(profile,new ProfileData()),
                    SuccessMessage = "returned requested profile"
                };
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual void
        CheckPermission(
            GraniteRequest            request,
            CreateOrUpdateProfileData profile)
        {
            IUser user = GetUserFrom(request);

            if (user.UserId != profile.UserId)
                if (!user.IsInRole("GraniteAdministrator"))
                    throw
                        new AuthorizationException(
                            "User is not in permissable role: " +
                            "GraniteAdministrator");
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual IUser
        GetUser(CreateOrUpdateProfileData input)
        {
            if (input.UserId != Guid.Empty)
                return itsUserRepository.GetUnique(input.UserId);

            if (!string.IsNullOrEmpty(input.UserName))
                return
                    itsUserRepository.GetUniqueWithUserName(input.UserName);

            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected virtual IProfile
        GetProfileToDelete(DeleteProfileRequest request)
        {
            if (request.ProfileIdToDelete != Guid.Empty)
                return
                    itsProfileRepository.GetUnique(request.ProfileIdToDelete);

            if (request.UserIdOfProfileToDelete != Guid.Empty)
            {
                IUser user = 
                    itsUserRepository.GetUnique(
                        request.UserIdOfProfileToDelete);

                if (user != null)
                    return itsProfileRepository.GetUniqueFor(user);
            }

             return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected void
        CheckPermission(GetProfileRequest request)
        {
            IUser user = GetUserFrom(request);

            if (user == null)
                throw
                    new AuthorizationException(
                        "Invalid user: could not find user from request");

            if (request.GetProfileOfCurrentUser)
            {
                if (!user.IsInRole("GraniteUser"))
                    throw
                        new AuthorizationException(
                            "User is not in permissable role: GraniteUser");

                return;
            }

            if (!user.IsInRole("GraniteAdministrator"))
                throw
                    new AuthorizationException(
                        "User is not in permissable role: " +
                        "GraniteAdministrator");


        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected IProfile
        GetProfileToReturn(GetProfileRequest request)
        {
            if (request.GetProfileOfCurrentUser)
            {
                IUser user = GetUserFrom(request);

                if (user != null)
                    return itsProfileRepository.GetUniqueFor(user); 
            }

            if (request.ProfileIdOfProfileToGet != Guid.Empty)
                return 
                    itsProfileRepository.GetUnique(
                        request.ProfileIdOfProfileToGet);

            if (request.UserIdOfProfileToGet != Guid.Empty)
            {
                IUser user =
                    itsUserRepository.GetUnique(
                        request.UserIdOfProfileToGet);

                if (user != null)
                    return itsProfileRepository.GetUniqueFor(user);
            }

            return null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private 
        Action<IMappingOperationOptions<CreateOrUpdateProfileData,IProfile>>
        IgnoreNullProperties()
        {
            return
                opt => opt
                    .ConfigureMap()
                    .ForAllMembers(
                        opt2 => opt2.Condition(
                            (src,dest,sourceMember)
                                => sourceMember != null));
        }
    }
}

//  ##########################################################################
