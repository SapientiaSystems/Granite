//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Feedback
//////////////////////////////////////////////////////////////////////////////

import type { FeedbackType } from "granite.feedback.service/RequestReply";

export interface IFeedbackField
{
    readonly key:         string;
    readonly label:       string;
    readonly placeholder: string;
    readonly multiline:   boolean;
    readonly minRows:     number;
}

export interface IFeedbackDraft
{
    readonly type:    FeedbackType;
    readonly summary: string;
    readonly details: Record<string,string>;
}

export const MAX_SUMMARY_LENGTH: number = 512;
export const MAX_DETAIL_LENGTH:  number = 4000;

export const FEEDBACK_TYPES: Array<{ value: FeedbackType; label: string }> =
[
    { value: "DEFECT",     label: "Defect" },
    { value: "PROBLEM",    label: "Problem" },
    { value: "SUGGESTION", label: "Suggestion" },
    { value: "KUDOS",      label: "Kudos" },
];

const FIELDS: Record<FeedbackType,Array<IFeedbackField>> =
{
    DEFECT:
    [
        { key: "stepsToReproduce", label: "Steps to Reproduce", placeholder: "1. Navigate to...\n2. Click on...\n3. Observe...", multiline: true, minRows: 3 },
        { key: "expectedBehavior", label: "Expected Behavior",  placeholder: "What should have happened",                         multiline: true, minRows: 2 },
        { key: "actualBehavior",   label: "Actual Behavior",    placeholder: "What actually happened",                            multiline: true, minRows: 2 },
    ],
    PROBLEM:
    [
        { key: "description", label: "Description", placeholder: "Describe the problem in detail",         multiline: true,  minRows: 3 },
        { key: "impact",      label: "Impact",      placeholder: "How does this affect your work?",        multiline: false, minRows: 1 },
        { key: "workaround",  label: "Workaround",  placeholder: "Any workaround you are currently using", multiline: true,  minRows: 2 },
    ],
    SUGGESTION:
    [
        { key: "description",     label: "Description",      placeholder: "Describe your suggestion",         multiline: true, minRows: 3 },
        { key: "expectedBenefit", label: "Expected Benefit", placeholder: "How would this improvement help?", multiline: true, minRows: 2 },
    ],
    KUDOS:
    [
        { key: "whatYouLike",        label: "What do you like?",   placeholder: "Tell us what's working well for you", multiline: true, minRows: 3 },
        { key: "additionalComments", label: "Additional Comments", placeholder: "Anything else you'd like to share",   multiline: true, minRows: 2 },
    ],
};

export function feedbackFields(type: FeedbackType): Array<IFeedbackField>
{
    return FIELDS[type] ?? [];
}

export function feedbackTypeLabel(type: FeedbackType | null | undefined): string
{
    return FEEDBACK_TYPES.find(t => t.value === type)?.label ?? (type ?? "");
}

export function fieldLabel(type: FeedbackType | null | undefined, key: string): string
{
    return type == null ? key : (feedbackFields(type).find(f => f.key === key)?.label ?? key);
}

export function toDraft(type: FeedbackType, summary: string, values: Record<string,string>): IFeedbackDraft
{
    const details: Record<string,string> = {};

    for (const field of feedbackFields(type))
    {
        const value: string = (values[field.key] ?? "").trim();

        if (value !== "")
        {
            details[field.key] = value;
        }
    }

    return { type, summary: summary.trim(), details };
}

export function validateDraft(draft: IFeedbackDraft): Array<string>
{
    const problems: Array<string> = [];

    if (draft.summary === "")
    {
        problems.push("Summary is required");
    }
    else if (draft.summary.length > MAX_SUMMARY_LENGTH)
    {
        problems.push("Summary is longer than " + MAX_SUMMARY_LENGTH + " characters");
    }

    for (const [key, value] of Object.entries(draft.details))
    {
        if (value.length > MAX_DETAIL_LENGTH)
        {
            problems.push(fieldLabel(draft.type,key) + " is longer than " + MAX_DETAIL_LENGTH + " characters");
        }
    }

    return problems;
}

//////////////////////////////////////////////////////////////////////////////
