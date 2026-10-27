//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { beforeEach, describe, expect, it } from "vitest";
import { KEYS, managerFor, redirectOutput, scriptedFrontEnd, sessionOutput, flush } from "./TestSupport";

class MemoryStorage
    implements Storage
{
    private readonly values: Map<string,string> = new Map();

    get length(): number { return this.values.size; }

    clear(): void { this.values.clear(); }

    getItem(key: string): string | null { return this.values.get(key) ?? null; }

    key(index: number): string | null { return Array.from(this.values.keys())[index] ?? null; }

    removeItem(key: string): void { this.values.delete(key); }

    setItem(key: string,value: string): void { this.values.set(key,value); }

    [name: string]: unknown;
}

function storedEntries(): Array<string>
{
    const entries: Array<string> = [];

    for (const storage of [window.localStorage,window.sessionStorage])
        for (let i = 0;i < storage.length;i++)
            entries.push(storage.key(i) + "=" + storage.getItem(storage.key(i) as string));

    return entries;
}

describe("browser storage",() =>
{
    beforeEach(() =>
    {
        Object.defineProperty(window,"localStorage",{ configurable: true,value: new MemoryStorage() });
        Object.defineProperty(window,"sessionStorage",{ configurable: true,value: new MemoryStorage() });
    });

    it("stays empty through restore, password login, re-authentication and logout",async () =>
    {
        const frontEnd = scriptedFrontEnd();
        const manager  = managerFor(frontEnd);

        await manager.restore();
        expect(storedEntries()).toEqual([]);

        await manager.login("alice","secret");
        expect(storedEntries()).toEqual([]);

        const retry = manager.handleUnauthorized();

        await flush();
        await manager.login("alice","secret");
        await retry;
        expect(storedEntries()).toEqual([]);

        await manager.logout();
        expect(storedEntries()).toEqual([]);
    });

    it("stays empty through the Okta flow",async () =>
    {
        const frontEnd = scriptedFrontEnd()
            .respond(KEYS.start,() => redirectOutput("https://okta.example/a"))
            .respond(KEYS.complete,() => sessionOutput(true,"okta.user",["ROLE_User"],"/users"));
        const manager  = managerFor(frontEnd);

        await manager.startOktaLogin("/users");
        await manager.completeOktaLogin("code","state");

        expect(storedEntries()).toEqual([]);
    });

    it("never writes a device key",async () =>
    {
        await managerFor(scriptedFrontEnd()).login("alice","secret");

        expect(window.localStorage.getItem("granite.auth.deviceId")).toBeNull();
    });

    it("exposes no token in the state",async () =>
    {
        const manager = managerFor(scriptedFrontEnd());

        await manager.login("alice","secret");

        expect(Object.keys(manager.getState()).sort()).toEqual(["authorities","status","userName"]);
    });
});

//////////////////////////////////////////////////////////////////////////////
