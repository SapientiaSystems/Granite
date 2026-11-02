//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Navigator
//////////////////////////////////////////////////////////////////////////////

import { useState }                            from "react";
import type { ReactElement }                   from "react";
import { ActionIcon, Group, Stack, Tabs, Text,
         Title, useComputedColorScheme }       from "@mantine/core";
import { useDisclosure }                       from "@mantine/hooks";
import { FeedbackView }                        from "granite.web.feedback/Feedback";
import { SurveyView }                          from "granite.web.survey/Survey";
import { ContentSearchView }                   from "granite.web.content/Search";
import { AgentChatView }                       from "granite.web.agent/Chat";
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

interface IDiagnosticCheck
{
    readonly status:      string;
    readonly name:        string;
    readonly description: string;
    readonly details:     string;
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



export function HelpView(): ReactElement
{
    const [selectedIndex, setSelectedIndex]   = useState(0);
    const [opened, { toggle }]                = useDisclosure(true);




    const colorScheme = useComputedColorScheme("light");
    const isDark      = colorScheme === "dark";

    const sidebarBg = isDark ? "#3a3a3a" : "#e8e8e8";
    const viewBg    = isDark ? "#3e3e3e" : "#ffffff";
    const iconColor = isDark ? "#c1c2c5" : "#495057";


    function handleTabChange(v: string | null): void
    {
        setSelectedIndex(Number(v));
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

                {selectedIndex === 0 && <ContentSearchView style={{ background: viewBg }} />}
                {selectedIndex === 1 && <AgentChatView style={{ background: viewBg }} />}
                {selectedIndex === 2 && <FeedbackView style={{ background: viewBg }} />}
                {selectedIndex === 3 && <SurveyView style={{ background: viewBg }} onDismiss={() => setSelectedIndex(0)} />}
                {selectedIndex === 4 && renderDiagnostics()}
                {selectedIndex === 5 && renderAbout()}
            </Group>
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
