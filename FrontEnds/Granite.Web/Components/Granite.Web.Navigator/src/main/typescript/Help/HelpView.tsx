//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Navigator
//////////////////////////////////////////////////////////////////////////////

import React, { useEffect, useRef, useState }  from "react";
import type { ReactElement }                   from "react";
import { ActionIcon, Button, Group, Paper,
         Radio, Slider, Stack, Tabs, Text,
         Textarea, TextInput, Title,
         useComputedColorScheme }              from "@mantine/core";
import { useDisclosure }                       from "@mantine/hooks";
import { AgGridReact }                         from "ag-grid-react";
import { AllCommunityModule, ModuleRegistry,
         themeQuartz }                         from "ag-grid-community";
import type { ColDef, Theme }                  from "ag-grid-community";

ModuleRegistry.registerModules([AllCommunityModule]);

const gridTheme: Theme = themeQuartz.withParams({ columnBorder: true, headerBackgroundColor: "#e8e8e8" });

interface IHelpSection
{
    readonly name:        string;
    readonly description: string;
}

interface IHelpArticle
{
    readonly id:         string;
    readonly title:      string;
    readonly keywords:   Array<string>;
    readonly paragraphs: Array<string>;
}

interface IChatMessage
{
    readonly role: "user" | "assistant";
    readonly text: string;
}

interface IDiagnosticCheck
{
    readonly status:      string;
    readonly name:        string;
    readonly description: string;
    readonly details:     string;
}

interface ISurveyQuestion
{
    readonly key:    "satisfied" | "needs" | "recommend";
    readonly label:  string;
    readonly labels: Record<number, string>;
}

const helpSections: Array<IHelpSection> =
[
    { name: "Search Help",                 description: "Search documentation and guides" },
    { name: "Chat With Virtual Assistant", description: "Get AI-powered assistance" },
    { name: "Provide Feedback",            description: "Report issues or suggestions" },
    { name: "Take Survey",                 description: "Share your experience with Granite" },
    { name: "View Diagnostics",            description: "System health and connectivity" },
    { name: "About Granite",            description: "Version and application information" },
];

const surveyMarks: Array<{ value: number; label: string }> =
[
    { value: 1, label: "1" },
    { value: 2, label: "2" },
    { value: 3, label: "3" },
    { value: 4, label: "4" },
    { value: 5, label: "5" },
];

const surveyQuestions: Array<ISurveyQuestion> =
[
    {
        key:   "satisfied",
        label: "Overall, how satisfied are you with Granite?",
        labels:
        {
            1: "Very dissatisfied",
            2: "Dissatisfied",
            3: "Neutral",
            4: "Satisfied",
            5: "Very satisfied",
        },
    },
    {
        key:   "needs",
        label: "How well does Granite meet your day-to-day needs?",
        labels:
        {
            1: "Not at all",
            2: "Slightly",
            3: "Moderately",
            4: "Very well",
            5: "Extremely well",
        },
    },
    {
        key:   "recommend",
        label: "How likely are you to recommend Granite to a colleague?",
        labels:
        {
            1: "Not at all likely",
            2: "Unlikely",
            3: "Neutral",
            4: "Likely",
            5: "Extremely likely",
        },
    },
];

const helpArticles: Array<IHelpArticle> =
[
    {
        id:       "getting-started",
        title:    "Getting Started with Granite",
        keywords: ["getting started", "introduction", "overview", "begin", "new user"],
        paragraphs:
        [
            "Granite is a template application. Replace this content with your own getting-started material.",
            "Navigate to {Users|users-help} from the main menu to view and manage records in the system.",
            "The {Help|help-overview} section provides search, chat, feedback, survey, diagnostics, and application information.",
        ],
    },
    {
        id:       "users-help",
        title:    "Working with Users",
        keywords: ["entity", "manage", "list", "view"],
        paragraphs:
        [
            "The Users view displays the records managed by the system in a sortable, filterable grid.",
            "Click any row to inspect it. Use the toolbar to create, edit, and save records.",
            "Each user has a unique id and user-defined fields. Replace this section with field-level documentation specific to your domain.",
        ],
    },
    {
        id:       "sessions-help",
        title:    "Working with Sessions",
        keywords: ["session", "sessions", "logged in", "sign out", "end session", "device"],
        paragraphs:
        [
            "The Sessions view lists who is signed in: one row per open session, with the user, device, login method and activity times.",
            "Turn on Include closed to also see recently closed sessions and why they closed. Expand a row to see the session values.",
            "Administrators can select sessions and choose End to sign those users out. Your own current session cannot be ended from this view.",
        ],
    },
    {
        id:       "help-overview",
        title:    "Help Center Overview",
        keywords: ["help", "support", "overview"],
        paragraphs:
        [
            "The Help center has six sections: Search Help, Chat With Virtual Assistant, Provide Feedback, Take Survey, View Diagnostics, and About Granite.",
            "Use Search Help to find articles like this one. Use Chat for interactive Q&A. Use Provide Feedback to send a defect or suggestion.",
            "View Diagnostics shows the current health of the system's integrations and services.",
        ],
    },
    {
        id:       "diagnostics-help",
        title:    "Understanding Diagnostics",
        keywords: ["diagnostic", "health", "status", "connectivity"],
        paragraphs:
        [
            "The Diagnostics view shows the current health status of each system integration. Each check reports a status: passed, recovered, or failed.",
            "Checks typically include database connectivity, event streaming (Kafka), authentication, and the health of individual microservices.",
            "If a check fails, the Details column provides specific error information. Contact your platform team for persistent failures.",
        ],
    },
];

