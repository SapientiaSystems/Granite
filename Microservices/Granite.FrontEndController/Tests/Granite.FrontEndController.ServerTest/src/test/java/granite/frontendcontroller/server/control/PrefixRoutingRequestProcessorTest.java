//////////////////////////////////////////////////////////////////////////////
// PrefixRoutingRequestProcessorTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control;

import granite.frontendcontroller.service.requestreply.LoginInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import strata.frontendcontroller.server.application.IRequestProcessor;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.util.Map;

@Tag("CommitStage")
public
class PrefixRoutingRequestProcessorTest
{
    private IRequestProcessor            session;
    private IRequestProcessor            fallback;
    private PrefixRoutingRequestProcessor subject;

    @BeforeEach
    public void
    setUp()
    {
        session = Mockito.mock(IRequestProcessor.class);
        fallback = Mockito.mock(IRequestProcessor.class);
        subject = new PrefixRoutingRequestProcessor(Map.of("Session.",session),fallback);
    }

    @Test
    public void
    sessionSourcesGoToTheSessionProcessor()
    {
        HandleInputRequest input = new HandleInputRequest().setInput(new LoginInput().setSource("Session.New"));
        GetOutputRequest   output = new GetOutputRequest().setSource("Session.Current");

        subject.process(input);
        subject.process(output);

        Mockito.verify(session).process(input);
        Mockito.verify(session).process(output);
        Mockito.verifyNoInteractions(fallback);
    }

    @Test
    public void
    otherSourcesGoToTheFallback()
    {
        HandleInputRequest input = new HandleInputRequest().setInput(new LoginInput().setSource("User.All"));
        GetOutputRequest   output = new GetOutputRequest().setSource("User.All");
        GetOutputRequest   none = new GetOutputRequest();

        subject.process(input);
        subject.process(output);
        subject.process(none);

        Mockito.verify(fallback).process(input);
        Mockito.verify(fallback).process(output);
        Mockito.verify(fallback).process(none);
        Mockito.verifyNoInteractions(session);
    }
}

//////////////////////////////////////////////////////////////////////////////
