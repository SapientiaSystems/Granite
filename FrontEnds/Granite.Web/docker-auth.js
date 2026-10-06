//////////////////////////////////////////////////////////////////////////////
// __System__.Web
//////////////////////////////////////////////////////////////////////////////

const { spawn } = require("child_process");

const target = process.argv[2];

function login(registry, user, password)
{
    return new Promise((resolve, reject) =>
    {
        const args = ["login", "-u", user, "--password-stdin"];

        if (registry)
        {
            args.push(registry);
        }

        const proc = spawn("docker", args, { stdio: ["pipe", "inherit", "inherit"] });

        proc.stdin.write(password);
        proc.stdin.end();
        proc.on("close", (code) =>
            code === 0
                ? resolve()
                : reject(new Error("docker login failed with exit code " + code)));
    });
}

async function main()
{
    if (target === "from")
    {
        const user     = process.env.DOCKER_USER;
        const password = process.env.DOCKER_PASSWORD;

        if (!user || !password)
        {
            console.log("DOCKER_USER/DOCKER_PASSWORD not set — skipping Docker Hub login.");
            return;
        }

        try
        {
            await login(null, user, password);
        }
        catch (error)
        {
            console.warn("Warning: Docker Hub login failed — proceeding without auth. " + error.message);
        }
    }
    else if (target === "to")
    {
        const token = process.env.GITLAB_TOKEN;

        if (!token)
        {
            console.error("Error: GITLAB_TOKEN environment variable is required.");
            process.exit(1);
        }

        await login("registry.gitlab.com", "oauth2", token);
    }
    else
    {
        console.error("Usage: node docker-auth.js <from|to>");
        process.exit(1);
    }
}

main().catch((error) =>
{
    console.error(error.message);
    process.exit(1);
});

//////////////////////////////////////////////////////////////////////////////
