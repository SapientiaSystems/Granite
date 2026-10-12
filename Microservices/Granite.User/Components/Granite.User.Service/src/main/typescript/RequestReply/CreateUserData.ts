export
interface CreateUserData
{
    userName?: string;
    password?: string;
    externalIdentity?: string;
    authorities?: Array<string>;
}
