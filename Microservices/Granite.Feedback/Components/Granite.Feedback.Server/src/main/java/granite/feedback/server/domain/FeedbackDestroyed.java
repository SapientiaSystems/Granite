//////////////////////////////////////////////////////////////////////////////
// FeedbackDestroyed.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.domain;

public
class FeedbackDestroyed
    extends AbstractFeedbackEvent
{
    public
    FeedbackDestroyed(Feedback src)
    {
        super("FeedbackDestroyed",src);
    }

    public
    FeedbackDestroyed(String correlId,Feedback src)
    {
        super("FeedbackDestroyed",correlId,src);
    }
}

//////////////////////////////////////////////////////////////////////////////
