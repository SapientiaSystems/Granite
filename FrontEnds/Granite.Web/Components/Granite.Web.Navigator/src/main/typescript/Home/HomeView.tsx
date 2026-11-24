//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Navigator
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useState }                         from "react";
import type { ReactElement, ReactNode }                from "react";
import { ActionIcon, Alert, Badge, Group, Loader, Paper, SimpleGrid,
         Stack, Text, Title, Tooltip, useComputedColorScheme } from "@mantine/core";
import { GraniteFrontEndControllerServiceClient }      from "granite.frontendcontroller.client/RequestReply";
import type { IGraniteFrontEndControllerServiceClient } from "granite.frontendcontroller.client/RequestReply";
import { GetOutputRequestBuilder }                     from "strata.frontendcontroller.service/RequestReply";
import type { GetHomeOutputReply, HomeCountData,
              HomeOutput }                             from "granite.frontendcontroller.service/RequestReply";
import { useAuthentication }                           from "granite.web.authentication/Authentication";
import { averageOf, breakdownTotal, countOf, dayTotals, formatAsOf, formatAverage, isForbidden,
         percentOf, roleLabels, segments, sparkHeights, stackedDays } from "./HomeDashboard";
import type { ISegment }                               from "./HomeDashboard";

const SOURCE:      string = "Home.Current";
const DESTINATION: string = "HomeView";
const SERVICE:     string = "frontendcontroller-user-service";

const DARK:  string = "var(--mantine-color-blue-9)";
const MID:   string = "var(--mantine-color-blue-5)";
const LIGHT: string = "var(--mantine-color-blue-2)";
const WARN:  string = "var(--mantine-color-orange-7)";
const TRACK: string = "var(--mantine-color-default-border)";

const LOGIN_COLORS:   Record<string,string> = { OKTA: DARK, PASSWORD: MID, ACCESS_TOKEN: LIGHT };
const STATUS_COLORS:  Record<string,string> = { NEW: WARN, SENT: MID, RECEIVED: DARK };
const BAND_COLORS:    Record<string,string> = { HIGH: DARK, NEUTRAL: MID, LOW: WARN };
const OUTCOME_COLORS: Record<string,string> = { OPEN: DARK, ADMIN: WARN, REVOKED_SECURITY: WARN };
const ROLE_COLORS:    Array<string>         = [DARK, MID, LIGHT];
const LOGIN_KEYS:     Array<string>         = ["OKTA", "PASSWORD", "ACCESS_TOKEN"];
const STATUS_KEYS:    Array<string>         = ["NEW", "SENT", "RECEIVED"];

const svgProps = { xmlns: "http://www.w3.org/2000/svg", width: 20, height: 20, viewBox: "0 0 24 24",
                   fill: "none", stroke: "currentColor", strokeWidth: 2,
                   strokeLinecap: "round" as const, strokeLinejoin: "round" as const };

export interface IHomeViewProps
{
    readonly service?: string;
}

interface IFact
{
    value: string;
    label: string;
}

interface IPanelProps
{
    title:         string;
    value:         string;
    caption:       string;
    available:     boolean;
    spark?:        Array<HomeCountData>;
    sparkCaption?: string;
    split:         Array<ISegment>;
    colors:        (key: string, index: number) => string;
    facts:         Array<IFact>;
}

function formatCurrentDate(): string
{
    return new Date().toLocaleDateString("en-US",{ weekday: "long", year: "numeric", month: "long", day: "numeric" });
}

function Legend(props: { items: Array<{ key: string; label: string; color: string; count?: number }> }): ReactElement
{
    return (
        <Group gap="md" wrap="wrap">
            {props.items.map(item => (
                <Group key={item.key} gap={6} wrap="nowrap">
                    <span style={{ width: 10, height: 10, borderRadius: 2, background: item.color, display: "inline-block" }} />
                    <Text size="xs" c="dimmed">{item.label}</Text>
                    {item.count !== undefined && <Text size="xs" fw={700}>{item.count}</Text>}
                </Group>
            ))}
        </Group>
    );
}

