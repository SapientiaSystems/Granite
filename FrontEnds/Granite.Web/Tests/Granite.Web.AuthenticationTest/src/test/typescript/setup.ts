//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AuthenticationTest
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

afterEach(() =>
{
    cleanup();
    window.sessionStorage.clear();
});

//////////////////////////////////////////////////////////////////////////////
