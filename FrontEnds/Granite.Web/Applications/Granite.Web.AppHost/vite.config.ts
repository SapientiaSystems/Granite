import { defineConfig } from "vite";
import react from "@vitejs/plugin-react-oxc";

export default defineConfig({
    plugins: [react()],
    resolve: {
        dedupe: [
            "@mantine/core",
            "@mantine/hooks",
            "react",
            "react-dom",
            "ag-grid-community",
            "ag-grid-react",
        ],
    },
    server: {
        proxy: Object.fromEntries(
            [
                "/frontendcontroller-user-service",
                "/frontendcontroller-admin-service",
                "/session-command-service",
                "/session-query-service",
                "/user-command-service",
                "/user-query-service",
            ].map((prefix: string) => [
                prefix,
                {
                    target:       process.env.GRANITE_GATEWAY_URL ?? "https://localhost:8181",
                    changeOrigin: true,
                    secure:       false,
                },
            ])),
    },
});
