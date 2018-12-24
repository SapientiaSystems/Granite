//  ##########################################################################
//  # File Name: TestGeneratorModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Ninject.Injection;

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
        TestGeneratorModule() {}

        public override void 
        Load()
        {
            /*
            Bind<IVerificationCodeGenerator>()
                .ToType<VerificationCodeGenerator>()
                .WithScope(DefaultScope);
                */
            Bind<ITestUserGenerator>()
                .To<TestUserGenerator>()
                .InDefaultScope();
        }
    }
}

//  ##########################################################################
