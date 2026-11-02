//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AgentTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }                                  from "vitest";
import { MAX_MESSAGE_LENGTH, WELCOME, pendingAction, toEntries,
         validateMessage }                                       from "granite.web.agent/Chat";
import type { ConversationData }                                 from "granite.agent.service/RequestReply";

describe("AgentChat",() =>
{
    it("greets when there is no conversation yet",() =>
    {
        expect(toEntries(null)).toEqual([{ key: "welcome", kind: "assistant", text: WELCOME, isError: false }]);
    });

    it("maps transcript roles to chat entries",() =>
    {
        const conversation: ConversationData =
        {
            conversationId: 3,
            messages:
            [
                { seq: 1, role: "USER",      text: "How do I end a session?" },
                { seq: 2, role: "TOOL",      text: "Search help for \"end session\"", toolName: "search_help_content", isError: false },
                { seq: 3, role: "ASSISTANT", text: "Open **Sessions** and choose End." },
                { seq: 4, role: "TOOL",      text: "the tool failed", isError: true },
            ],
        };

        expect(toEntries(conversation).map(e => [e.kind, e.isError])).toEqual([["user",false],["step",false],["assistant",false],["step",true]]);
        expect(pendingAction(conversation)).toBeNull();
    });

    it("exposes the pending action",() =>
    {
        const conversation: ConversationData =
            { conversationId: 3, messages: [], pendingAction: { toolUseId: "toolu_1", toolName: "end_sessions", summary: "End 2 sessions" } };

        expect(pendingAction(conversation)?.toolUseId).toBe("toolu_1");
    });

    it("validates the message",() =>
    {
        expect(validateMessage("  ")).toBe("Enter a message");
        expect(validateMessage("x".repeat(MAX_MESSAGE_LENGTH + 1))).not.toBeNull();
        expect(validateMessage(" Hello ")).toBeNull();
    });
});

//////////////////////////////////////////////////////////////////////////////
