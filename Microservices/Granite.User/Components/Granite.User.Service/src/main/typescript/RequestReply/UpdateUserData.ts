export
interface UpdateUserData
{
    userId: number;
    userName?: string;
    password?: string;
    externalIdentity?: string;
    authorities?: Array<string>;
    values?: Record<string,unknown>;
}