function Card(props: { title: string; aside?: ReactNode; children: ReactNode }): ReactElement
{
    return (
        <Paper withBorder radius="md" p="md" style={{ display: "flex", flexDirection: "column", gap: 12, minWidth: 0 }}>
            <Group justify="space-between" wrap="wrap" gap="xs">
                <Title order={5}>{props.title}</Title>
                {props.aside}
            </Group>
            {props.children}
        </Paper>
    );
}

function Unavailable(): ReactElement
{
    return <Text size="sm" c="dimmed">Unavailable</Text>;
}

function SubjectPanel(props: IPanelProps): ReactElement
{
    const heights: Array<number> = sparkHeights(props.spark,40);

    return (
        <Paper withBorder radius="md" p="lg" style={{ display: "flex", flexDirection: "column", gap: 14, minWidth: 0 }}>
            <Title order={5}>{props.title}</Title>
            {!props.available ? <Unavailable /> : (
                <>
                    <Group justify="space-between" align="flex-end" wrap="wrap" gap="md">
                        <Stack gap={2}>
                            <Text fz={36} fw={700} lh={1}>{props.value}</Text>
                            <Text size="sm" c="dimmed">{props.caption}</Text>
                        </Stack>
                        {props.spark !== undefined && (
                            <Stack gap={4} align="flex-end">
                                <div role="img" aria-label={props.sparkCaption}
                                     style={{ display: "flex", alignItems: "flex-end", gap: 3, height: 40 }}>
                                    {heights.map((h,i) => <div key={i} style={{ width: 10, height: h, background: MID, borderRadius: 2 }} />)}
                                </div>
                                <Text size="xs" c="dimmed">{props.sparkCaption}</Text>
                            </Stack>
                        )}
                    </Group>
                    <Stack gap={8}>
                        <div role="img" aria-label={props.split.map(s => s.label + " " + s.count).join(", ")}
                             style={{ display: "flex", height: 12, gap: 2, borderRadius: 6, overflow: "hidden", background: TRACK }}>
                            {props.split.filter(s => s.pct > 0).map(s =>
                                <div key={s.key} style={{ width: s.pct + "%", background: props.colors(s.key,props.split.indexOf(s)) }} />)}
                        </div>
                        <Legend items={props.split.map((s,i) => ({ key: s.key, label: s.label, color: props.colors(s.key,i), count: s.count }))} />
                    </Stack>
                    <SimpleGrid cols={2} spacing="xs" pt="sm" style={{ borderTop: "1px solid " + TRACK }}>
                        {props.facts.map(f => (
                            <Stack key={f.label} gap={0}>
                                <Text fw={700}>{f.value}</Text>
                                <Text size="xs" c="dimmed">{f.label}</Text>
                            </Stack>
                        ))}
                    </SimpleGrid>
                </>
            )}
        </Paper>
    );
}

function BarRow(props: { label: string; count: string; pct: number; color: string }): ReactElement
{
    return (
        <div style={{ display: "grid", gridTemplateColumns: "minmax(90px, 40%) 1fr 40px", alignItems: "center", gap: 10 }}>
            <Text size="sm" c="dimmed" truncate="end">{props.label}</Text>
            <div style={{ height: 10, background: TRACK, borderRadius: 5 }}>
                <div style={{ width: props.pct + "%", height: 10, background: props.color, borderRadius: 5 }} />
            </div>
            <Text size="sm" fw={600} ta="right">{props.count}</Text>
        </div>
    );
}

