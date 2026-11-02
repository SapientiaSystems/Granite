//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Agent
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useRef, useState }                  from "react";
import type { CSSProperties, ReactElement }             from "react";
import { Alert, Button, Group, Loader, Paper, Stack,
         Text, TextInput, useComputedColorScheme }      from "@mantine/core";
import { GraniteFrontEndControllerServiceClient }       from "granite.frontendcontroller.client/RequestReply";
import type { IGraniteFrontEndControllerServiceClient } from "granite.frontendcontroller.client/RequestReply";
import { GetOutputRequestBuilder, HandleInputRequestBuilder } from "strata.frontendcontroller.service/RequestReply";
import type { AgentInput, GetAgentOutputReply,
              HandleAgentInputReply }                   from "granite.frontendcontroller.service/RequestReply";
import type { ConversationData }                        from "granite.agent.service/RequestReply";
import { useAuthentication }                            from "granite.web.authentication/Authentication";
import { MarkdownView }                                 from "granite.web.content/Markdown";
import { pendingAction, toEntries, validateMessage }    from "./AgentChat";

const SOURCE:      string = "Agent.Current";
const DESTINATION: string = "AgentView";
const SERVICE:     string = "frontendcontroller-user-service";

export interface IAgentChatViewProps
{
    readonly service?:   string;
    readonly className?: string;
    readonly style?:     CSSProperties;
}

