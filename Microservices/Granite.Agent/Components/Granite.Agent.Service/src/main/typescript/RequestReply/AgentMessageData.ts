import type { AgentMessageRole } from "./AgentMessageRole";

export
interface AgentMessageData
{
    seq?: number;
    role?: AgentMessageRole;
    text?: string;
    toolName?: string;
    isError?: boolean;
    at?: string;
}
