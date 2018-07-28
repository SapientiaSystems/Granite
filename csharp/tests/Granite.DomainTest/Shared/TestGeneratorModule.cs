//  ##########################################################################
//  # File Name: TestGeneratorModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.Common.Verification;
using Strata.Foundation.Injection;
using System;

namespace Granite.Domain.Shared
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class TestGeneratorModule:
        AbstractModule
    {
        public
        TestGeneratorModule():
            base("TestGeneratorModule") {}

        public override void 
        Initialize()
        {
            /*
            Bind<IVerificationCodeGenerator>()
                .ToType<VerificationCodeGenerator>()
                .WithScope(DefaultScope);
                */
            Bind<ITestUserGenerator>()
                .ToType<TestUserGenerator>()
                .WithScope(DefaultScope);
        }
    }
}

//  ##########################################################################
