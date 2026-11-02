import type { ContentKind } from "./ContentKind";

export
interface ContentHitData
{
    key?: string;
    title?: string;
    kind?: ContentKind;
    snippet?: string;
    score?: number;
}
