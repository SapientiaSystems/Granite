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
        port: 5174,
        proxy: Object.fromEntries(
            [
                "/frontendcontroller-user-service",
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