export function AgentChatView(props: IAgentChatViewProps): ReactElement
{
    const service: string                 = props.service ?? SERVICE;
    const { state, createFrontEndClient } = useAuthentication();
    const signedIn: boolean               = state.status === "authenticated" || state.status === "reauthenticating";
    const dark: boolean                   = useComputedColorScheme("light") === "dark";
    const [conversation, setConversation] = useState<ConversationData | null>(null);
    const [text, setText]                 = useState<string>("");
    const [messages, setMessages]         = useState<Array<string>>([]);
    const [busy, setBusy]                 = useState<boolean>(false);
    const scrollRef                       = useRef<HTMLDivElement>(null);
    const entries                         = toEntries(conversation);
    const pending                         = pendingAction(conversation);

    const createClient = (): IGraniteFrontEndControllerServiceClient =>
        createFrontEndClient((url,fetchFunction) => new GraniteFrontEndControllerServiceClient(url,fetchFunction),service);

    useEffect(() =>
    {
        if (scrollRef.current != null)
        {
            scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
        }
    }, [conversation, busy]);

    useEffect(() =>
    {
        if (!signedIn)
        {
            return;
        }

        const client: IGraniteFrontEndControllerServiceClient = createClient();

        client
            .getAgentOutput(GetOutputRequestBuilder.newBuilder().setSource(SOURCE).setDestination(DESTINATION).build())
            .thenApply((reply: GetAgentOutputReply) =>
            {
                if (reply.success !== false && reply.output != null)
                {
                    setConversation(reply.output.conversation ?? null);
                    setMessages(reply.output.failureMessages ?? []);
                }

                return reply;
            })
            .exceptionally(() => ({} as GetAgentOutputReply))
            .subscribe(undefined,() => undefined);

        return () => { client.close(); };
    }, [signedIn]);

    function run(command: string, fields: Partial<AgentInput>): void
    {
        const client: IGraniteFrontEndControllerServiceClient = createClient();
        const input: AgentInput =
            { userId: null, source: SOURCE, command, destination: DESTINATION,
              conversationId: conversation?.conversationId ?? null, text: null, toolUseId: null, ...fields } as AgentInput;

        setBusy(true);
        setMessages([]);
        client
            .handleAgentInput(HandleInputRequestBuilder.newBuilder<AgentInput>().setInput(input).build())
            .thenApply((reply: HandleAgentInputReply) =>
            {
                client.close();
                setBusy(false);

                if (reply.success === false || reply.output == null)
                    setMessages([reply.failureMessage ?? "The assistant could not answer"]);
                else if ((reply.output.failureMessages ?? []).length > 0)
                    setMessages(reply.output.failureMessages);
                else
                    setConversation(reply.output.conversation ?? null);

                return reply;
            })
            .exceptionally((error: Error) =>
            {
                client.close();
                setBusy(false);
                setMessages([error?.message ?? "The assistant could not answer"]);
                return {} as HandleAgentInputReply;
            })
            .subscribe(undefined,(error: Error) =>
            {
                client.close();
                setBusy(false);
                setMessages([error?.message ?? "The assistant could not answer"]);
            });
    }

    function send(): void
    {
        const problem: string | null = validateMessage(text);

        if (problem != null)
        {
            setMessages([problem]);
            return;
        }

        run("Send",{ text: text.trim() });
        setText("");
    }

    function bubble(kind: string): CSSProperties
    {
        if (kind === "user")
            return { maxWidth: "75%", background: dark ? "#1971c2" : "#339af0" };

        return { maxWidth: "75%", background: dark ? "#4a4a4a" : "#f1f3f5" };
    }

    return (
        <Stack gap={0} className={props.className} style={{ flex: 1, minWidth: 0, minHeight: 0, ...props.style }}>
            <Group justify="space-between" px="md" py={6} style={{ flexShrink: 0 }}>
                <Text size="sm" fw={600}>{conversation?.title ?? "New conversation"}</Text>
                <Button variant="subtle" size="xs" disabled={busy || conversation == null} onClick={() => { setConversation(null); setMessages([]); }}>
                    New conversation
                </Button>
            </Group>
            <div ref={scrollRef} style={{ flex: 1, overflowY: "auto", padding: 16 }}>
                <Stack gap="sm">
                    {entries.map(entry =>
                        entry.kind === "step"
                            ? <Text key={entry.key} size="xs" c={entry.isError ? "red" : "dimmed"} style={{ paddingLeft: 8 }}>• {entry.text}</Text>
                            : <Group key={entry.key} justify={entry.kind === "user" ? "flex-end" : "flex-start"}>
                                  <Paper p="sm" radius="md" style={bubble(entry.kind)}>
                                      {entry.kind === "user"
                                          ? <Text size="sm" c="white" style={{ whiteSpace: "pre-wrap" }}>{entry.text}</Text>
                                          : <MarkdownView source={entry.text} />}
                                  </Paper>
                              </Group>)}
                    {busy && <Group gap="xs"><Loader size="xs" /><Text size="xs" c="dimmed">Thinking…</Text></Group>}
                    {pending != null && !busy &&
                    <Paper p="sm" radius="md" withBorder>
                        <Text size="sm" fw={600} mb={4}>Approve this action?</Text>
                        <Text size="sm" mb="sm">{pending.summary}</Text>
                        <Group gap="sm">
                            <Button size="xs" color="blue" onClick={() => run("Approve",{ toolUseId: pending.toolUseId ?? null })}>Approve</Button>
                            <Button size="xs" variant="default" onClick={() => run("Decline",{ toolUseId: pending.toolUseId ?? null })}>Decline</Button>
                        </Group>
                    </Paper>}
                </Stack>
            </div>
            {!signedIn && <Alert color="yellow" mx="md" mb="sm">Login to chat with the virtual assistant.</Alert>}
            {messages.length > 0 &&
            <Alert color="red" withCloseButton onClose={() => setMessages([])} mx="md" mb="sm">
                {messages.map((m, i) => <div key={i}>{m}</div>)}
            </Alert>}
            <Group gap="sm" p="md" style={{ flexShrink: 0, borderTop: "1px solid " + (dark ? "#4a4a4a" : "#dee2e6") }}>
                <TextInput placeholder="Type a message..." size="sm" style={{ flex: 1 }}
                           value={text} disabled={!signedIn || busy || pending != null}
                           onChange={(e) => setText(e.currentTarget.value)}
                           onKeyDown={(e) => { if (e.key === "Enter") { send(); } }} />
                <Button variant="filled" size="sm" color="blue" disabled={!signedIn || busy || pending != null} onClick={send}>Send</Button>
            </Group>
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
