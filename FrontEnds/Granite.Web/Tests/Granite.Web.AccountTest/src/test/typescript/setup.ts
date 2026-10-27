//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AccountTest
//////////////////////////////////////////////////////////////////////////////

import { afterEach } from "vitest";
import { cleanup }   from "@testing-library/react";

Object.defineProperty(window,"matchMedia",{
    writable: true,
    value:    (query: string) => ({
        matches:             false,
        media:               query,
        onchange:            null,
        addListener:         () => undefined,
        removeListener:      () => undefined,
        addEventListener:    () => undefined,
        removeEventListener: () => undefined,
        dispatchEvent:       () => false,
    }),
});

class NoopResizeObserver
{
    observe(): void {}
    unobserve(): void {}
    disconnect(): void {}
}

window.ResizeObserver = window.ResizeObserver ?? NoopResizeObserver;

class MemoryStorage
{
    private readonly items: Map<string,string> = new Map();

    getItem(key: string): string | null { return this.items.get(key) ?? null; }
    setItem(key: string,value: string): void { this.items.set(key,String(value)); }
    removeItem(key: string): void { this.items.delete(key); }
    clear(): void { this.items.clear(); }
}

Object.defineProperty(window,"localStorage",{ configurable: true, value: new MemoryStorage() });

afterEach(() =>
{
    cleanup();
    window.localStorage.clear();
    document.documentElement.removeAttribute("data-mantine-color-scheme");
});

//////////////////////////////////////////////////////////////////////////////