const diagnosticChecks: Array<IDiagnosticCheck> =
[
    { status: "passed",    name: "Database Connectivity",  description: "Primary database cluster",       details: "Connected. Replication lag within SLA." },
    { status: "passed",    name: "Gateway Service",        description: "Granite.Gateway",             details: "Routing to all backend services normally." },
    { status: "passed",    name: "FrontEndController",     description: "Granite.FrontEndController",  details: "User and admin hosts responding within SLA." },
    { status: "recovered", name: "Outbox Event Relay",     description: "Granite.Outbox",              details: "Transient Kafka connection error at startup; recovered after one retry." },
    { status: "passed",    name: "Authentication Service", description: "SSO and token validation",       details: "Token validation latency within SLA." },
    { status: "failed",    name: "External Integration",   description: "Downstream partner connection",  details: "Connection timed out. Replace with your real integration checks." },
];

function DiagnosticStatusRenderer(props: { value: string }): ReactElement
{
    if (props.value === "passed")
    {
        return (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#2b8a3e"
                 strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="12" r="10" />
                <polyline points="9 12 12 15 16 9" />
            </svg>
        );
    }

    if (props.value === "recovered")
    {
        return (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#e67700"
                 strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
                <line x1="12" y1="9" x2="12" y2="13" />
                <line x1="12" y1="17" x2="12.01" y2="17" />
            </svg>
        );
    }

    return (
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fa5252"
             strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <circle cx="12" cy="12" r="10" />
            <line x1="15" y1="9" x2="9" y2="15" />
            <line x1="9" y1="9" x2="15" y2="15" />
        </svg>
    );
}

const diagnosticColumnDefs: Array<ColDef> =
[
    { field: "status",      headerName: "Status",      width: 90,  cellRenderer: DiagnosticStatusRenderer, headerClass: "ag-header-center", cellStyle: { display: "flex", justifyContent: "center", paddingTop: 8 } },
    { field: "name",        headerName: "Name",        width: 220                                         },
    { field: "description", headerName: "Description", width: 260                                         },
    { field: "details",     headerName: "Details",     flex: 1, wrapText: true, autoHeight: true          },
];

function renderParagraph(
    text:       string,
    onNavigate: (id: string) => void,
    key:        number,
): ReactElement
{
    const parts: Array<ReactElement | string> = [];
    const regex   = /\{([^|]+)\|([^}]+)\}/g;
    let lastIndex = 0;
    let match;
    let partKey   = 0;

    while ((match = regex.exec(text)) !== null)
    {
        if (match.index > lastIndex)
        {
            parts.push(text.slice(lastIndex, match.index));
        }

        const linkText  = match[1];
        const articleId = match[2];

        parts.push(
            <span key={partKey++} style={{ color: "#339af0", cursor: "pointer", textDecoration: "underline" }}
                  onClick={() => onNavigate(articleId)}>
                {linkText}
            </span>
        );

        lastIndex = regex.lastIndex;
    }

    if (lastIndex < text.length)
    {
        parts.push(text.slice(lastIndex));
    }

    return <Text key={key} size="sm" mb="xs">{parts}</Text>;
}

