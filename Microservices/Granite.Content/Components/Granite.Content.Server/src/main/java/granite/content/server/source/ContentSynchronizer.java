//////////////////////////////////////////////////////////////////////////////
// ContentSynchronizer.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.source;

import granite.content.server.application.ITransactionalContentCommandService;
import granite.content.service.requestreply.UpsertContentDocumentsReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.request.RequestContextHolder;
import strata.server.spring.inject.RequestAttributeMap;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public
class ContentSynchronizer
    implements ApplicationListener<ContextRefreshedEvent>
{
    private final List<IContentSource>                                 sources;
    private final ObjectProvider<ITransactionalContentCommandService> commandService;
    private final AtomicBoolean                                        done;
    private final Logger                                               logger;

    public
    ContentSynchronizer(List<IContentSource> sources,ObjectProvider<ITransactionalContentCommandService> commandService)
    {
        this.sources        = sources;
        this.commandService = commandService;
        this.done           = new AtomicBoolean(false);
        this.logger         = LogManager.getLogger(ContentSynchronizer.class);
    }

    @Override
    public void
    onApplicationEvent(ContextRefreshedEvent event)
    {
        if (done.compareAndSet(false,true))
            synchronize();
    }

    public void
    synchronize()
    {
        for (IContentSource source : sources)
        {
            boolean owned = RequestContextHolder.getRequestAttributes() == null;

            if (owned)
                RequestContextHolder.setRequestAttributes(new RequestAttributeMap());

            try
            {
                UpsertContentDocumentsReply reply =
                    commandService
                        .getObject()
                        .synchronizeSource(source.getName(),source.load())
                        .toCompletableFuture()
                        .join();

                if (reply.isSuccess())
                    logger.info("Content source {} synchronized: {} document(s)",source.getName(),reply.getUpsertedDocuments().size());
                else
                    logger.warn("Content source {} not synchronized: {}",source.getName(),reply.getFailureMessage());
            }
            catch (RuntimeException e)
            {
                logger.warn("Content source {} failed: {}",source.getName(),e.getMessage());
            }
            finally
            {
                if (owned)
                    RequestContextHolder.resetRequestAttributes();
            }
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
