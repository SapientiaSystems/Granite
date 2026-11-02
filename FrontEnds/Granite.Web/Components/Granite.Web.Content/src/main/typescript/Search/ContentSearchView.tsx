//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Content
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useRef, useState }                  from "react";
import type { CSSProperties, ReactElement }             from "react";
import { ActionIcon, Alert, Group, Loader, Paper, Stack,
         Text, TextInput, Title }                       from "@mantine/core";
import { GraniteFrontEndControllerServiceClient }       from "granite.frontendcontroller.client/RequestReply";
import type { IGraniteFrontEndControllerServiceClient } from "granite.frontendcontroller.client/RequestReply";
import { GetOutputRequestBuilder, HandleInputRequestBuilder } from "strata.frontendcontroller.service/RequestReply";
import type { ContentInput, ContentOutput, GetContentOutputReply,
              HandleContentInputReply }                 from "granite.frontendcontroller.service/RequestReply";
import type { ContentDocumentData, ContentHitData }     from "granite.content.service/RequestReply";
import { useAuthentication }                            from "granite.web.authentication/Authentication";
import { MarkdownView }                                 from "../Markdown/MarkdownView";
import { INITIAL_HISTORY, back, canGoBack, canGoForward, currentLocation,
         forward, navigate }                            from "./ContentHistory";
import type { IContentHistory }                         from "./ContentHistory";

const SOURCE:      string = "Content.All";
const DESTINATION: string = "ContentView";
const SERVICE:     string = "frontendcontroller-user-service";
const DEBOUNCE:    number = 300;

export interface IContentSearchViewProps
{
    readonly service?:    string;
    readonly initialKey?: string;
    readonly className?:  string;
    readonly style?:      CSSProperties;
}

const iconProps = { width: 20, height: 20, viewBox: "0 0 24 24", fill: "none", stroke: "currentColor",
                    strokeWidth: 2, strokeLinecap: "round" as const, strokeLinejoin: "round" as const };

