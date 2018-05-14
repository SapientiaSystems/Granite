//  ##########################################################################
//  # File Name: IVerificationNotifier.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################


namespace Granite.Common.Verification
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// 
    /// </summary>
    ///  
    public
    interface IVerificationNotifier
    {
        void
        Notify(VerificationNotice notice);
    }
}

//  ##########################################################################
