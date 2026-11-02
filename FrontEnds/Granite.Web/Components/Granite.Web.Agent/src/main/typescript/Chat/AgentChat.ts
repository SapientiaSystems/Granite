//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Agent
//////////////////////////////////////////////////////////////////////////////

import type { AgentMessageData, AgentPendingActionData,
              ConversationData }                       from "granite.agent.service/RequestReply";

export const MAX_MESSAGE_LENGTH: number = 4000;

export const WELCOME: string = "Hello! I am the virtual assistant. Ask me how to do something in the application, or ask me to look something up for you.";

export interface IChatEntry
{
    readonly key:     string;
    readonly kind:    "user" | "assistant" | "step";
    readonly text:    string;
    readonly isError: boolean;
}

export function toEntries(conversation: ConversationData | null | undefined): Array<IChatEntry>
{
    const messages: Array<AgentMessageData> = conversation?.messages ?? [];

    if (messages.length === 0)
    {
        return [{ key: "welcome", kind: "assistant", text: WELCOME, isError: false }];
    }

    return messages.map((message, index) =>
    ({
        key:     String(message.seq ?? index),
        kind:    message.role === "USER" ? "user" : message.role === "ASSISTANT" ? "assistant" : "step",
        text:    message.text ?? "",
        isError: message.isError === true,
    }));
}

export function pendingAction(conversation: ConversationData | null | undefined): AgentPendingActionData | null
{
    return conversation?.pendingAction ?? null;
}

export function validateMessage(text: string): string | null
{
    const trimmed: string = text.trim();

    if (trimmed === "")
    {
        return "Enter a message";
    }

    if (trimmed.length > MAX_MESSAGE_LENGTH)
    {
        return "Message is longer than " + MAX_MESSAGE_LENGTH + " characters";
    }

    return null;
}

//////////////////////////////////////////////////////////////////////////////
