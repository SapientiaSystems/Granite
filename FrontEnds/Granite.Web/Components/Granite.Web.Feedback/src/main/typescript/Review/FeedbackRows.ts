//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Feedback
//////////////////////////////////////////////////////////////////////////////

import type { FeedbackData, FeedbackStatus,
              UpdateFeedbackData }           from "granite.feedback.service/RequestReply";
import { fieldLabel }                        from "../Feedback/FeedbackForm";

export interface IFeedbackRow
{
    readonly rowKey:     string;
    readonly feedbackId: number;
    readonly userId?:    number;
    readonly userName:   string;
    readonly type?:      string;
    readonly summary:    string;
    readonly details:    string;
    readonly status:     FeedbackStatus;
    readonly notes:      string;
    readonly created?:   string;
    readonly sent?:      string;
    readonly received?:  string;
}

export const FEEDBACK_STATUSES: Array<FeedbackStatus> = ["NEW", "SENT", "RECEIVED"];

export function detailsText(item: FeedbackData): string
{
    return Object
        .entries(item.details ?? {})
        .map(([key, value]) => fieldLabel(item.type,key) + ": " + value)
        .join(" | ");
}

export function toFeedbackRows(feedback: Array<FeedbackData> | undefined, userNames: Record<string,string> | undefined): Array<IFeedbackRow>
{
    return (feedback ?? [])
        .filter(item => item.feedbackId != null)
        .map(item =>
        ({
            rowKey:     String(item.feedbackId),
            feedbackId: item.feedbackId as number,
            userId:     item.userId,
            userName:   userNames?.[String(item.userId)] ?? (item.userId == null ? "" : "#" + item.userId),
            type:       item.type,
            summary:    item.summary ?? "",
            details:    detailsText(item),
            status:     item.status ?? "NEW",
            notes:      item.notes ?? "",
            created:    item.created,
            sent:       item.sent,
            received:   item.received,
        }));
}

export function changedFeedback(original: Array<IFeedbackRow>, edited: Array<IFeedbackRow>): Array<UpdateFeedbackData>
{
    const before:  Map<string,IFeedbackRow>   = new Map(original.map(row => [row.rowKey, row]));
    const changes: Array<UpdateFeedbackData> = [];

    for (const row of edited)
    {
        const was: IFeedbackRow | undefined = before.get(row.rowKey);

        if (was != null && (was.status !== row.status || was.notes !== row.notes))
        {
            changes.push({ feedbackId: row.feedbackId, status: row.status, notes: row.notes });
        }
    }

    return changes;
}

export function formatInstant(value: string | null | undefined): string
{
    if (value == null || value === "")
    {
        return "";
    }

    const time: Date = new Date(value);

    return Number.isNaN(time.getTime()) ? value : time.toLocaleString();
}

//////////////////////////////////////////////////////////////////////////////
