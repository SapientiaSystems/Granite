//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Survey
//////////////////////////////////////////////////////////////////////////////

import { Fragment, useEffect, useState }                 from "react";
import type { CSSProperties, ReactElement }              from "react";
import { Alert, Button, Group, Slider, Stack, Text,
         Textarea, Title }                               from "@mantine/core";
import { GraniteFrontEndControllerServiceClient }        from "granite.frontendcontroller.client/RequestReply";
import type { IGraniteFrontEndControllerServiceClient }  from "granite.frontendcontroller.client/RequestReply";
import { GetOutputRequestBuilder, HandleInputRequestBuilder } from "strata.frontendcontroller.service/RequestReply";
import type { GetSurveyOutputReply, HandleSurveyInputReply,
              SurveyInput, SurveyOutput }                from "granite.frontendcontroller.service/RequestReply";
import type { SurveyQuestionData }                       from "granite.survey.service/RequestReply";
import { useAuthentication }                             from "granite.web.authentication/Authentication";
import { SURVEY_MARKS, answerLabel, isComplete, questionKey, surveyQuestions,
         toSurveyDraft, validateSurveyDraft }            from "./SurveyForm";

const SOURCE:      string = "Survey.New";
const DESTINATION: string = "SurveyView";
const SERVICE:     string = "frontendcontroller-user-service";

export interface ISurveyViewProps
{
    readonly service?:   string;
    readonly onDismiss?: () => void;
    readonly className?: string;
    readonly style?:     CSSProperties;
}

export function SurveyView(props: ISurveyViewProps): ReactElement
{
    const service: string                 = props.service ?? SERVICE;
    const { state, createFrontEndClient } = useAuthentication();
    const [questions, setQuestions]       = useState<Array<SurveyQuestionData>>(surveyQuestions(null));
    const [values, setValues]             = useState<Record<string,number | null>>({});
    const [comment, setComment]           = useState<string>("");
    const [messages, setMessages]         = useState<Array<string>>([]);
    const [busy, setBusy]                 = useState<boolean>(false);
    const [submitted, setSubmitted]       = useState<boolean>(false);
    const signedIn: boolean               = state.status === "authenticated" || state.status === "reauthenticating";

    const createClient = (): IGraniteFrontEndControllerServiceClient =>
        createFrontEndClient((url,fetchFunction) => new GraniteFrontEndControllerServiceClient(url,fetchFunction),service);

    useEffect(() =>
    {
        if (!signedIn)
        {
            return;
        }

        const client: IGraniteFrontEndControllerServiceClient = createClient();

        client
            .getSurveyOutput(GetOutputRequestBuilder.newBuilder().setSource(SOURCE).setDestination(DESTINATION).build())
            .thenApply((reply: GetSurveyOutputReply) =>
            {
                if (reply.success !== false && reply.output != null)
                    setQuestions(surveyQuestions(reply.output.questions));

                return reply;
            })
            .exceptionally(() => ({} as GetSurveyOutputReply))
            .subscribe(undefined,() => undefined);

        return () => { client.close(); };
    }, [signedIn]);

    function reset(): void
    {
        setValues({});
        setComment("");
        setMessages([]);
        setSubmitted(false);
    }

    function dismiss(): void
    {
        reset();
        props.onDismiss?.();
    }

    function submit(): void
    {
        const draft    = toSurveyDraft(questions,values,comment);
        const problems = validateSurveyDraft(questions,draft);

        if (problems.length > 0)
        {
            setMessages(problems);
            return;
        }

        const client: IGraniteFrontEndControllerServiceClient = createClient();
        const input: SurveyInput =
            {
                userId:      null,
                source:      SOURCE,
                command:     "Submit",
                destination: DESTINATION,
                answers:     draft.answers,
                comment:     draft.comment
            } as SurveyInput;

        setBusy(true);
        setMessages([]);
        client
            .handleSurveyInput(HandleInputRequestBuilder.newBuilder<SurveyInput>().setInput(input).build())
            .thenApply((reply: HandleSurveyInputReply) =>
            {
                client.close();
                setBusy(false);

                const output: SurveyOutput | undefined = reply.output;

                if (reply.success === false || output == null)
                    setMessages([reply.failureMessage ?? "Unable to submit survey"]);
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
                setMessages([error?.message ?? "Unable to submit survey"]);
                return {} as HandleSurveyInputReply;
            })
            .subscribe(undefined,(error: Error) =>
            {
                client.close();
                setBusy(false);
                setMessages([error?.message ?? "Unable to submit survey"]);
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
                <Text size="sm" c="dimmed">Your survey has been submitted successfully.</Text>
                <Button variant="light" size="sm" color="blue" onClick={dismiss}>Dismiss</Button>
            </Stack>
        );
    }

    return (
        <Stack gap="lg" p="md" className={props.className} style={containerStyle}>
            <Title order={3} style={{ flexShrink: 0 }}>Take Survey</Title>
            {!signedIn && <Alert color="yellow">Login to take the survey.</Alert>}
            {messages.length > 0 &&
            <Alert color="red" withCloseButton onClose={() => setMessages([])}>
                {messages.map((m, i) => <div key={i}>{m}</div>)}
            </Alert>}

            <div style={{ display: "grid", gridTemplateColumns: "auto 240px", gap: "24px 16px", alignItems: "center", justifyContent: "start" }}>
                {questions.map(q =>
                {
                    const key: string          = questionKey(q);
                    const value: number | null = values[key] ?? null;

                    return (
                        <Fragment key={key}>
                            <Text size="md" fw={600} style={{ textAlign: "right", paddingRight: 24 }}>
                                {q.label}
                            </Text>
                            <div style={{ paddingBottom: 24 }}>
                                <Slider
                                    min={SURVEY_MARKS[0].value}
                                    max={SURVEY_MARKS[SURVEY_MARKS.length - 1].value}
                                    step={1}
                                    marks={SURVEY_MARKS}
                                    value={value ?? SURVEY_MARKS[0].value}
                                    onChange={(v) => setValues(prev => ({ ...prev, [key]: v }))}
                                    label={(v) => value !== null ? answerLabel(q,v) : null}
                                    labelAlwaysOn={value !== null}
                                    color="teal.8"
                                    styles={{
                                        thumb: { opacity: value === null ? 0 : 1, transition: "opacity 150ms ease" },
                                        track: { opacity: value === null ? 0.4 : 1, transition: "opacity 150ms ease" },
                                        label: { top: "unset", bottom: -30, fontSize: 11, whiteSpace: "nowrap" },
                                    }}
                                />
                            </div>
                        </Fragment>
                    );
                })}
            </div>

            <Textarea
                label="What's one thing we could improve about Granite?"
                placeholder="Share your thoughts..."
                value={comment}
                onChange={(e) => setComment(e.currentTarget.value)}
                minRows={3}
                size="sm"
            />

            <Group>
                <Button variant="filled" size="sm" color="blue" loading={busy}
                        disabled={!signedIn || !isComplete(questions,values)} onClick={submit}>
                    Submit Survey
                </Button>
            </Group>
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