export function ContentSearchView(props: IContentSearchViewProps): ReactElement
{
    const service: string                 = props.service ?? SERVICE;
    const { state, createFrontEndClient } = useAuthentication();
    const signedIn: boolean               = state.status === "authenticated" || state.status === "reauthenticating";
    const [history, setHistory]           = useState<IContentHistory>(
        props.initialKey == null ? INITIAL_HISTORY : navigate(INITIAL_HISTORY,{ view: "article", key: props.initialKey }));
    const [query, setQuery]               = useState<string>("");
    const [index, setIndex]               = useState<Array<ContentHitData>>([]);
    const [hits, setHits]                 = useState<Array<ContentHitData> | null>(null);
    const [article, setArticle]           = useState<ContentDocumentData | null>(null);
    const [messages, setMessages]         = useState<Array<string>>([]);
    const [busy, setBusy]                 = useState<boolean>(false);
    const searchSeq                       = useRef<number>(0);
    const location                        = currentLocation(history);

    const createClient = (): IGraniteFrontEndControllerServiceClient =>
        createFrontEndClient((url,fetchFunction) => new GraniteFrontEndControllerServiceClient(url,fetchFunction),service);

    const input = (command: string, fields: Partial<ContentInput>): ContentInput =>
        ({ userId: null, source: SOURCE, command, destination: DESTINATION, query: null, articleKey: null, kinds: [], ...fields } as ContentInput);

    const failures = (output: ContentOutput | undefined, fallback: string): Array<string> =>
        output == null ? [fallback] : (output.failureMessages ?? []);

    useEffect(() =>
    {
        if (!signedIn)
        {
            return;
        }

        const client: IGraniteFrontEndControllerServiceClient = createClient();

        client
            .getContentOutput(GetOutputRequestBuilder.newBuilder().setSource(SOURCE).setDestination(DESTINATION).build())
            .thenApply((reply: GetContentOutputReply) =>
            {
                if (reply.success === false || reply.output == null)
                    setMessages([reply.failureMessage ?? "Unable to load help topics"]);
                else
                {
                    setIndex(reply.output.index ?? []);
                    setMessages(failures(reply.output,"Unable to load help topics"));
                }

                return reply;
            })
            .exceptionally((error: Error) =>
            {
                setMessages([error?.message ?? "Unable to load help topics"]);
                return {} as GetContentOutputReply;
            })
            .subscribe(undefined,(error: Error) => setMessages([error?.message ?? "Unable to load help topics"]));

        return () => { client.close(); };
    }, [signedIn]);

    useEffect(() =>
    {
        const text: string = query.trim();

        if (!signedIn || text.length < 2)
        {
            setHits(null);
            return;
        }

        const seq: number = ++searchSeq.current;
        const timer       = setTimeout(() =>
        {
            const client: IGraniteFrontEndControllerServiceClient = createClient();

            setBusy(true);
            client
                .handleContentInput(HandleInputRequestBuilder.newBuilder<ContentInput>().setInput(input("Search",{ query: text })).build())
                .thenApply((reply: HandleContentInputReply) =>
                {
                    client.close();

                    if (seq === searchSeq.current)
                    {
                        setBusy(false);
                        setHits(reply.output?.hits ?? []);
                        setMessages(reply.success === false ? [reply.failureMessage ?? "Unable to search help"] : failures(reply.output,"Unable to search help"));
                    }

                    return reply;
                })
                .exceptionally((error: Error) =>
                {
                    client.close();
                    setBusy(false);
                    setMessages([error?.message ?? "Unable to search help"]);
                    return {} as HandleContentInputReply;
                })
                .subscribe(undefined,(error: Error) =>
                {
                    client.close();
                    setBusy(false);
                    setMessages([error?.message ?? "Unable to search help"]);
                });
        },DEBOUNCE);

        return () => { clearTimeout(timer); };
    }, [query, signedIn]);

    useEffect(() =>
    {
        if (!signedIn || location.view !== "article")
        {
            setArticle(null);
            return;
        }

        const client: IGraniteFrontEndControllerServiceClient = createClient();

        setBusy(true);
        client
            .handleContentInput(HandleInputRequestBuilder.newBuilder<ContentInput>().setInput(input("Open",{ articleKey: location.key })).build())
            .thenApply((reply: HandleContentInputReply) =>
            {
                setBusy(false);
                setArticle(reply.output?.article ?? null);
                setMessages(reply.success === false ? [reply.failureMessage ?? "Unable to open article"] : failures(reply.output,"Unable to open article"));
                return reply;
            })
            .exceptionally((error: Error) =>
            {
                setBusy(false);
                setMessages([error?.message ?? "Unable to open article"]);
                return {} as HandleContentInputReply;
            })
            .subscribe(undefined,(error: Error) =>
            {
                setBusy(false);
                setMessages([error?.message ?? "Unable to open article"]);
            });

        return () => { client.close(); };
    }, [location.view, location.view === "article" ? location.key : "", signedIn]);

    function open(key: string | undefined): void
    {
        if (key != null && key !== "")
        {
            setHistory(previous => navigate(previous,{ view: "article", key }));
        }
    }

    function showTopics(next: string): void
    {
        setQuery(next);
        setHistory(previous => navigate(previous,{ view: "topics" }));
    }

    function renderList(): ReactElement
    {
        const items: Array<ContentHitData> = hits ?? index;
        const title: string                = hits == null ? "Help Topics" : `Search Results for "${query.trim()}"`;

        return (
            <Stack gap="sm">
                <Title order={3}>{title}</Title>
                {items.length === 0 && !busy && <Text size="sm" c="dimmed">{hits == null ? "No help topics available." : "No articles found matching your search."}</Text>}
                {items.map(item => (
                    <Paper key={item.key} p="sm" withBorder style={{ cursor: "pointer" }} onClick={() => open(item.key)}>
                        <Text size="sm" c="blue" fw={500} style={{ textDecoration: "underline" }}>{item.title}</Text>
                        {item.snippet != null && item.snippet !== "" && <Text size="xs" c="dimmed" mt={4}>{item.snippet}</Text>}
                    </Paper>
                ))}
            </Stack>
        );
    }

    function renderArticle(): ReactElement
    {
        if (article == null)
        {
            return busy ? <Loader size="sm" /> : <Text size="sm" c="dimmed">Article not available.</Text>;
        }

        return <MarkdownView source={article.body} onNavigate={open} />;
    }

    return (
        <Stack gap="md" p="md" className={props.className}
               style={{ flex: 1, minWidth: 0, minHeight: 0, overflowY: "auto", ...props.style }}>
            <Group gap="sm" wrap="nowrap" style={{ flexShrink: 0 }}>
                <ActionIcon variant="default" size="lg" aria-label="Back" disabled={!canGoBack(history)}
                            onClick={() => setHistory(back)}>
                    <svg {...iconProps}><line x1="19" y1="12" x2="5" y2="12" /><polyline points="12 19 5 12 12 5" /></svg>
                </ActionIcon>
                <ActionIcon variant="default" size="lg" aria-label="Forward" disabled={!canGoForward(history)}
                            onClick={() => setHistory(forward)}>
                    <svg {...iconProps}><line x1="5" y1="12" x2="19" y2="12" /><polyline points="12 5 19 12 12 19" /></svg>
                </ActionIcon>
                <TextInput placeholder="Search help..." size="sm" style={{ flex: 1, maxWidth: 400 }}
                           value={query} disabled={!signedIn}
                           onChange={(e) => showTopics(e.currentTarget.value)}
                           rightSection={busy ? <Loader size="xs" /> : undefined} />
            </Group>
            {!signedIn && <Alert color="yellow">Login to search help.</Alert>}
            {messages.length > 0 &&
            <Alert color="red" withCloseButton onClose={() => setMessages([])}>
                {messages.map((m, i) => <div key={i}>{m}</div>)}
            </Alert>}
            {location.view === "article" ? renderArticle() : renderList()}
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
