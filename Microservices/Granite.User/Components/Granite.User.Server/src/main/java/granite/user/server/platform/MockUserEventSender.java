//////////////////////////////////////////////////////////////////////////////
// MockUserEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.platform;

import granite.user.service.event.IUserEventSender;
import granite.user.service.event.UserEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import strata.foundation.core.event.CompletableSendResult;
import strata.foundation.core.event.ICompletableSendResult;
import strata.foundation.core.event.IEventSender;
import strata.foundation.core.event.SendResult;

import java.util.concurrent.CompletableFuture;

public
class MockUserEventSender
    implements IUserEventSender
{
    private final Logger logger;

    public
    MockUserEventSender()
    {
        logger = LogManager.getLogger(MockUserEventSender.class);
    }

    @Override
    public IEventSender<UserEvent>
    open() throws Exception
    {
        logger.info("open()");
        return this;
    }

    @Override
    public IEventSender<UserEvent>
    close() throws Exception
    {
        logger.info("close()");
        return this;
    }

    @Override
    public ICompletableSendResult<UserEvent>
    send(UserEvent event)
    {
        logger.info("send(" + event.toString() + ")");
        return
            new CompletableSendResult<>(
                CompletableFuture.completedFuture(new SendResult<>(event)));
    }

    @Override
    public boolean
    isOpen()
    {
        return false;
    }

    @Override
    public boolean
    isClosed()
    {
        return false;
    }
}

//////////////////////////////////////////////////////////////////////////////
