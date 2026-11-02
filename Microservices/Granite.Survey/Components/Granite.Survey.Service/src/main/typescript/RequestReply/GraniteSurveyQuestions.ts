import type { SurveyQuestionData } from "./SurveyQuestionData";

export const SURVEY_MIN_ANSWER:         number = 1;
export const SURVEY_MAX_ANSWER:         number = 5;
export const SURVEY_MAX_COMMENT_LENGTH: number = 2000;

export const GRANITE_SURVEY_QUESTIONS: Array<SurveyQuestionData> =
[
    {
        key:    "satisfied",
        label:  "Overall, how satisfied are you with Granite?",
        labels: { "1": "Very dissatisfied", "2": "Dissatisfied", "3": "Neutral", "4": "Satisfied", "5": "Very satisfied" },
    },
    {
        key:    "needs",
        label:  "How well does Granite meet your day-to-day needs?",
        labels: { "1": "Not at all", "2": "Slightly", "3": "Moderately", "4": "Very well", "5": "Extremely well" },
    },
    {
        key:    "recommend",
        label:  "How likely are you to recommend Granite to a colleague?",
        labels: { "1": "Not at all likely", "2": "Unlikely", "3": "Neutral", "4": "Likely", "5": "Extremely likely" },
    },
];
