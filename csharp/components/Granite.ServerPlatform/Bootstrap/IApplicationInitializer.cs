//  ##########################################################################
//  # File Name: IApplicationInitializer.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################


namespace Granite.ServerPlatform.Bootstrap
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// Initializes data and other elements of application.
    /// </summary>
    ///  
    public
    interface IApplicationInitializer
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Initializes the application.
        /// </summary>
        ///
        void
        Initialize();
    }
}

//  ##########################################################################
