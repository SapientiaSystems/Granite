//////////////////////////////////////////////////////////////////////////////
// ConversationCreated.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.domain;

public
class ConversationCreated
    extends AbstractConversationEvent
{
    public
    ConversationCreated(Conversation src)
    {
        super("ConversationCreated",src);
    }

    public
    ConversationCreated(String correlId,Conversation src)
    {
        super("ConversationCreated",correlId,src);
    }
}

//////////////////////////////////////////////////////////////////////////////
