//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Survey
//////////////////////////////////////////////////////////////////////////////

import { GRANITE_SURVEY_QUESTIONS, SURVEY_MAX_ANSWER,
         SURVEY_MAX_COMMENT_LENGTH, SURVEY_MIN_ANSWER } from "granite.survey.service/RequestReply";
import type { SurveyQuestionData }                     from "granite.survey.service/RequestReply";

export interface ISurveyDraft
{
    readonly answers: Record<string,number>;
    readonly comment: string | null;
}

export const SURVEY_MARKS: Array<{ value: number; label: string }> =
    Array.from({ length: SURVEY_MAX_ANSWER - SURVEY_MIN_ANSWER + 1 },(_, i) => ({ value: SURVEY_MIN_ANSWER + i, label: String(SURVEY_MIN_ANSWER + i) }));

export function surveyQuestions(questions: Array<SurveyQuestionData> | null | undefined): Array<SurveyQuestionData>
{
    return questions != null && questions.length > 0 ? questions : GRANITE_SURVEY_QUESTIONS;
}

export function questionKey(question: SurveyQuestionData): string
{
    return question.key ?? "";
}

export function answerLabel(question: SurveyQuestionData, value: number | null | undefined): string
{
    return value == null ? "" : (question.labels?.[String(value)] ?? String(value));
}

export function isComplete(questions: Array<SurveyQuestionData>, values: Record<string,number | null>): boolean
{
    return questions.every(q => values[questionKey(q)] != null);
}

export function toSurveyDraft(questions: Array<SurveyQuestionData>, values: Record<string,number | null>, comment: string): ISurveyDraft
{
    const answers: Record<string,number> = {};

    for (const question of questions)
    {
        const value: number | null | undefined = values[questionKey(question)];

        if (value != null)
        {
            answers[questionKey(question)] = value;
        }
    }

    const trimmed: string = comment.trim();

    return { answers, comment: trimmed === "" ? null : trimmed };
}

export function validateSurveyDraft(questions: Array<SurveyQuestionData>, draft: ISurveyDraft): Array<string>
{
    const problems: Array<string> = [];

    for (const question of questions)
    {
        const value: number | undefined = draft.answers[questionKey(question)];

        if (value == null)
        {
            problems.push("Answer: " + (question.label ?? questionKey(question)));
        }
        else if (value < SURVEY_MIN_ANSWER || value > SURVEY_MAX_ANSWER)
        {
            problems.push((question.label ?? questionKey(question)) + " must be between " + SURVEY_MIN_ANSWER + " and " + SURVEY_MAX_ANSWER);
        }
    }

    if (draft.comment != null && draft.comment.length > SURVEY_MAX_COMMENT_LENGTH)
    {
        problems.push("Comment is longer than " + SURVEY_MAX_COMMENT_LENGTH + " characters");
    }

    return problems;
}

//////////////////////////////////////////////////////////////////////////////
