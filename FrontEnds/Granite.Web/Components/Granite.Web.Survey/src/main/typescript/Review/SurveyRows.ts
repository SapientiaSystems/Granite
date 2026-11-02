//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Survey
//////////////////////////////////////////////////////////////////////////////

import { questionKey }                                   from "../Survey/SurveyForm";
import type { SurveyQuestionData, SurveyResponseData,
              SurveyResponseStatus, UpdateSurveyResponseData } from "granite.survey.service/RequestReply";

export interface ISurveyRow
{
    readonly rowKey:     string;
    readonly responseId: number;
    readonly userId?:    number;
    readonly userName:   string;
    readonly answers:    Record<string,number>;
    readonly comment:    string;
    readonly status:     SurveyResponseStatus;
    readonly notes:      string;
    readonly created?:   string;
    readonly sent?:      string;
    readonly received?:  string;
}

export const SURVEY_STATUSES: Array<SurveyResponseStatus> = ["NEW", "SENT", "RECEIVED"];

export function toSurveyRows(responses: Array<SurveyResponseData> | undefined, userNames: Record<string,string> | undefined): Array<ISurveyRow>
{
    return (responses ?? [])
        .filter(item => item.responseId != null)
        .map(item =>
        ({
            rowKey:     String(item.responseId),
            responseId: item.responseId as number,
            userId:     item.userId,
            userName:   userNames?.[String(item.userId)] ?? (item.userId == null ? "" : "#" + item.userId),
            answers:    item.answers ?? {},
            comment:    item.comment ?? "",
            status:     item.status ?? "NEW",
            notes:      item.notes ?? "",
            created:    item.created,
            sent:       item.sent,
            received:   item.received,
        }));
}

export function changedResponses(original: Array<ISurveyRow>, edited: Array<ISurveyRow>): Array<UpdateSurveyResponseData>
{
    const before:  Map<string,ISurveyRow>          = new Map(original.map(row => [row.rowKey, row]));
    const changes: Array<UpdateSurveyResponseData> = [];

    for (const row of edited)
    {
        const was: ISurveyRow | undefined = before.get(row.rowKey);

        if (was != null && (was.status !== row.status || was.notes !== row.notes))
        {
            changes.push({ responseId: row.responseId, status: row.status, notes: row.notes });
        }
    }

    return changes;
}

export function averagesText(questions: Array<SurveyQuestionData>, averages: Record<string,number> | undefined): string
{
    return questions
        .filter(q => averages?.[questionKey(q)] != null)
        .map(q => questionKey(q) + " " + (averages?.[questionKey(q)] as number).toFixed(2))
        .join(" | ");
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
