export
interface UserData
{
    userId: number;
    userName?: string;
    externalIdentity?: string;
    authorities?: Array<string>;
    values?: Map<string,unknown>;
}
