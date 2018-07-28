//  ##########################################################################
//  # File Name: RegistrationService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Registration;
using SystemEx.Injection;

namespace Granite.Application.Registration
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class RegistrationService:
        IRegistrationService
    {
        private IRegistrationProcessor itsProcessor;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        [Inject]
        public
        RegistrationService(IRegistrationProcessor processor)
        {
            itsProcessor = processor;
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public RegisterReply 
        Register(RegisterRequest request)
        {
            return itsProcessor.Register(request);
        }
    }
}

//  ##########################################################################
