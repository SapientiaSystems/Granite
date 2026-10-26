//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Navigator
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useState }                       from "react";
import type { ReactElement }                        from "react";
import { Badge, Group, Loader, Paper,
         Stack, Text, Title }                       from "@mantine/core";
import { FakeHomeFrontEndControllerServiceClient } from "granite.frontendcontroller.client/RequestReply";
import { GetOutputRequestBuilder }               from "strata.frontendcontroller.service/RequestReply";
import type { GetHomeOutputReply }                       from "granite.frontendcontroller.service/RequestReply";

function formatUserName(firstName: string, lastName: string, userName: string): string
{
    const full: string = (firstName + " " + lastName).trim();

    if (full.length > 0)
    {
        return full;
    }

    return userName.length > 0 ? userName : "User";
}

function formatCurrentDate(): string
{
    const now: Date = new Date();

    return now.toLocaleDateString("en-US", {
        weekday: "long",
        year:    "numeric",
        month:   "long",
        day:     "numeric",
    });
}

export function HomeView(): ReactElement
{
    const [userName, setUserName]   = useState<string>("");
    const [firstName, setFirstName] = useState<string>("");
    const [lastName, setLastName]   = useState<string>("");
    const [role, setRole]           = useState<string>("");
    const [loading, setLoading]     = useState<boolean>(true);

    useEffect(() =>
    {
        const service: FakeHomeFrontEndControllerServiceClient = new FakeHomeFrontEndControllerServiceClient();

        service
            .getHomeOutput(
                GetOutputRequestBuilder
                    .newBuilder()
                    .setUserId(1)
                    .setSource("Home")
                    .setDestination("HomeView")
                    .build())
            .thenApply(
                (reply: GetHomeOutputReply) =>
                {
                    const home = reply.output;

                    if (home)
                    {
                        setUserName(home.userName ?? "");
                        setFirstName(home.firstName ?? "");
                        setLastName(home.lastName ?? "");
                        setRole(home.role ?? "");
                    }

                    setLoading(false);
                    return reply;
                })
            .exceptionally((error: Error) =>
            {
                console.error("Failed to get Home output", error);
                setLoading(false);
                return {} as GetHomeOutputReply;
            })
            .subscribe();

        return () => { service.close(); };
    }, []);

    return (
        <Stack gap={0} className="view-container">
            <Group gap="sm" className="view-toolbar" px="sm" py={6} justify="space-between" align="center">
                <Stack gap={0}>
                    <Text size="sm" fw={500}>Current User: {formatUserName(firstName, lastName, userName)}</Text>
                    <Group gap={6}>
                        <Text size="xs" c="dimmed">Role:</Text>
                        <Badge variant="light" color="blue" size="sm">{role || "Unknown"}</Badge>
                    </Group>
                </Stack>
                <Text size="sm" c="dimmed">{formatCurrentDate()}</Text>
            </Group>
            <Stack gap="lg" p="md" style={{ flex: 1, minHeight: 0, overflowY: "auto", position: "relative" }}>
                {loading && (
                    <div style={{
                        position: "absolute", inset: 0, display: "flex",
                        alignItems: "center", justifyContent: "center", zIndex: 10,
                    }}>
                        <Loader size="lg" />
                    </div>
                )}

                <Paper p="md" radius="md" withBorder>
                    <Title order={4} mb="sm">Welcome</Title>
                    <Text size="sm">
                        This is the Home view of the Granite web application template.
                        Replace this content with dashboards and widgets relevant to your
                        domain, and wire the HomeView to your own FrontEndController
                        output types.
                    </Text>
                </Paper>
            </Stack>
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
