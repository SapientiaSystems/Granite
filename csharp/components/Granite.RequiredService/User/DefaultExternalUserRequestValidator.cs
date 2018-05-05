//  ##########################################################################
//  # File Name: DefaultExternalUserRequestValidator.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Validation;
using Strata.Foundation.Value;
using System;

namespace Granite.RequiredService.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class DefaultExternalUserRequestValidator:
        IExternalUserRequestValidator
    {
        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public void 
        Validate(CreateExternalUserRequest request)
        {
            if (!CheckUserNameOrEmailExists(request.UserName,request.Email))
                throw
                    new ValidationFailedException(
                        "UserName and Email cannot both be null");

            if (!CheckUserNameIsNotEmail(request.UserName))
                throw
                    new ValidationFailedException(
                        "UserName ({0}) cannot be an email address",
                        request.UserName);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public void 
        Validate(UpdateExternalUserRequest request)
        {
            if (!CheckUserId(request.UserId))
                throw
                    new ValidationFailedException("UserId cannot be null");

            if (!CheckExternalUserId(request.ExternalUserId))
                throw
                    new ValidationFailedException(
                        "ExternalUserId cannot be null");
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public void 
        Validate(DeleteExternalUserRequest request)
        {
            if (!CheckUserId(request.UserId))
                throw
                    new ValidationFailedException("UserId cannot be null");

            if (!CheckExternalUserId(request.ExternalUserId))
                throw
                    new ValidationFailedException(
                        "ExternalUserId cannot be null");
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private bool
        CheckUserNameOrEmailExists(string userName,EmailAddress email)
        {
            return !(string.IsNullOrEmpty(userName) && email == null);
               
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private bool
        CheckUserNameIsNotEmail(string userName)
        {
            try
            {
                new EmailAddress(userName);
                return false;
            }
            catch (Exception)
            {
                return true;
            }
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private bool
        CheckUserId(Guid userId)
        {
            return userId != null;
        }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private bool
        CheckExternalUserId(string externalUserId)
        {
            return !string.IsNullOrEmpty(externalUserId);
        }
    }
}

//  ##########################################################################
