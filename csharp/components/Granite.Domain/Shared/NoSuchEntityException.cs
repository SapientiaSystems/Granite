//  ##########################################################################
//  # File Name: NoSuchEntityException.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using System;

namespace Granite.Domain.Shared
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    [Serializable]
    public
    class NoSuchEntityException:
        Exception
    {
        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        NoSuchEntityException(Type type,Guid key) :
            this(type,key.ToString()) {}

        //////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        public
        NoSuchEntityException(Type type,string key) :
            base(
                string.Format(
                    "No such entity of type {0} and key {1} was found.",
                    type.FullName,
                    key)) {}
    }
}

//  ##########################################################################
