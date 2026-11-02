//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Content
//////////////////////////////////////////////////////////////////////////////

export type InlineNode =
    | { readonly type: "text";   readonly text: string }
    | { readonly type: "strong"; readonly text: string }
    | { readonly type: "em";     readonly text: string }
    | { readonly type: "code";   readonly text: string }
    | { readonly type: "link";   readonly text: string; readonly href: string };

export type BlockNode =
    | { readonly type: "heading";   readonly level: number; readonly content: Array<InlineNode> }
    | { readonly type: "paragraph"; readonly content: Array<InlineNode> }
    | { readonly type: "list";      readonly ordered: boolean; readonly items: Array<Array<InlineNode>> }
    | { readonly type: "code";      readonly text: string };

export const CONTENT_LINK_PREFIX: string = "content:";

const INLINE = /(\*\*[^*]+\*\*|\*[^*\s][^*]*\*|_[^_\s][^_]*_|`[^`]+`|\[[^\]]+\]\([^)\s]+\))/g;

export function parseInline(text: string): Array<InlineNode>
{
    const nodes: Array<InlineNode> = [];
    let last: number = 0;

    for (const match of text.matchAll(INLINE))
    {
        const token: string = match[0];
        const at:    number = match.index ?? 0;

        if (at > last)
        {
            nodes.push({ type: "text", text: text.slice(last,at) });
        }

        if (token.startsWith("**"))
        {
            nodes.push({ type: "strong", text: token.slice(2,-2) });
        }
        else if (token.startsWith("`"))
        {
            nodes.push({ type: "code", text: token.slice(1,-1) });
        }
        else if (token.startsWith("["))
        {
            const close: number = token.indexOf("](");

            nodes.push({ type: "link", text: token.slice(1,close), href: token.slice(close + 2,-1) });
        }
        else
        {
            nodes.push({ type: "em", text: token.slice(1,-1) });
        }

        last = at + token.length;
    }

    if (last < text.length)
    {
        nodes.push({ type: "text", text: text.slice(last) });
    }

    return nodes;
}

export function parseMarkdown(source: string | null | undefined): Array<BlockNode>
{
    const lines:  Array<string>    = (source ?? "").replace(/\r\n/g,"\n").split("\n");
    const blocks: Array<BlockNode> = [];
    let paragraph: Array<string>   = [];
    let index: number              = 0;

    const flush = (): void =>
    {
        if (paragraph.length > 0)
        {
            blocks.push({ type: "paragraph", content: parseInline(paragraph.join(" ")) });
            paragraph = [];
        }
    };

    while (index < lines.length)
    {
        const line: string = lines[index];

        if (line.trim().startsWith("```"))
        {
            flush();

            const code: Array<string> = [];

            index++;

            while (index < lines.length && !lines[index].trim().startsWith("```"))
            {
                code.push(lines[index]);
                index++;
            }

            blocks.push({ type: "code", text: code.join("\n") });
            index++;
            continue;
        }

        const heading = /^\s{0,3}(#{1,6})\s+(.*)$/.exec(line);

        if (heading != null)
        {
            flush();
            blocks.push({ type: "heading", level: heading[1].length, content: parseInline(heading[2].trim()) });
            index++;
            continue;
        }

        const item = /^\s*([-*+]|\d+[.)])\s+(.*)$/.exec(line);

        if (item != null)
        {
            flush();

            const ordered: boolean                 = /\d/.test(item[1]);
            const items:   Array<Array<InlineNode>> = [];

            while (index < lines.length)
            {
                const next = /^\s*([-*+]|\d+[.)])\s+(.*)$/.exec(lines[index]);

                if (next == null || /\d/.test(next[1]) !== ordered)
                {
                    break;
                }

                items.push(parseInline(next[2].trim()));
                index++;
            }

            blocks.push({ type: "list", ordered, items });
            continue;
        }

        if (line.trim() === "")
        {
            flush();
        }
        else
        {
            paragraph.push(line.trim());
        }

        index++;
    }

    flush();
    return blocks;
}

export function contentKeyOf(href: string): string | null
{
    return href.startsWith(CONTENT_LINK_PREFIX) ? href.slice(CONTENT_LINK_PREFIX.length) : null;
}

export function isExternalLink(href: string): boolean
{
    return /^https?:\/\//i.test(href);
}

//////////////////////////////////////////////////////////////////////////////
