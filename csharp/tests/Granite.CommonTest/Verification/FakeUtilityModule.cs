//  ##########################################################################
//  # File Name: FakeUtilityModule.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Ninject.Injection;

namespace Granite.Common.Verification
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    class FakeUtilityModule:
        AbstractModule
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        /// 
        public
        FakeUtilityModule() {}

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        /// 
        public override void
        Load()
        {
            Bind<IVerificationNotifier>()
                .To<FakeEmailSender>()
                .InDefaultScope();

            Bind<IVerificationCodeGenerator>()
                .To<VerificationCodeGenerator>()
                .InDefaultScope();
        }
    }
}

//  ##########################################################################
