//////////////////////////////////////////////////////////////////////////////
// Granite.Web.UserTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it, vi }  from "vitest";
import { createDetailGridRegistry }  from "granite.web.user/EditableView";

describe("DetailGridRegistry", () =>
{
    it("stopsEditingOnEveryRegisteredDetailGrid", () =>
    {
        const registry = createDetailGridRegistry();
        const first    = { stopEditing: vi.fn() };
        const second   = { stopEditing: vi.fn() };

        registry.register("user-1",first);
        registry.register("user-2",second);
        registry.stopEditing();

        expect(first.stopEditing).toHaveBeenCalledTimes(1);
        expect(second.stopEditing).toHaveBeenCalledTimes(1);
    });

    it("forgetsUnregisteredDetailGrids", () =>
    {
        const registry = createDetailGridRegistry();
        const api      = { stopEditing: vi.fn() };

        registry.register("user-1",api);
        registry.register("user-1",null);
        registry.stopEditing();

        expect(api.stopEditing).not.toHaveBeenCalled();
    });

    it("keepsTheLatestApiForARowKey", () =>
    {
        const registry = createDetailGridRegistry();
        const stale    = { stopEditing: vi.fn() };
        const fresh    = { stopEditing: vi.fn() };

        registry.register("user-1",stale);
        registry.register("user-1",fresh);
        registry.stopEditing();

        expect(stale.stopEditing).not.toHaveBeenCalled();
        expect(fresh.stopEditing).toHaveBeenCalledTimes(1);
    });
});

//////////////////////////////////////////////////////////////////////////////
