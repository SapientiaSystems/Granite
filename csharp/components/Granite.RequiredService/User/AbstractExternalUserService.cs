//  ##########################################################################
//  # File Name: AbstractExternalUserService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;

namespace Granite.RequiredService.User
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public abstract
    class AbstractExternalUserService:
        IExternalUserService
    {
        protected IExternalUserRequestValidator Validator { get; set; }

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected
        AbstractExternalUserService():
            this(new DefaultExternalUserRequestValidator()) {}

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected
        AbstractExternalUserService(IExternalUserRequestValidator validator)
        {
            Validator = validator;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public abstract CreateExternalUserReply 
        CreateExternalUser(CreateExternalUserRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public abstract UpdateExternalUserReply 
        UpdateExternalUser(UpdateExternalUserRequest request);

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public abstract DeleteExternalUserReply 
        DeleteExternalUser(DeleteExternalUserRequest request);
    }
}

//  ##########################################################################
