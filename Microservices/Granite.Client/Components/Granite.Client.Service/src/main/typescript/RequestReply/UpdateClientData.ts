export
interface UpdateClientData
{
    clientId?: string;
    name?: string;
    description?: string;
    authorities?: Array<string>;
    owners?: Array<number>;
    enabled?: boolean;
    secretExpiresAt?: string;
    version?: number;
}