function Panels(props: { output: HomeOutput }): ReactElement
{
    const { users, sessions, feedback, survey } = props.output;
    const satisfied: number | null              = averageOf(survey?.averages,"satisfied");

    return (
        <SimpleGrid cols={{ base: 1, sm: 2, lg: 4 }} spacing="md">
            <SubjectPanel title="Users" available={users != null}
                          value={String(users?.total ?? 0)} caption="users"
                          split={segments(users?.byRole)} colors={(_,i) => ROLE_COLORS[i % ROLE_COLORS.length]}
                          facts={[{ value: String(users?.oktaLinked ?? 0), label: "linked to Okta" },
                                  { value: String((users?.total ?? 0) - (users?.oktaLinked ?? 0)), label: "not linked to Okta" }]} />
            <SubjectPanel title="Sessions" available={sessions != null}
                          value={String(sessions?.open ?? 0)} caption="open sessions now"
                          spark={dayTotals(sessions?.dailyLogins).slice(-7)} sparkCaption="logins, last 7 days"
                          split={segments(sessions?.openByLoginMethod)} colors={k => LOGIN_COLORS[k] ?? LIGHT}
                          facts={[{ value: String(sessions?.activeLastHour ?? 0), label: "active in the last hour" },
                                  { value: String(sessions?.expiringWithinHour ?? 0), label: "expire within 1 hour" }]} />
            <SubjectPanel title="Feedback" available={feedback != null}
                          value={String(countOf(feedback?.byStatus,"NEW"))} caption="new, waiting for triage"
                          spark={feedback?.daily} sparkCaption="submitted, last 7 days"
                          split={segments(feedback?.byStatus)} colors={k => STATUS_COLORS[k] ?? MID}
                          facts={[{ value: String(feedback?.total ?? 0), label: "feedback, all time" },
                                  { value: String(breakdownTotal(feedback?.byType?.find(t => t.key === "DEFECT") ?? { key: "", label: "", counts: {} })), label: "defects, all time" }]} />
            <SubjectPanel title="Surveys" available={survey != null}
                          value={formatAverage(satisfied)} caption="average satisfaction out of 5"
                          spark={survey?.daily} sparkCaption="responses, last 7 days"
                          split={segments(survey?.satisfactionBands)} colors={k => BAND_COLORS[k] ?? MID}
                          facts={[{ value: formatAverage(averageOf(survey?.averages,"needs")), label: "meets day-to-day needs" },
                                  { value: formatAverage(averageOf(survey?.averages,"recommend")), label: "would recommend" }]} />
        </SimpleGrid>
    );
}

