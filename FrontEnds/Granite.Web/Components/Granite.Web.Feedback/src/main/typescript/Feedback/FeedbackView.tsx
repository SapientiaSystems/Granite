//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Feedback
//////////////////////////////////////////////////////////////////////////////

import { useState }                                      from "react";
import type { CSSProperties, ReactElement }              from "react";
import { Alert, Button, Group, Radio, Stack, Text,
         Textarea, TextInput, Title }                    from "@mantine/core";
import { GraniteFrontEndControllerServiceClient }        from "granite.frontendcontroller.client/RequestReply";
import type { IGraniteFrontEndControllerServiceClient }  from "granite.frontendcontroller.client/RequestReply";
import { HandleInputRequestBuilder }                     from "strata.frontendcontroller.service/RequestReply";
import type { FeedbackInput, FeedbackOutput,
              HandleFeedbackInputReply }                 from "granite.frontendcontroller.service/RequestReply";
import type { FeedbackType }                             from "granite.feedback.service/RequestReply";
import { useAuthentication }                             from "granite.web.authentication/Authentication";
import { FEEDBACK_TYPES, feedbackFields, toDraft, validateDraft } from "./FeedbackForm";

const SOURCE:      string = "Feedback.New";
const DESTINATION: string = "FeedbackView";
const SERVICE:     string = "frontendcontroller-user-service";

export interface IFeedbackViewProps
{
    readonly service?:   string;
    readonly className?: string;
    readonly style?:     CSSProperties;
}

export function FeedbackView(props: IFeedbackViewProps): ReactElement
{
    const service: string                   = props.service ?? SERVICE;
    const { state, createFrontEndClient }   = useAuthentication();
    const [type, setType]                   = useState<FeedbackType>("DEFECT");
    const [summary, setSummary]             = useState<string>("");
    const [values, setValues]               = useState<Record<string,string>>({});
    const [messages, setMessages]           = useState<Array<string>>([]);
    const [busy, setBusy]                   = useState<boolean>(false);
    const [submitted, setSubmitted]         = useState<boolean>(false);
    const signedIn: boolean                 = state.status === "authenticated" || state.status === "reauthenticating";

    function changeType(value: string): void
    {
        setType(value as FeedbackType);
        setValues({});
        setMessages([]);
    }

    function reset(): void
    {
        setSummary("");
        setValues({});
        setMessages([]);
        setSubmitted(false);
    }

    function submit(): void
    {
        const draft    = toDraft(type,summary,values);
        const problems = validateDraft(draft);

        if (problems.length > 0)
        {
            setMessages(problems);
            return;
        }

        const client: IGraniteFrontEndControllerServiceClient =
            createFrontEndClient((url,fetchFunction) => new GraniteFrontEndControllerServiceClient(url,fetchFunction),service);
        const input: FeedbackInput =
            {
                userId:      null,
                source:      SOURCE,
                command:     "Submit",
                destination: DESTINATION,
                type:        draft.type,
                summary:     draft.summary,
                details:     draft.details
            } as FeedbackInput;

        setBusy(true);
        setMessages([]);
        client
            .handleFeedbackInput(HandleInputRequestBuilder.newBuilder<FeedbackInput>().setInput(input).build())
            .thenApply((reply: HandleFeedbackInputReply) =>
            {
                client.close();
                setBusy(false);

                const output: FeedbackOutput | undefined = reply.output;

                if (reply.success === false || output == null)
                    setMessages([reply.failureMessage ?? "Unable to submit feedback"]);
                else if ((output.failureMessages ?? []).length > 0)
                    setMessages(output.failureMessages);
                else
                    setSubmitted(true);

                return reply;
            })
            .exceptionally((error: Error) =>
            {
                client.close();
                setBusy(false);
                setMessages([error?.message ?? "Unable to submit feedback"]);
                return {} as HandleFeedbackInputReply;
            })
            .subscribe(undefined,(error: Error) =>
            {
                client.close();
                setBusy(false);
                setMessages([error?.message ?? "Unable to submit feedback"]);
            });
    }

    const containerStyle: CSSProperties = { flex: 1, minWidth: 0, minHeight: 0, overflowY: "auto", ...props.style };

    if (submitted)
    {
        return (
            <Stack gap="md" p="md" align="center" justify="center" className={props.className} style={containerStyle}>
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#2b8a3e"
                     strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <circle cx="12" cy="12" r="10" />
                    <polyline points="9 12 12 15 16 9" />
                </svg>
                <Title order={3}>Thank You</Title>
                <Text size="sm" c="dimmed">Your feedback has been submitted successfully.</Text>
                <Button variant="default" size="sm" onClick={reset}>Submit more feedback</Button>
            </Stack>
        );
    }

    return (
        <Stack gap="md" p="md" className={props.className} style={containerStyle}>
            <Title order={3} style={{ flexShrink: 0 }}>Provide Feedback</Title>
            {!signedIn && <Alert color="yellow">Log in to send feedback.</Alert>}
            {messages.length > 0 &&
            <Alert color="red" withCloseButton onClose={() => setMessages([])}>
                {messages.map((m, i) => <div key={i}>{m}</div>)}
            </Alert>}

            <Radio.Group value={type} onChange={changeType}>
                <Group gap="lg">
                    {FEEDBACK_TYPES.map(t => <Radio key={t.value} value={t.value} label={t.label} color="blue" />)}
                </Group>
            </Radio.Group>

            <Stack key={type} gap="sm">
                <TextInput label="Summary" placeholder="Brief summary" size="sm" required
                           value={summary} onChange={(e) => setSummary(e.currentTarget.value)} />
                {feedbackFields(type).map(field =>
                    field.multiline
                        ? <Textarea key={field.key} label={field.label} placeholder={field.placeholder} minRows={field.minRows}
                                    autosize size="sm" value={values[field.key] ?? ""}
                                    onChange={(e) => { const v = e.currentTarget.value; setValues(prev => ({ ...prev, [field.key]: v })); }} />
                        : <TextInput key={field.key} label={field.label} placeholder={field.placeholder} size="sm"
                                     value={values[field.key] ?? ""}
                                     onChange={(e) => { const v = e.currentTarget.value; setValues(prev => ({ ...prev, [field.key]: v })); }} />)}
                <Group>
                    <Button variant="filled" size="sm" color="blue" loading={busy} disabled={!signedIn} onClick={submit}>
                        Submit Feedback
                    </Button>
                </Group>
            </Stack>
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
