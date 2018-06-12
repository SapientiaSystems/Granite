//  ##########################################################################
//  # File Name: MockUserIdProvider.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;

namespace Granite.Domain.Shared
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class MockUserIdProvider:
        IUserIdProvider
    {
        public Guid UserId { get; protected set; }

        public
        MockUserIdProvider():
            this(Guid.NewGuid()) {}

        public
        MockUserIdProvider(Guid userId)
        {
            UserId = userId;
        }
    }
}

//  ##########################################################################