function Details(props: { output: HomeOutput }): ReactElement
{
    const { sessions, feedback, survey } = props.output;
    const days                           = stackedDays(sessions?.dailyLogins,LOGIN_KEYS,180);
    const outcomeMax: number             = Math.max(0,...(sessions?.recentOutcomes ?? []).map(o => o.count));
    const typeMax:    number             = Math.max(0,...(feedback?.byType ?? []).map(breakdownTotal));
    const loginLegend                    = (sessions?.openByLoginMethod ?? []).map(m => ({ key: m.key, label: m.label, color: LOGIN_COLORS[m.key] ?? LIGHT }));
    const statusLegend                   = (feedback?.byStatus ?? []).map(s => ({ key: s.key, label: s.label, color: STATUS_COLORS[s.key] ?? MID }));

    return (
        <>
            <div style={{ display: "flex", flexWrap: "wrap", gap: 16 }}>
                <div style={{ flex: "2 1 560px", minWidth: 0 }}>
                    <Card title="Logins per day · last 14 days" aside={<Legend items={loginLegend} />}>
                        {sessions == null ? <Unavailable /> : (
                            <>
                                <div role="img" aria-label="Column chart of daily logins by login method"
                                     style={{ display: "flex", alignItems: "flex-end", gap: 6, height: 180, borderBottom: "1px solid " + TRACK }}>
                                    {days.map(d => (
                                        <Tooltip key={d.key} label={d.key + ": " + d.total + " logins"}>
                                            <div style={{ flex: 1, height: "100%", display: "flex", flexDirection: "column-reverse", gap: 1 }}>
                                                {d.segments.filter(s => s.height > 0).map(s =>
                                                    <div key={s.key} style={{ height: s.height, background: LOGIN_COLORS[s.key] ?? LIGHT, borderRadius: 2 }} />)}
                                            </div>
                                        </Tooltip>
                                    ))}
                                </div>
                                <div style={{ display: "flex", gap: 6 }}>
                                    {days.map(d => <Text key={d.key} size="xs" c="dimmed" ta="center" style={{ flex: 1 }}>{d.label}</Text>)}
                                </div>
                            </>
                        )}
                    </Card>
                </div>
                <div style={{ flex: "1 1 320px", minWidth: 0 }}>
                    <Card title="Sessions started in the last 7 days · how they ended">
                        {sessions == null ? <Unavailable /> : (sessions.recentOutcomes ?? []).map(o =>
                            <BarRow key={o.key} label={o.label} count={String(o.count)} pct={percentOf(o.count,outcomeMax)}
                                    color={OUTCOME_COLORS[o.key] ?? MID} />)}
                    </Card>
                </div>
            </div>
            <SimpleGrid cols={{ base: 1, md: 2 }} spacing="md">
                <Card title="Feedback by type and status" aside={<Legend items={statusLegend} />}>
                    {feedback == null ? <Unavailable /> : (feedback.byType ?? []).map(t => (
                        <div key={t.key} style={{ display: "grid", gridTemplateColumns: "100px 1fr 40px", alignItems: "center", gap: 10 }}>
                            <Text size="sm" c="dimmed">{t.label}</Text>
                            <div role="img" aria-label={t.label + ": " + STATUS_KEYS.map(k => k.toLowerCase() + " " + (t.counts?.[k] ?? 0)).join(", ")}
                                 style={{ display: "flex", height: 16, gap: 2, background: TRACK, borderRadius: 3, overflow: "hidden" }}>
                                {STATUS_KEYS.filter(k => (t.counts?.[k] ?? 0) > 0).map(k =>
                                    <div key={k} style={{ width: percentOf(t.counts?.[k] ?? 0,typeMax) + "%", background: STATUS_COLORS[k] }} />)}
                            </div>
                            <Text size="sm" fw={600} ta="right">{breakdownTotal(t)}</Text>
                        </div>
                    ))}
                </Card>
                <Card title="Survey averages · 1 to 5"
                      aside={survey != null && <Text size="xs" c="dimmed">{survey.responses} responses</Text>}>
                    {survey == null ? <Unavailable /> : (survey.averages ?? []).map(a => (
                        <Stack key={a.key} gap={6}>
                            <Group justify="space-between" wrap="nowrap" gap="sm">
                                <Text size="sm" c="dimmed">{a.label}</Text>
                                <Text size="sm" fw={700}>{formatAverage(a.average)}</Text>
                            </Group>
                            <div style={{ height: 10, background: TRACK, borderRadius: 5 }}>
                                <div style={{ width: percentOf(a.average ?? 0,5) + "%", height: 10, background: MID, borderRadius: 5 }} />
                            </div>
                        </Stack>
                    ))}
                </Card>
            </SimpleGrid>
        </>
    );
}

