//////////////////////////////////////////////////////////////////////////////
// Granite.Web.ContentTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }                               from "vitest";
import { contentKeyOf, isExternalLink, parseInline,
         parseMarkdown }                                      from "granite.web.content/Markdown";
import { INITIAL_HISTORY, back, canGoBack, canGoForward,
         currentLocation, forward, navigate }                 from "granite.web.content/Search";

describe("MarkdownParser",() =>
{
    it("parses inline emphasis, code and links",() =>
    {
        expect(parseInline("See **bold**, *it*, `x` and [Users](content:granite/users-help).")).toEqual(
        [
            { type: "text",   text: "See " },
            { type: "strong", text: "bold" },
            { type: "text",   text: ", " },
            { type: "em",     text: "it" },
            { type: "text",   text: ", " },
            { type: "code",   text: "x" },
            { type: "text",   text: " and " },
            { type: "link",   text: "Users", href: "content:granite/users-help" },
            { type: "text",   text: "." },
        ]);
    });

    it("parses headings, paragraphs, lists and code blocks",() =>
    {
        const blocks = parseMarkdown("# Title\r\n\r\nLine one\r\nline two\r\n\r\n- a\r\n- b\r\n\r\n1. first\r\n\r\n```\r\ncode()\r\n```\r\n");

        expect(blocks.map(b => b.type)).toEqual(["heading","paragraph","list","list","code"]);
        expect(blocks[1]).toEqual({ type: "paragraph", content: [{ type: "text", text: "Line one line two" }] });
        expect(blocks[2]).toEqual({ type: "list", ordered: false, items: [[{ type: "text", text: "a" }],[{ type: "text", text: "b" }]] });
        expect(blocks[3]).toMatchObject({ type: "list", ordered: true });
        expect(blocks[4]).toEqual({ type: "code", text: "code()" });
        expect(parseMarkdown(null)).toEqual([]);
    });

    it("classifies links",() =>
    {
        expect(contentKeyOf("content:granite/x")).toBe("granite/x");
        expect(contentKeyOf("https://example.com")).toBeNull();
        expect(isExternalLink("https://example.com")).toBe(true);
        expect(isExternalLink("javascript:alert(1)")).toBe(false);
    });
});

describe("ContentHistory",() =>
{
    it("navigates, goes back and forward, and drops the forward branch",() =>
    {
        let history = navigate(INITIAL_HISTORY,{ view: "article", key: "a" });

        history = navigate(history,{ view: "article", key: "b" });
        expect(currentLocation(history)).toEqual({ view: "article", key: "b" });

        history = back(history);
        expect(currentLocation(history)).toEqual({ view: "article", key: "a" });
        expect(canGoForward(history)).toBe(true);

        history = navigate(history,{ view: "article", key: "c" });
        expect(canGoForward(history)).toBe(false);
        expect(history.entries.length).toBe(3);

        history = forward(back(back(history)));
        expect(currentLocation(history)).toEqual({ view: "article", key: "a" });
        expect(canGoBack(INITIAL_HISTORY)).toBe(false);
    });

    it("ignores navigation to the current location",() =>
    {
        expect(navigate(INITIAL_HISTORY,{ view: "topics" })).toBe(INITIAL_HISTORY);
    });
});

//////////////////////////////////////////////////////////////////////////////
