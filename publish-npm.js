#!/usr/bin/env node
'use strict';

const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const { spawnSync } = require('child_process');

const USAGE = 'usage: node publish-npm.js [--dry-run] [--registry <url>]';
const SKIP_DIRECTORIES = new Set(['node_modules', '.git', '.gradle', '.idea']);

function fail(message) {
    console.error('publish-npm: ' + message);
    process.exit(1);
}

function parseArguments(argv) {
    const options = { dryRun: false, registry: 'https://registry.npmjs.org/' };
    for (let i = 0; i < argv.length; i++) {
        const arg = argv[i];
        if (arg === '--dry-run') options.dryRun = true;
        else if (arg === '--registry') options.registry = argv[++i];
        else fail(USAGE);
    }
    if (!options.registry) fail(USAGE);
    if (!options.registry.endsWith('/')) options.registry += '/';
    return options;
}

function findTarballs(directory, found) {
    for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
        const full = path.join(directory, entry.name);
        if (entry.isDirectory()) {
            if (!SKIP_DIRECTORIES.has(entry.name)) findTarballs(full, found);
        } else if (entry.name.endsWith('.tgz') && path.basename(directory) === 'dist' &&
                   path.basename(path.dirname(directory)) === '.build') {
            found.push(full);
        }
    }
    return found;
}

function readManifest(tarball) {
    const data = zlib.gunzipSync(fs.readFileSync(tarball));
    let offset = 0;
    while (offset + 512 <= data.length) {
        const header = data.subarray(offset, offset + 512);
        if (header.every(byte => byte === 0)) break;
        const field = (start, length) => header.subarray(start, start + length).toString('utf8').replace(/\0.*$/s, '');
        const prefix = field(345, 155);
        const name = (prefix ? prefix + '/' : '') + field(0, 100);
        const size = parseInt(field(124, 12).trim() || '0', 8);
        if (name === 'package/package.json') {
            return JSON.parse(data.subarray(offset + 512, offset + 512 + size).toString('utf8'));
        }
        offset += 512 + Math.ceil(size / 512) * 512;
    }
    fail(tarball + ' has no package/package.json');
}

function orderByDependencies(packages) {
    const byName = new Map(packages.map(entry => [entry.name, entry]));
    const ordered = [];
    const state = new Map();
    const visit = (entry, trail) => {
        if (state.get(entry.name) === 'done') return;
        if (state.get(entry.name) === 'visiting') fail('dependency cycle: ' + trail.concat(entry.name).join(' -> '));
        state.set(entry.name, 'visiting');
        const manifest = entry.manifest;
        const dependencies = Object.keys(Object.assign({}, manifest.peerDependencies, manifest.dependencies)).sort();
        for (const dependency of dependencies) {
            if (byName.has(dependency)) visit(byName.get(dependency), trail.concat(entry.name));
        }
        state.set(entry.name, 'done');
        ordered.push(entry);
    };
    for (const entry of [...packages].sort((a, b) => a.name.localeCompare(b.name))) visit(entry, []);
    return ordered;
}

async function registryState(registry, name, version, tag) {
    const url = registry + name.replace('/', '%2f') + '?cb=' + Date.now();
    const response = await fetch(url, { headers: { accept: 'application/json', 'cache-control': 'no-cache' } });
    if (response.status === 404) return { published: false, current: null };
    if (!response.ok) throw new Error(url + ' returned HTTP ' + response.status);
    const document = await response.json();
    return {
        published: Boolean((document.versions || {})[version]),
        current: (document['dist-tags'] || {})[tag] || null
    };
}

function stampOf(version) {
    const match = /-snapshot\.(\d+)$/.exec(version || '');
    return match ? BigInt(match[1]) : null;
}

async function main() {
    const options = parseArguments(process.argv.slice(2));
    const root = __dirname;
    const packages = findTarballs(root, []).map(tarball => {
        const manifest = readManifest(tarball);
        const match = /^(\d+\.\d+\.\d+)-snapshot\.\d+$/.exec(manifest.version || '');
        if (!manifest.name || !match) fail(path.relative(root, tarball) + ' does not hold a <x.y.z>-snapshot.<timestamp> version');
        return { name: manifest.name, version: manifest.version, tag: 'snapshot-' + match[1], tarball, manifest };
    });
    if (packages.length === 0) fail('no .build/dist/*.tgz found under ' + root);

    const seen = new Map();
    for (const entry of packages) {
        if (seen.has(entry.name)) {
            fail(entry.name + ' has several tarballs: ' + path.relative(root, seen.get(entry.name).tarball) + ', ' + path.relative(root, entry.tarball));
        }
        seen.set(entry.name, entry);
    }

    const ordered = orderByDependencies(packages);
    console.log('publish-npm: ' + ordered.length + ' packages to ' + options.registry + (options.dryRun ? ' (dry run)' : ''));
    let published = 0;
    let skipped = 0;
    for (const entry of ordered) {
        const relative = path.relative(root, entry.tarball);
        const args = ['publish', entry.tarball, '--tag', entry.tag, '--registry', options.registry];
        const state = await registryState(options.registry, entry.name, entry.version, entry.tag);
        if (state.published) {
            console.log('publish-npm: skip ' + entry.name + '@' + entry.version + ' (already published)');
            skipped++;
            continue;
        }
        const currentStamp = stampOf(state.current);
        if (currentStamp !== null && currentStamp > stampOf(entry.version)) {
            fail(relative + ' (' + entry.version + ') is older than ' + entry.name + '@' + entry.tag + ' = ' + state.current + '; rebuild before publishing');
        }
        if (options.dryRun) {
            console.log('publish-npm: [dry-run] npm publish ' + relative + ' --tag ' + entry.tag + '  (' + entry.name + '@' + entry.version + ')');
            continue;
        }
        console.log('publish-npm: publishing ' + entry.name + '@' + entry.version + ' as ' + entry.tag);
        const result = spawnSync('npm', args, { stdio: 'inherit', shell: process.platform === 'win32' });
        if (result.status !== 0) fail('npm publish failed for ' + entry.name + '@' + entry.version);
        published++;
    }
    console.log('publish-npm: published ' + published + ', skipped ' + skipped + ', total ' + ordered.length);
}

main().catch(error => fail(error.stack || String(error)));
