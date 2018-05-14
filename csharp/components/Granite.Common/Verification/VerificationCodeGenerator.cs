//  ##########################################################################
//  # File Name: VerificationCodeGenerator.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;

namespace Granite.Common.Verification
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class VerificationCodeGenerator:
        IVerificationCodeGenerator
    {
        private Random itsRandom;

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        VerificationCodeGenerator()
        {
            itsRandom = new Random();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public string 
        Generate()
        {
            return itsRandom.Next(100000,999999).ToString();
        }
    }
}

//  ##########################################################################
