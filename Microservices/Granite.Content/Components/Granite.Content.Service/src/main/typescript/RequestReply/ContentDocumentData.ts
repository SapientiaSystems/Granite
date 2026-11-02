import type { ContentKind } from "./ContentKind";
import type { ContentFormat } from "./ContentFormat";

export
interface ContentDocumentData
{
    documentId?: number;
    key?: string;
    title?: string;
    kind?: ContentKind;
    format?: ContentFormat;
    body?: string;
    keywords?: Array<string>;
    requiredAuthorities?: Array<string>;
    source?: string;
    version?: string;
    updated?: string;
}
