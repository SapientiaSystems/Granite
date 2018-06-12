//  ##########################################################################
//  # File Name: RandomStringGenerator.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Strata.Foundation.Value;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Numerics;
using System.Text;

namespace Granite.Common.Utility
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public
    class RandomStringGenerator:
        IRandomValueGenerator<string>
    {
        public int            Length { get; protected set; }

        private Random        random;
        private byte[]        buffer;
        private StringBuilder builder;

        private static char[] alphabet = 
            new char[]
            {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
                'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
                'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
                'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
                'Y', 'Z'
            };

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        RandomStringGenerator():
            this(32) {}

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        RandomStringGenerator(int length)
        {
            Length = length;
            random = new Random();
            buffer = new byte[length];
            builder = new StringBuilder();
        }

        //////////////////////////////////////////////////////////////////////
        /// <inheritDoc/>
        ///  
        public string 
        Generate()
        {
            random.NextBytes(buffer);
            return ToBase62(buffer);
        }

        protected string 
        ToBase62(byte[] toConvert)
        {
            BigInteger dividend = new BigInteger(toConvert);
            int        i = 0;

            builder.Clear();

            while (dividend != 0 && i < Length)
            {
                BigInteger remainder;

                dividend = BigInteger.DivRem(dividend,62,out remainder);
                builder.Insert(0,alphabet[Math.Abs((int)remainder)]);
                ++i;
            }

            return builder.ToString();
        }
    }
}

//  ##########################################################################
