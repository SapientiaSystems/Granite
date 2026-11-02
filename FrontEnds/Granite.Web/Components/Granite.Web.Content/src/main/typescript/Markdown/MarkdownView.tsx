//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Content
//////////////////////////////////////////////////////////////////////////////

import { useMemo }                                      from "react";
import type { CSSProperties, ReactElement }             from "react";
import { Anchor, Code, List, Stack, Text, Title }       from "@mantine/core";
import { contentKeyOf, isExternalLink, parseMarkdown }  from "./MarkdownParser";
import type { BlockNode, InlineNode }                   from "./MarkdownParser";

export interface IMarkdownViewProps
{
    readonly source:      string | null | undefined;
    readonly onNavigate?: (key: string) => void;
    readonly className?:  string;
    readonly style?:      CSSProperties;
}

function renderInline(nodes: Array<InlineNode>, onNavigate: ((key: string) => void) | undefined): Array<ReactElement | string>
{
    return nodes.map((node, i) =>
    {
        switch (node.type)
        {
            case "strong":
                return <strong key={i}>{node.text}</strong>;
            case "em":
                return <em key={i}>{node.text}</em>;
            case "code":
                return <Code key={i}>{node.text}</Code>;
            case "link":
            {
                const key: string | null = contentKeyOf(node.href);

                if (key != null && onNavigate != null)
                    return <Anchor key={i} component="button" type="button" size="sm" onClick={() => onNavigate(key)}>{node.text}</Anchor>;

                if (isExternalLink(node.href))
                    return <Anchor key={i} href={node.href} target="_blank" rel="noopener noreferrer" size="sm">{node.text}</Anchor>;

                return <span key={i}>{node.text}</span>;
            }
            default:
                return node.text;
        }
    });
}

function renderBlock(block: BlockNode, index: number, onNavigate: ((key: string) => void) | undefined): ReactElement
{
    switch (block.type)
    {
        case "heading":
            return <Title key={index} order={Math.min(6,Math.max(2,block.level + 1)) as 2 | 3 | 4 | 5 | 6}>{renderInline(block.content,onNavigate)}</Title>;
        case "list":
            return (
                <List key={index} type={block.ordered ? "ordered" : "unordered"} size="sm" spacing={4}>
                    {block.items.map((item, i) => <List.Item key={i}>{renderInline(item,onNavigate)}</List.Item>)}
                </List>
            );
        case "code":
            return <Code key={index} block>{block.text}</Code>;
        default:
            return <Text key={index} size="sm">{renderInline(block.content,onNavigate)}</Text>;
    }
}

export function MarkdownView(props: IMarkdownViewProps): ReactElement
{
    const blocks = useMemo(() => parseMarkdown(props.source), [props.source]);

    return (
        <Stack gap="xs" className={props.className} style={props.style}>
            {blocks.map((block, i) => renderBlock(block,i,props.onNavigate))}
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
