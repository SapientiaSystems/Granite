//  ##########################################################################
//  # File Name: ActiveDirectoryUserService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.RequiredService.User;
using Microsoft.Azure.ActiveDirectory.GraphClient;
using Newtonsoft.Json;
using Strata.Foundation.Mapper;
using System;
using System.Collections.Generic;
using SystemEx.Injection;

namespace Granite.ServerPlatform.ActiveDirectory
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class ActiveDirectoryUserService:
        AbstractExternalUserService
    {
        private ActiveDirectoryGraphClient itsClient;
        private IObjectMapper<User,string> itsMapper;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        ActiveDirectoryUserService(ActiveDirectoryGraphClient client)
        {
            itsClient = client;
            itsMapper = 
                new JsonObjectMapper<User>(
                    new JsonSerializer()
                    {
                        ContractResolver = new ActiveDirectoryContractResolver(),
                        NullValueHandling = NullValueHandling.Ignore,
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

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override CreateExternalUserReply 
        CreateExternalUser(CreateExternalUserRequest request)
        {
            try
            {
                User newUser = new User();

                Validator.Validate(request);

                newUser = SetRequiredProperties(newUser,request);
                newUser = SetOptionalProperties(newUser,request);
                newUser =
                    itsMapper.ToObject<User>(
                        itsClient.CreateUser(
                            itsMapper.ToPayload(newUser)));

                return
                    new CreateExternalUserReply()
                    {
                        ExternalUserId = newUser.ObjectId,
                        IsSuccessful = true
                    };
            }
            catch (Exception e)
            {
                return
                    new CreateExternalUserReply()
                    {
                        IsSuccessful = false,
                        FailureMessage = e.Message,
                        StackTrace = e.StackTrace
                    };
            }
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override UpdateExternalUserReply 
        UpdateExternalUser(UpdateExternalUserRequest request)
        {
            try
            {
                string output = null;
                User   updatedUser = new User();

                updatedUser.RefreshTokensValidFromDateTime = null;
                Validator.Validate(request);

                updatedUser = SetUpdatableProperties(updatedUser,request);
                output =        
                    itsClient.UpdateUser(
                        request.ExternalUserId,
                        itsMapper.ToPayload(updatedUser));

                if (string.IsNullOrEmpty(output))
                    return
                        new UpdateExternalUserReply()
                        {
                            UserId = request.UserId,
                            ExternalUserId = request.ExternalUserId,
                            IsSuccessful = true
                        };
                else
                    return
                        new UpdateExternalUserReply()
                        {
                            UserId = request.UserId,
                            ExternalUserId = request.ExternalUserId,
                            IsSuccessful = false,
                            FailureMessage = output
                        };
            }
            catch (Exception e)
            {
                return
                    new UpdateExternalUserReply()
                    {
                        IsSuccessful = false,
                        FailureMessage = e.Message,
                        StackTrace = e.StackTrace
                    };
            }
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public override DeleteExternalUserReply 
        DeleteExternalUser(DeleteExternalUserRequest request)
        {
            try
            {
                string output = null;

                Validator.Validate(request);

                output = itsClient.DeleteUser(request.ExternalUserId);

                if (string.IsNullOrEmpty(output))
                    return
                        new DeleteExternalUserReply()
                        {
                            UserId         = request.UserId,
                            ExternalUserId = request.ExternalUserId,
                            IsSuccessful   = true
                        };
                else
                    return
                        new DeleteExternalUserReply()
                        {
                            UserId = request.UserId,
                            ExternalUserId = request.ExternalUserId,
                            IsSuccessful = false,
                            FailureMessage = output
                        };
            }
            catch (Exception e)
            {
                return
                    new DeleteExternalUserReply()
                    {
                        UserId = request.UserId,
                        ExternalUserId = request.ExternalUserId,
                        IsSuccessful = false,
                        FailureMessage = e.Message,
                        StackTrace = e.StackTrace
                    };

            }


        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private User
        SetRequiredProperties(User user,CreateExternalUserRequest request)
        {
            user.SignInNames = new List<SignInName>();

            if (!string.IsNullOrEmpty(request.UserName))
            {
                user.SignInNames.Add(
                    new SignInName()
                    {
                        Type = "userName",
                        Value = request.UserName
                    });
                user.DisplayName = request.UserName;
            }
            else
            {
                user.SignInNames.Add(
                    new SignInName()
                    {
                        Type = "emailAddress",
                        Value = request.Email.ToString()
                    });
                user.DisplayName = request.Email.ToString();
            }

            if ( request.Email != null)
                user.OtherMails =
                    new List<string>() { request.Email.ToString() };

            user.PasswordProfile =
                new PasswordProfile()
                {
                    Password = request.Password,
                    ForceChangePasswordNextLogin = false
                };
            user.CreationType = "LocalAccount";
            user.AccountEnabled = true;

            return user;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private User
        SetOptionalProperties(User user,CreateExternalUserRequest request)
        {
            if (request.JobTitle != null)
                user.JobTitle = request.JobTitle;

            if (request.Name != null)
            {
                user.GivenName = request.Name.FirstName;
                user.Surname = request.Name.LastName;
            }

            if (request.Address != null)
            {
                user.StreetAddress = request.Address.Address;
                user.City = request.Address.City;
                user.State = request.Address.State;
                user.Country = request.Address.CountryCode;
                user.PostalCode = request.Address.PostalCode;
            }

        
            if (request.Phone != null)
                user.TelephoneNumber = request.Phone.ToString();

            return user;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private User
        SetUpdatableProperties(User user,UpdateExternalUserRequest request)
        {
            if (request.Email != null)
                user.OtherMails =
                    new List<string>() { request.Email.ToString() };

            if (!string.IsNullOrEmpty(request.Password))
                user.PasswordProfile =
                    new PasswordProfile()
                    {
                        Password = request.Password,
                        ForceChangePasswordNextLogin = false
                    };
            if (request.JobTitle != null)
                user.JobTitle = request.JobTitle;

            if (request.Name != null)
            {
                user.GivenName = request.Name.FirstName;
                user.Surname = request.Name.LastName;
            }

            if (request.Address != null)
            {
                user.StreetAddress = request.Address.Address;
                user.City = request.Address.City;
                user.State = request.Address.State;
                user.Country = request.Address.CountryCode;
                user.PostalCode = request.Address.PostalCode;
            }

            if (request.Phone != null)
                user.TelephoneNumber = request.Phone.ToString();

            return user;
        }


    }
}

//  ##########################################################################
