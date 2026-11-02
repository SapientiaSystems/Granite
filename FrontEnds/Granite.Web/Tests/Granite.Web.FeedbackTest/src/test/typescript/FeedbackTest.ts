//////////////////////////////////////////////////////////////////////////////
// Granite.Web.FeedbackTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }                                  from "vitest";
import { feedbackFields, feedbackTypeLabel, fieldLabel, toDraft,
         validateDraft, MAX_SUMMARY_LENGTH }                     from "granite.web.feedback/Feedback";
import { changedFeedback, detailsText, toFeedbackRows }          from "granite.web.feedback/Review";
import type { FeedbackData }                                     from "granite.feedback.service/RequestReply";

describe("FeedbackForm",() =>
{
    it("declares the fields of each type",() =>
    {
        expect(feedbackFields("DEFECT").map(f => f.key)).toEqual(["stepsToReproduce","expectedBehavior","actualBehavior"]);
        expect(feedbackFields("KUDOS").map(f => f.key)).toEqual(["whatYouLike","additionalComments"]);
        expect(feedbackTypeLabel("SUGGESTION")).toBe("Suggestion");
        expect(fieldLabel("PROBLEM","impact")).toBe("Impact");
    });

    it("keeps only the type's non-blank fields, trimmed",() =>
    {
        const draft = toDraft("PROBLEM"," Slow ",{ description: " Takes ages ", impact: " ", whatYouLike: "ignored" });

        expect(draft).toEqual({ type: "PROBLEM", summary: "Slow", details: { description: "Takes ages" } });
    });

    it("requires a summary within the length limit",() =>
    {
        expect(validateDraft(toDraft("KUDOS","",{}))).toEqual(["Summary is required"]);
        expect(validateDraft(toDraft("KUDOS","x".repeat(MAX_SUMMARY_LENGTH + 1),{})).length).toBe(1);
        expect(validateDraft(toDraft("KUDOS","Great",{}))).toEqual([]);
    });
});

describe("FeedbackRows",() =>
{
    const item: FeedbackData =
        { feedbackId: 5, userId: 7, type: "DEFECT", summary: "Broken", details: { actualBehavior: "Nothing" }, status: "NEW" };

    it("joins user names and labels the details",() =>
    {
        const rows = toFeedbackRows([item,{ ...item, feedbackId: 6, userId: 8 }],{ "7": "alice" });

        expect(rows.map(r => r.userName)).toEqual(["alice","#8"]);
        expect(detailsText(item)).toBe("Actual Behavior: Nothing");
        expect(rows[0].notes).toBe("");
    });

    it("reports only rows whose status or notes changed",() =>
    {
        const original = toFeedbackRows([item],{});
        const edited   = [{ ...original[0], status: "SENT" as const, notes: "forwarded" }];

        expect(changedFeedback(original,original)).toEqual([]);
        expect(changedFeedback(original,edited)).toEqual([{ feedbackId: 5, status: "SENT", notes: "forwarded" }]);
    });
});

//////////////////////////////////////////////////////////////////////////////
