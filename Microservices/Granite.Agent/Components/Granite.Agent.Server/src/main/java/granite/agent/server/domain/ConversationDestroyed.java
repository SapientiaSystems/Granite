//////////////////////////////////////////////////////////////////////////////
// ConversationDestroyed.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.domain;

public
class ConversationDestroyed
    extends AbstractConversationEvent
{
    public
    ConversationDestroyed(Conversation src)
    {
        super("ConversationDestroyed",src);
    }

    public
    ConversationDestroyed(String correlId,Conversation src)
    {
        super("ConversationDestroyed",correlId,src);
    }
}

//////////////////////////////////////////////////////////////////////////////
