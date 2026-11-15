export
interface ClientData
{
    clientId?: string;
    name?: string;
    description?: string;
    authorities?: Array<string>;
    owners?: Array<number>;
    enabled?: boolean;
    secretExpiresAt?: string;
    lastTokenIssuedAt?: string;
    createdAt?: string;
    updatedAt?: string;
    version?: number;
}
