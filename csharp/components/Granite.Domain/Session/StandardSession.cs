//  ##########################################################################
//  # File Name: StandardSession.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Utility;
using Granite.Domain.User;
using Strata.Domain.Shared;
using System;

namespace Granite.Domain.Session
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class StandardSession:
        AbstractEntity<
            Guid,
            ISession,
            ISessionDomainEvent,
            ISessionDomainEventObserver>,
        ISession
    {
        public virtual Guid     UserId { get; set; }

        public virtual string   ClientIpAddress { get; set; }
        public virtual bool     
        HasClientIpAddress
            { get { return !string.IsNullOrEmpty(ClientIpAddress); } }

        public virtual IUser    User { get; protected set; }
        public virtual bool     IsUserValid { get { return User != null; } }
        public virtual bool     IsOpen { get; set; }

        public virtual string   AccessToken { get; set; }

        public virtual DateTime LastRefresh { get; set; }
        public virtual DateTime LastRequest { get; set; }

        private IRandomValueGenerator<string> itsGenerator;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        StandardSession():
            this(null) {}

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        StandardSession(IUser user)
        {
            User   = user;

            if (User != null)
                UserId = User.UserId;

            IsOpen = false;
            itsGenerator = new RandomStringGenerator();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void
        ActivateTenant()
        {
            if (!IsUserValid)
                throw
                    new InvalidOperationException(
                        "Session does not have a valid user.");

            User.ActivateTenant();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void
        DeactivateTenant()
        {
            if (!IsUserValid)
                throw
                    new InvalidOperationException(
                        "Session does not have a valid user.");

            User.DeactivateTenant();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void
        Open()
        {
            AccessToken  = itsGenerator.Generate();
            LastRefresh  = DateTime.Now;
            LastModified = LastRefresh;
            IsOpen       = true;

            Notify(new SessionOpenedEvent(this));
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void
        Refresh()
        {
            AccessToken  = itsGenerator.Generate();
            LastRefresh  = DateTime.Now;
            LastModified = LastRefresh;

            Notify(new SessionRefreshedEvent(this));
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void
        Close()
        {
            IsOpen = false;
            AccessToken = null;
            Notify(new SessionClosedEvent(this));
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public virtual void
        Initialize(IUserRepository userRepository)
        {
            User = userRepository.GetUnique(UserId);
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        protected override ISession
        GetSelf()
        {
            return this;
        }
    }
}

//  ##########################################################################
