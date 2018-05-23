//  ##########################################################################
//  # File Name: ITestUserGenerator.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Domain.User;
using Strata.Foundation.Value;
using System;

namespace Granite.Domain.Shared
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface ITestUserGenerator
    {
        string
        GenerateUserName();

        string
        GeneratePassword();

        string
        GenerateExternalUserId();

        EmailAddress
        GenerateEmail();

        PersonName
        GenerateName();

        PostalAddress
        GenerateAddress();

        PhoneNumber
        GeneratePhone();

        IUser
        GenerateUser();
    }
}

//  ##########################################################################
