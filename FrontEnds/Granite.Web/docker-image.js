//////////////////////////////////////////////////////////////////////////////
// Granite.Web
//////////////////////////////////////////////////////////////////////////////

const { execFileSync } = require("child_process");
const fs = require("fs");
const path = require("path");

const REPOSITORY = "ghcr.io/johnliebenau/granite/";
const TAG_VARIABLE = "BUILD_IMAGE_TAG";
const USAGE = "Usage: node docker-image.js <build|push> <image> [context]";

function fail(message)
{
    console.error("docker-image: " + message);
    process.exit(1);
}

function readVersion()
{
    const file = path.resolve(__dirname, "../../gradle/granite-versions.gradle");
    const text = fs.readFileSync(file, "utf8");
    const value = (name) =>
    {
        const match = new RegExp("^\\s*" + name + "\\s*=\\s*\"([^\"]*)\"", "m").exec(text);

        if (!match)
        {
            fail("cannot read " + name + " from " + file);
        }

        return match[1];
    };

    return value("major") + value("minor") + value("patch");
}

function escapeRegExp(text)
{
    return text.replace(/[.*+?^${}()|[\]\\-]/g, "\\$&");
}

function docker(args)
{
    if (process.env.DOCKER_IMAGE_DRY_RUN === "1")
    {
        console.log("docker " + args.join(" "));
        return;
    }

    execFileSync("docker", args, { stdio: "inherit" });
}

function main()
{
    const [action, image, context] = process.argv.slice(2);

    if (!["build", "push"].includes(action) || !image || (action === "build" && !context))
    {
        fail(USAGE);
    }

    const tag = (process.env[TAG_VARIABLE] || "").trim();

    if (tag === "")
    {
        console.warn(action + " " + image + " skipped: " + TAG_VARIABLE + " is not set");
        return;
    }

    const version = readVersion();

    if (!new RegExp("^" + escapeRegExp(version) + "-[0-9]{14}-(local|dev)$").test(tag))
    {
        fail(TAG_VARIABLE + " '" + tag + "' is invalid; expected format " + version + "-<yyyyMMddHHmmss>-<local|dev>");
    }

    const name = REPOSITORY + image + ":" + tag;

    if (action === "build")
    {
        const labels = {
            "org.opencontainers.image.version": version,
            "org.opencontainers.image.revision": process.env.BUILD_GIT_SHA,
            "org.opencontainers.image.created": process.env.BUILD_CREATED,
            "granite.build.user": process.env.BUILD_USER,
            "granite.image.tag": tag
        };
        const args = ["build", "-t", name];

        for (const [key, value] of Object.entries(labels))
        {
            if (value && value.trim() !== "")
            {
                args.push("--label", key + "=" + value.trim());
            }
        }

        args.push(context);
        docker(args);
    }
    else
    {
        docker(["push", name]);
    }
}

main();

//////////////////////////////////////////////////////////////////////////////
