//////////////////////////////////////////////////////////////////////////////
// Granite.Web.SurveyTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }                                 from "vitest";
import { SURVEY_MARKS, answerLabel, isComplete, surveyQuestions,
         toSurveyDraft, validateSurveyDraft }                   from "granite.web.survey/Survey";
import { averagesText, changedResponses, toSurveyRows }         from "granite.web.survey/Review";
import type { SurveyResponseData }                              from "granite.survey.service/RequestReply";

describe("SurveyForm",() =>
{
    const questions = surveyQuestions(null);

    it("falls back to the Granite questions",() =>
    {
        expect(questions.map(q => q.key)).toEqual(["satisfied","needs","recommend"]);
        expect(SURVEY_MARKS.map(m => m.value)).toEqual([1,2,3,4,5]);
        expect(answerLabel(questions[0],5)).toBe("Very satisfied");
        expect(answerLabel(questions[0],null)).toBe("");
    });

    it("is complete only when every question is answered",() =>
    {
        expect(isComplete(questions,{ satisfied: 4, needs: 3 })).toBe(false);
        expect(isComplete(questions,{ satisfied: 4, needs: 3, recommend: 5 })).toBe(true);
    });

    it("builds a trimmed draft and validates it",() =>
    {
        const draft = toSurveyDraft(questions,{ satisfied: 4, needs: null, recommend: 5, other: 2 },"  ");

        expect(draft).toEqual({ answers: { satisfied: 4, recommend: 5 }, comment: null });
        expect(validateSurveyDraft(questions,draft)).toEqual(["Answer: " + questions[1].label]);
        expect(validateSurveyDraft(questions,toSurveyDraft(questions,{ satisfied: 1, needs: 2, recommend: 3 }," Fine "))).toEqual([]);
        expect(validateSurveyDraft(questions,toSurveyDraft(questions,{ satisfied: 1, needs: 2, recommend: 3 },"x".repeat(2001))).length).toBe(1);
    });
});

describe("SurveyRows",() =>
{
    const item: SurveyResponseData =
        { responseId: 5, userId: 7, answers: { satisfied: 4, needs: 3, recommend: 5 }, comment: "Nice", status: "NEW" };

    it("maps responses to rows with user names",() =>
    {
        const rows = toSurveyRows([item,{ status: "NEW" }],{ "7": "alice" });

        expect(rows.length).toBe(1);
        expect(rows[0].userName).toBe("alice");
        expect(rows[0].answers.recommend).toBe(5);
        expect(toSurveyRows([item],{})[0].userName).toBe("#7");
    });

    it("reports only changed status or notes",() =>
    {
        const original = toSurveyRows([item],{});
        const edited   = [{ ...original[0], notes: "followed up" }];

        expect(changedResponses(original,original)).toEqual([]);
        expect(changedResponses(original,edited)).toEqual([{ responseId: 5, status: "NEW", notes: "followed up" }]);
    });

    it("formats averages in question order",() =>
    {
        expect(averagesText(surveyQuestions(null),{ recommend: 4.5, satisfied: 3.25 })).toBe("satisfied 3.25 | recommend 4.50");
        expect(averagesText(surveyQuestions(null),undefined)).toBe("");
    });
});

//////////////////////////////////////////////////////////////////////////////