export function HomeView(props: IHomeViewProps): ReactElement
{
    const service: string                 = props.service ?? SERVICE;
    const { state, createFrontEndClient } = useAuthentication();
    const dark: boolean                   = useComputedColorScheme("light") === "dark";
    const [output, setOutput]             = useState<HomeOutput | null>(null);
    const [loading, setLoading]           = useState<boolean>(true);
    const [forbidden, setForbidden]       = useState<boolean>(false);
    const [messages, setMessages]         = useState<Array<string>>([]);
    const [reload, setReload]             = useState<number>(0);
    const signedIn: boolean               = state.status === "authenticated" || state.status === "reauthenticating";

    useEffect(() =>
    {
        if (!signedIn)
        {
            setOutput(null);
            setLoading(state.status === "restoring");
            return;
        }

        const client: IGraniteFrontEndControllerServiceClient =
            createFrontEndClient((url,fetchFunction) => new GraniteFrontEndControllerServiceClient(url,fetchFunction),service);

        const fail = (error: unknown): void =>
        {
            if (isForbidden(error))
                setForbidden(true);
            else
                setMessages([(error as Error)?.message ?? "Failed to load the dashboard"]);

            setLoading(false);
        };

        setLoading(true);
        client
            .getHomeOutput(GetOutputRequestBuilder.newBuilder().setSource(SOURCE).setDestination(DESTINATION).build())
            .thenApply((reply: GetHomeOutputReply) =>
            {
                if (reply.success === false)
                {
                    setMessages([reply.failureMessage ?? "Failed to load the dashboard"]);
                }
                else
                {
                    setOutput(reply.output ?? null);
                    setMessages(reply.output?.failureMessages ?? []);
                    setForbidden(false);
                }

                setLoading(false);
                return reply;
            })
            .exceptionally((error: Error) =>
            {
                fail(error);
                return {} as GetHomeOutputReply;
            })
            .subscribe(undefined,fail);

        return () => { client.close(); };
    }, [reload,signedIn,state.status]);

    const userName: string        = output?.userName ?? state.userName ?? "";
    const roles:    Array<string> = roleLabels(output?.roles,state.authorities ?? []);
    const asOf:     string        = formatAsOf(output?.generated);

    return (
        <Stack gap={0} className="view-container" style={{ position: "relative", minHeight: "calc(100vh - var(--app-header-height, 0px))" }}>
            <Group gap="md" className="view-toolbar" px="md" py={8} justify="space-between" wrap="wrap"
                   style={{ backgroundColor: dark ? "var(--mantine-color-dark-6)" : "var(--mantine-color-gray-3)", alignItems: "center" }}>
                <Stack gap={4}>
                    <Text size="md" fw={600}>{userName.length > 0 ? userName : "User"}</Text>
                    <Group gap={6} wrap="wrap">
                        {roles.length === 0
                            ? <Badge variant="light" color="gray" size="sm">No role</Badge>
                            : roles.map(r => <Badge key={r} variant="light" color="blue" size="sm">{r}</Badge>)}
                        <Text size="xs" c="dimmed">{formatCurrentDate()}{asOf.length > 0 ? " · as of " + asOf : ""}</Text>
                    </Group>
                </Stack>
                <Tooltip label="Refresh Dashboard">
                    <Stack align="center" gap={2}>
                        <ActionIcon variant="default" size="lg" aria-label="Refresh" disabled={loading || !signedIn}
                                    onClick={() => setReload(previous => previous + 1)}>
                            <svg {...svgProps}><polyline points="23 4 23 10 17 10" /><polyline points="1 20 1 14 7 14" />
                                <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" /></svg>
                        </ActionIcon>
                        <Text size="xs" c={loading ? "dimmed" : undefined}>Refresh</Text>
                    </Stack>
                </Tooltip>
            </Group>
            {loading && (
                <div style={{ position: "absolute", inset: 0, display: "flex", alignItems: "center", justifyContent: "center", zIndex: 10 }}>
                    <Loader size="lg" />
                </div>
            )}
            <Stack gap="md" p="md" style={{ width: "100%", maxWidth: 1400, margin: "0 auto", boxSizing: "border-box" }}>
                {messages.length > 0 && (
                    <Alert color="red" withCloseButton onClose={() => setMessages([])}>
                        {messages.map((m,i) => <div key={i}>{m}</div>)}
                    </Alert>
                )}
                {!signedIn && state.status !== "restoring" && (
                    <Paper withBorder radius="md" p="md">
                        <Text size="sm">Log in to see the dashboard.</Text>
                    </Paper>
                )}
                {forbidden && (
                    <Paper withBorder radius="md" p="md">
                        <Text size="sm">You do not have access to the dashboard metrics.</Text>
                    </Paper>
                )}
                {output != null && !forbidden && (
                    <>
                        <Panels output={output} />
                        <Details output={output} />
                    </>
                )}
            </Stack>
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
