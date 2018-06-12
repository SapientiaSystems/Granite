//  ##########################################################################
//  # File Name: RandomStringGeneratorTest.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.Linq;

namespace Granite.Common.Utility
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    [TestFixture]
    public
    class RandomStringGeneratorTest
    {
        [Test]
        public void 
        TestGenerate()
        {
            IRandomValueGenerator<string> generator = 
                new RandomStringGenerator();
            ISet<string> results = new HashSet<string>();

            for (int i=0;i<1000000;++i)
            {
                string result = generator.Generate();

                Assert.IsFalse(results.Contains(result));
                results.Add(result);
            }

        }
    }
}

//  ##########################################################################
