export
interface UserData
{
    userId: number;
    userName?: string;
    externalIdentity?: string;
    password?: string;
    authorities?: Array<string>;
    values?: Map<string,unknown>;
}
