//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Client
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement }                                from "react";
import { Alert, Button, CopyButton, Group, Modal, Stack, TextInput } from "@mantine/core";
import { DEFAULT_ENV_PREFIX, envFileName, toEnvFile }                         from "./EnvFile";

export interface ISecretReveal
{
    readonly clientId:   string;
    readonly clientName: string;
    readonly secret:     string;
}

export interface ISecretDialogProps
{
    readonly reveal:     ISecretReveal | null;
    readonly onClose:    () => void;
    readonly envPrefix?: string;
}

export function downloadEnvFile(reveal: ISecretReveal,envPrefix: string = DEFAULT_ENV_PREFIX): void
{
    const blob: Blob   = new Blob([toEnvFile(reveal.clientName,reveal.clientId,reveal.secret,envPrefix)],{ type: "text/plain" });
    const url:  string = URL.createObjectURL(blob);

    try
    {
        const anchor: HTMLAnchorElement = document.createElement("a");

        anchor.href     = url;
        anchor.download = envFileName(reveal.clientName);
        document.body.appendChild(anchor);
        anchor.click();
        document.body.removeChild(anchor);
    }
    finally
    {
        setTimeout(() => URL.revokeObjectURL(url),1000);
    }
}

function copyField(label: string,value: string,monospace: boolean): ReactElement
{
    return (
        <Group gap="xs" align="flex-end" wrap="nowrap">
            <TextInput label={label} value={value} readOnly style={{ flex: 1 }}
                       styles={monospace ? { input: { fontFamily: "monospace" } } : undefined} />
            <CopyButton value={value}>
                {({ copied,copy }) => <Button variant="default" onClick={copy}>{copied ? "Copied" : "Copy"}</Button>}
            </CopyButton>
        </Group>
    );
}

export function SecretDialog(props: ISecretDialogProps): ReactElement
{
    const reveal: ISecretReveal | null = props.reveal;

    return (
        <Modal opened={reveal !== null} onClose={props.onClose} title="Client secret" centered size="lg"
               closeOnClickOutside={false}>
            {reveal !== null &&
            <Stack gap="md">
                <Alert color="yellow">This secret will not be shown again.</Alert>
                {copyField("Client ID",reveal.clientId,true)}
                {copyField("Client secret",reveal.secret,true)}
                <Group justify="flex-end">
                    <Button variant="default" onClick={() => downloadEnvFile(reveal,props.envPrefix)}>Download .env</Button>
                    <Button onClick={props.onClose}>Close</Button>
                </Group>
            </Stack>}
        </Modal>
    );
}

//////////////////////////////////////////////////////////////////////////////
