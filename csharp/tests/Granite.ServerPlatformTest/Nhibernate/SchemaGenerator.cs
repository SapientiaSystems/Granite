//  ##########################################################################
//  # File Name: SchemaGenerator.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using NHibernate.Cfg;
using NHibernate.Tool.hbm2ddl;
using NUnit.Framework;

namespace Granite.ServerPlatform.Nhibernate
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    //[Ignore("use when needed regenerate schema")]
    [TestFixture]
    public
    class SchemaGenerator
    {
        [Test]
        public void
        GenerateSchema()
        {
            Configuration config = new Configuration();

            config
                .Configure()
                .AddAssembly("Granite.ServerPlatform");

            new SchemaExport(config).Create(true,true);
        }
    }
}

//  ##########################################################################
