export
interface CreateClientData
{
    name?: string;
    description?: string;
    authorities?: Array<string>;
    owners?: Array<number>;
    enabled?: boolean;
    secretExpiresAt?: string;
}
