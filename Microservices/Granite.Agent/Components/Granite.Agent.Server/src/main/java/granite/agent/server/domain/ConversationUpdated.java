//////////////////////////////////////////////////////////////////////////////
// ConversationUpdated.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.domain;

public
class ConversationUpdated
    extends AbstractConversationEvent
{
    public
    ConversationUpdated(Conversation src)
    {
        super("ConversationUpdated",src);
    }

    public
    ConversationUpdated(String correlId,Conversation src)
    {
        super("ConversationUpdated",correlId,src);
    }
}

//////////////////////////////////////////////////////////////////////////////