export function HelpView(): ReactElement
{
    const [selectedIndex, setSelectedIndex]   = useState(0);
    const [opened, { toggle }]                = useDisclosure(true);

    const [history, setHistory]               = useState<Array<string>>(["search"]);
    const [historyIndex, setHistoryIndex]     = useState(0);
    const [searchQuery, setSearchQuery]       = useState("");

    const [chatMessages, setChatMessages]     = useState<Array<IChatMessage>>([
        { role: "assistant", text: "Welcome to Granite Help. I am the Granite Virtual Assistant. How can I help you today?" },
    ]);
    const [chatInput, setChatInput]           = useState("");
    const chatContainerRef                    = useRef<HTMLDivElement>(null);

    const [feedbackType, setFeedbackType]           = useState("Defect");
    const [feedbackSubmitted, setFeedbackSubmitted] = useState(false);

    const [surveyValues, setSurveyValues]       = useState<{ satisfied: number | null; needs: number | null; recommend: number | null }>({ satisfied: null, needs: null, recommend: null });
    const [surveyComment, setSurveyComment]     = useState("");
    const [surveySubmitted, setSurveySubmitted] = useState(false);

    const colorScheme = useComputedColorScheme("light");
    const isDark      = colorScheme === "dark";

    const topbarBg  = isDark ? "#353535" : "#d6d6d6";
    const sidebarBg = isDark ? "#3a3a3a" : "#e8e8e8";
    const viewBg    = isDark ? "#3e3e3e" : "#ffffff";
    const iconColor = isDark ? "#c1c2c5" : "#495057";

    useEffect(() =>
    {
        if (chatContainerRef.current != null)
        {
            chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
        }
    }, [chatMessages]);

    function handleTabChange(v: string | null): void
    {
        setSelectedIndex(Number(v));
        setFeedbackSubmitted(false);
        setSurveySubmitted(false);
    }

    function handleNavigate(articleId: string): void
    {
        const newHistory = history.slice(0, historyIndex + 1);
        newHistory.push(articleId);
        setHistory(newHistory);
        setHistoryIndex(newHistory.length - 1);
    }

    function handleBack(): void
    {
        if (historyIndex > 0)
        {
            setHistoryIndex(historyIndex - 1);
        }
    }

    function handleForward(): void
    {
        if (historyIndex < history.length - 1)
        {
            setHistoryIndex(historyIndex + 1);
        }
    }

    function handleSendMessage(): void
    {
        const text = chatInput.trim();

        if (text.length === 0)
        {
            return;
        }

        const userMsg: IChatMessage = { role: "user",      text: text };
        const botMsg:  IChatMessage = { role: "assistant", text: "This is a placeholder response. Wire in your actual assistant backend to replace it." };

        setChatMessages(prev => [...prev, userMsg, botMsg]);
        setChatInput("");
    }

    const currentView  = history[historyIndex];
    const query        = searchQuery.trim().toLowerCase();
    const searchResults = query.length > 0
        ? helpArticles.filter(a =>
            a.title.toLowerCase().includes(query) ||
            a.keywords.some(k => k.includes(query)))
        : helpArticles;

    const currentArticle = currentView !== "search"
        ? helpArticles.find(a => a.id === currentView) ?? null
        : null;

    function renderSearchHelp(): ReactElement
    {
        if (currentArticle != null)
        {
            return (
                <Stack gap="md" p="md" style={{ flex: 1, minWidth: 0, minHeight: 0, background: viewBg, overflowY: "auto" }}>
                    <Title order={3} style={{ flexShrink: 0 }}>{currentArticle.title}</Title>
                    {currentArticle.paragraphs.map((p, i) => renderParagraph(p, handleNavigate, i))}
                </Stack>
            );
        }

        return (
            <Stack gap="md" p="md" style={{ flex: 1, minWidth: 0, minHeight: 0, background: viewBg, overflowY: "auto" }}>
                <Title order={3} style={{ flexShrink: 0 }}>
                    {query.length > 0 ? `Search Results for "${searchQuery.trim()}"` : "Help Topics"}
                </Title>
                {searchResults.length === 0 && (
                    <Text size="sm" c="dimmed">No articles found matching your search.</Text>
                )}
                {searchResults.map(article => (
                    <Paper key={article.id} p="sm" withBorder style={{ cursor: "pointer" }}
                           onClick={() => handleNavigate(article.id)}>
                        <Text size="sm" c="#339af0" fw={500} style={{ textDecoration: "underline" }}>
                            {article.title}
                        </Text>
                        <Text size="xs" c="dimmed" mt={4}>
                            {article.paragraphs[0].replace(/\{([^|]+)\|[^}]+\}/g, "$1").slice(0, 150)}...
                        </Text>
                    </Paper>
                ))}
            </Stack>
        );
    }

    function renderChat(): ReactElement
    {
        return (
            <Stack gap={0} style={{ flex: 1, minWidth: 0, minHeight: 0, background: viewBg }}>
                <div ref={chatContainerRef} style={{ flex: 1, overflowY: "auto", padding: 16 }}>
                    <Stack gap="sm">
                        {chatMessages.map((msg, i) => (
                            <Group key={i} justify={msg.role === "user" ? "flex-end" : "flex-start"}>
                                <Paper
                                    p="sm"
                                    radius="md"
                                    style={{
                                        maxWidth:   "70%",
                                        background: msg.role === "user"
                                            ? (isDark ? "#1971c2" : "#339af0")
                                            : (isDark ? "#4a4a4a" : "#f1f3f5"),
                                    }}
                                >
                                    <Text size="sm" c={msg.role === "user" ? "white" : undefined}>
                                        {msg.text}
                                    </Text>
                                </Paper>
                            </Group>
                        ))}
                    </Stack>
                </div>
                <Group gap="sm" p="md" style={{ flexShrink: 0, borderTop: "1px solid " + (isDark ? "#4a4a4a" : "#dee2e6") }}>
                    <TextInput
                        placeholder="Type a message..."
                        value={chatInput}
                        onChange={(e) => setChatInput(e.currentTarget.value)}
                        onKeyDown={(e) => { if (e.key === "Enter") { handleSendMessage(); } }}
                        style={{ flex: 1 }}
                        size="sm"
                    />
                    <Button variant="filled" size="sm" color="blue" onClick={handleSendMessage}>Send</Button>
                </Group>
            </Stack>
        );
    }

    function renderFeedback(): ReactElement
    {
        if (feedbackSubmitted)
        {
            return (
                <Stack gap="md" p="md" align="center" justify="center"
                       style={{ flex: 1, minWidth: 0, minHeight: 0, background: viewBg }}>
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#2b8a3e"
                         strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <circle cx="12" cy="12" r="10" />
                        <polyline points="9 12 12 15 16 9" />
                    </svg>
                    <Title order={3}>Thank You</Title>
                    <Text size="sm" c="dimmed">Your feedback has been submitted successfully.</Text>
                </Stack>
            );
        }

        return (
            <Stack gap="md" p="md" style={{ flex: 1, minWidth: 0, minHeight: 0, background: viewBg, overflowY: "auto" }}>
                <Title order={3} style={{ flexShrink: 0 }}>Provide Feedback</Title>

                <Radio.Group value={feedbackType} onChange={setFeedbackType}>
                    <Group gap="lg">
                        <Radio value="Defect"     label="Defect"     color="blue" />
                        <Radio value="Problem"    label="Problem"    color="blue" />
                        <Radio value="Suggestion" label="Suggestion" color="blue" />
                        <Radio value="Kudos"      label="Kudos"      color="blue" />
                    </Group>
                </Radio.Group>

                <Stack key={feedbackType} gap="sm">
                    <TextInput label="Summary" placeholder="Brief summary" size="sm" />

                    {feedbackType === "Defect" && (
                        <>
                            <Textarea label="Steps to Reproduce" placeholder="1. Navigate to...\n2. Click on...\n3. Observe..." minRows={3} size="sm" />
                            <Textarea label="Expected Behavior"  placeholder="What should have happened" minRows={2} size="sm" />
                            <Textarea label="Actual Behavior"    placeholder="What actually happened" minRows={2} size="sm" />
                        </>
                    )}

                    {feedbackType === "Problem" && (
                        <>
                            <Textarea  label="Description" placeholder="Describe the problem in detail" minRows={3} size="sm" />
                            <TextInput label="Impact"      placeholder="How does this affect your work?" size="sm" />
                            <Textarea  label="Workaround"  placeholder="Any workaround you are currently using" minRows={2} size="sm" />
                        </>
                    )}

                    {feedbackType === "Suggestion" && (
                        <>
                            <Textarea label="Description"     placeholder="Describe your suggestion" minRows={3} size="sm" />
                            <Textarea label="Expected Benefit" placeholder="How would this improvement help?" minRows={2} size="sm" />
                        </>
                    )}

                    {feedbackType === "Kudos" && (
                        <>
                            <Textarea label="What do you like?"    placeholder="Tell us what's working well for you" minRows={3} size="sm" />
                            <Textarea label="Additional Comments" placeholder="Anything else you'd like to share" minRows={2} size="sm" />
                        </>
                    )}

                    <Group>
                        <Button variant="filled" size="sm" color="blue" onClick={() => setFeedbackSubmitted(true)}>
                            Submit Feedback
                        </Button>
                    </Group>
                </Stack>
            </Stack>
        );
    }

    function renderSurvey(): ReactElement
    {
        if (surveySubmitted)
        {
            return (
                <Stack gap="md" p="md" align="center" justify="center"
                       style={{ flex: 1, minWidth: 0, minHeight: 0, background: viewBg }}>
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#2b8a3e"
                         strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <circle cx="12" cy="12" r="10" />
                        <polyline points="9 12 12 15 16 9" />
                    </svg>
                    <Title order={3}>Thank You</Title>
                    <Text size="sm" c="dimmed">Your survey has been submitted successfully.</Text>
                    <Button variant="light" size="sm" color="blue" onClick={() =>
                    {
                        setSurveySubmitted(false);
                        setSurveyValues({ satisfied: null, needs: null, recommend: null });
                        setSurveyComment("");
                        setSelectedIndex(0);
                    }}>
                        Dismiss
                    </Button>
                </Stack>
            );
        }

        const canSubmit: boolean = surveyValues.satisfied !== null
                                && surveyValues.needs     !== null
                                && surveyValues.recommend !== null;

        return (
            <Stack gap="lg" p="md" style={{ flex: 1, minWidth: 0, minHeight: 0, background: viewBg, overflowY: "auto" }}>
                <Title order={3} style={{ flexShrink: 0 }}>Take Survey</Title>

                <div style={{ display: "grid", gridTemplateColumns: "auto 240px", gap: "24px 16px", alignItems: "center", justifyContent: "start" }}>
                    {surveyQuestions.map(q =>
                    {
                        const value: number | null = surveyValues[q.key];

                        return (
                            <React.Fragment key={q.key}>
                                <Text size="md" fw={600} style={{ textAlign: "right", paddingRight: 24 }}>
                                    {q.label}
                                </Text>
                                <div style={{ paddingBottom: 24 }}>
                                    <Slider
                                        min={1}
                                        max={5}
                                        step={1}
                                        marks={surveyMarks}
                                        value={value ?? 1}
                                        onChange={(v) => setSurveyValues(prev => ({ ...prev, [q.key]: v }))}
                                        label={(v) => value !== null ? q.labels[v] : null}
                                        labelAlwaysOn={value !== null}
                                        color="teal.8"
                                        styles={{
                                            thumb: { opacity: value === null ? 0 : 1, transition: "opacity 150ms ease" },
                                            track: { opacity: value === null ? 0.4 : 1, transition: "opacity 150ms ease" },
                                            label: { top: "unset", bottom: -30, fontSize: 11, whiteSpace: "nowrap" },
                                        }}
                                    />
                                </div>
                            </React.Fragment>
                        );
                    })}
                </div>

                <Textarea
                    label="What's one thing we could improve about Granite?"
                    placeholder="Share your thoughts..."
                    value={surveyComment}
                    onChange={(e) => setSurveyComment(e.currentTarget.value)}
                    minRows={3}
                    size="sm"
                />

                <Group>
                    <Button variant="filled" size="sm" color="blue" disabled={!canSubmit}
                            onClick={() => setSurveySubmitted(true)}>
                        Submit Survey
                    </Button>
                </Group>
            </Stack>
        );
    }

    function renderDiagnostics(): ReactElement
    {
        return (
            <Stack gap="md" p="md" style={{ flex: 1, minWidth: 0, minHeight: 0, background: viewBg }}>
                <Title order={3} style={{ flexShrink: 0 }}>Diagnostic Checks</Title>
                <div className="ag-theme-quartz" style={{ flex: 1, minHeight: 0, width: "100%" }}>
                    <AgGridReact
                        theme={gridTheme}
                        rowData={diagnosticChecks}
                        columnDefs={diagnosticColumnDefs}
                        defaultColDef={{ sortable: true, resizable: true }}
                    />
                </div>
            </Stack>
        );
    }

    function renderAbout(): ReactElement
    {
        return (
            <Stack gap="lg" p="md" style={{ flex: 1, minWidth: 0, minHeight: 0, background: viewBg, overflowY: "auto" }}>
                <div>
                    <Title order={3}>Granite</Title>
                    <Text size="sm" mt="sm">
                        Granite is a template application generated from the Granite system template.
                        Replace this description with a short summary of what your system does, who uses it,
                        and the integrations and architectural patterns it depends on.
                    </Text>
                </div>

                <div>
                    <Title order={3}>Version</Title>
                    <Text size="sm" mt="sm">1.0.0-SNAPSHOT</Text>
                </div>

                <div>
                    <Title order={3}>Colophon</Title>
                    <Text size="sm" mt="sm" fs="italic">On the name Granite</Text>
                    <Text size="sm" mt="sm">
                        Replace this with the story behind your system's name. Describe why it was chosen,
                        what it represents, and any history or meaning that connects the name to the work
                        the system does.
                    </Text>
                </div>
            </Stack>
        );
    }

    return (
        <Stack gap={0} className="view-container">
            <Group
                gap="sm"
                px="sm"
                py={6}
                className="view-toolbar"
            >
                <ActionIcon variant="subtle" size="lg" onClick={toggle}>
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
                         fill="none" stroke={iconColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <line x1="3" y1="6" x2="21" y2="6" />
                        <line x1="3" y1="12" x2="21" y2="12" />
                        <line x1="3" y1="18" x2="21" y2="18" />
                    </svg>
                </ActionIcon>
                {selectedIndex === 0 && (
                    <>
                        <div style={{ width: opened ? 220 : 0, transition: "width 200ms ease", flexShrink: 0 }} />
                        <ActionIcon variant="default" size="lg" style={{ background: topbarBg }}
                                    disabled={historyIndex <= 0} onClick={handleBack}>
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
                                 fill="none" stroke={iconColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                <line x1="19" y1="12" x2="5" y2="12" />
                                <polyline points="12 19 5 12 12 5" />
                            </svg>
                        </ActionIcon>
                        <ActionIcon variant="default" size="lg" style={{ background: topbarBg }}
                                    disabled={historyIndex >= history.length - 1} onClick={handleForward}>
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
                                 fill="none" stroke={iconColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                <line x1="5" y1="12" x2="19" y2="12" />
                                <polyline points="12 5 19 12 12 19" />
                            </svg>
                        </ActionIcon>
                        <TextInput
                            leftSection={
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke={iconColor}
                                     strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                    <circle cx="11" cy="11" r="8" />
                                    <line x1="21" y1="21" x2="16.65" y2="16.65" />
                                </svg>
                            }
                            placeholder="Search help..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.currentTarget.value)}
                            size="sm"
                            style={{ flex: 1, maxWidth: 400 }}
                        />
                    </>
                )}
            </Group>

            <Group align="stretch" gap={0} wrap="nowrap" style={{ flex: 1, minHeight: 0 }}>
                <nav style={{
                    width:      opened ? 280 : 0,
                    overflow:   "hidden",
                    transition: "width 200ms ease",
                    flexShrink: 0,
                    background: sidebarBg,
                }}>
                    <div style={{ width: 280 }}>
                        <Tabs
                            orientation="vertical"
                            variant="unstyled"
                            value={String(selectedIndex)}
                            onChange={handleTabChange}
                            styles={{ list: { width: "100%" } }}
                        >
                            <Tabs.List>
                                {helpSections.map((s, i) =>
                                {
                                    const isActive = i === selectedIndex;

                                    return (
                                        <Tabs.Tab
                                            key={s.name}
                                            value={String(i)}
                                            py="md"
                                            px="lg"
                                            style={{
                                                width:       "100%",
                                                borderLeft:  "4px solid " + (isActive ? "#5d8abf" : "transparent"),
                                                background:  isActive ? "rgba(93, 138, 191, 0.1)" : "transparent",
                                                textAlign:   "left",
                                                transition:  "background 0.15s, border-color 0.15s",
                                            }}
                                        >
                                            <Text size="sm" fw={isActive ? 600 : 400}
                                                  c={isActive ? "#5d8abf" : undefined}>{s.name}</Text>
                                        </Tabs.Tab>
                                    );
                                })}
                            </Tabs.List>
                        </Tabs>
                    </div>
                </nav>

                {selectedIndex === 0 && renderSearchHelp()}
                {selectedIndex === 1 && renderChat()}
                {selectedIndex === 2 && renderFeedback()}
                {selectedIndex === 3 && renderSurvey()}
                {selectedIndex === 4 && renderDiagnostics()}
                {selectedIndex === 5 && renderAbout()}
            </Group>
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
