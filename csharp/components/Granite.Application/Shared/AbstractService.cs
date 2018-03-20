//  ##########################################################################
//  # File Name: AbstractService.cs
//  # Copyright: 2018, Sapientia Systems, LLC.
//  ##########################################################################

using Granite.ProvidedService.Shared;
using Strata.Foundation.Logging;
using Strata.Persistence.UnitOfWork;
using System;

namespace Granite.Application.Shared
{
    //////////////////////////////////////////////////////////////////////////
    /// <summary>
    /// </summary>
    ///  
    public abstract
    class AbstractService
    {
        protected IUnitOfWorkProvider Provider { get; set; }
        protected ILogger             Logger { get; set; }

        private static readonly DateTime theirEpoch = 
            new DateTime(1970,1,1,0,0,0,DateTimeKind.Utc);

        //////////////////////////////////////////////////////////////////////////
        /// <summary>
        /// Initializes a new instance of <code>AbstractService</code>.
        /// </summary>
        ///  
        protected
        AbstractService(IUnitOfWorkProvider provider,ILogger logger)
        {
            Provider = provider;
            Logger   = logger;
        }

        //////////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        protected TReply
        Execute<TReply,TRequest>(
            Func<TRequest,TReply>      action,
            Action<TRequest,Exception> exceptionLogger,
            TRequest                   request)
            where TRequest: GraniteRequest
            where TReply: GraniteReply
        {
            IUnitOfWork unitOfWork = null;

            try
            {
                //CheckPermission(request);
                unitOfWork = Provider.GetUnitOfWork();

                try
                {
                    TReply reply = action(request);

                    Commit(unitOfWork,request);

                    try
                    {
                        reply.ExecutionMilliseconds = 
                            GetCurrentTimeInMilliseconds() -
                            request.ReceivedTimestamp;
                        return reply;
                    }
                    catch (Exception e)
                    {
                        exceptionLogger(request,e);
                        throw;
                    }
                }
                catch (Exception e)
                {
                    Rollback(unitOfWork,request);
                    exceptionLogger(request,e);
                    throw;
                }
            }
            catch (Exception e)
            {
                exceptionLogger(request,e);
                throw;
            }


        }

        //////////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private void 
        Commit<TRequest>(IUnitOfWork unitOfWork,TRequest request)
        {
            throw new NotImplementedException();
        }

        //////////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private void 
        Rollback<TRequest>(IUnitOfWork unitOfWork,TRequest request) where TRequest : GraniteRequest
        {
            throw new NotImplementedException();
        }

        //////////////////////////////////////////////////////////////////////////
        /// <summary>
        /// </summary>
        ///  
        private long
        GetCurrentTimeInMilliseconds()
        {
            return (long)(DateTime.UtcNow - theirEpoch).TotalMilliseconds;
        }
    }
}

//  ##########################################################################
