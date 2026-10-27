# Strata.FrontEndController: shared `handleInput`/`getOutput` vs. typed per-feature operations

**Audience:** Strata.FrontEndController maintainers
**From:** Granite (a downstream application built on Strata.FrontEndController)
**Date:** 2026-10-26
**Status:** Analysis and recommendation for discussion

## Summary

Strata.FrontEndController exposes two shared endpoints, `handle-input` and `get-output`. A polymorphic hierarchy of `FrontEndInput`/`FrontEndOutput` subtypes, identified by Jackson `DEDUCTION`, extends them, and a `source->command->destination` key routes each request to an action. Granite recently moved login, logout and session status onto these endpoints as part of server-side sessions. Most of the defects and workarounds in that work came from the shared-endpoint design itself, not from the features.

This document compares the current design with an alternative: typed operations, one per feature. For example:

```
handleUsersInput(HandleUsersInputRequest):     HandleUsersInputReply
handleSessionInput(HandleSessionInputRequest): HandleSessionInputReply
```

**Recommendation:** move to typed per-feature operations, keeping the `source->command->destination` key inside each feature's request where the screen-flow model is useful. If that is too large a change, a smaller step addresses the worst problems: put the key in the URL path and replace `DEDUCTION` with an explicit type property.

## The two designs

| | Shared endpoints (current) | Typed per-feature operations (alternative) |
|---|---|---|
| Endpoints | `POST /handle-input`, `POST /get-output` for every screen | One endpoint per feature, e.g. `POST /handle-session-input`, `POST /handle-users-input` |
| Payload | Abstract `FrontEndInput`/`FrontEndOutput`, subtype inferred from the set of property names (`@JsonTypeInfo(use = DEDUCTION)`) | Concrete request and reply types per operation |
| Dispatch | Key string `source->command->destination` matched by an `IRequestProcessor` at runtime | Spring MVC maps each path to a controller method |
| Extension | New subtype, new action, new key, and registration of the subtype in an application Jackson module | New interface method, controller mapping, Java client method and TS client method |

## Comparison by area

| Area | Shared endpoints (current) | Typed operations (alternative) |
|---|---|---|
| **The contract** | Implicit. OpenAPI shows an abstract `FrontEndInput`, so callers must know the key strings and each subtype's property set. | Explicit. Each operation has concrete request and reply types and its own OpenAPI entry. |
| **Adding a feature** | Add a type, an action and a key, and register the type. The service interface, controllers and clients don't change. | Touch the interface, controller, Java client and TS client. More code, and more places to forget one. |
| **Telling payload types apart** | Inferred from property names. Every subtype needs a property no sibling has, so marker properties get added. An output whose identifying field is null can't be identified. The TS client must send every field, even empty ones. | Not needed: the endpoint fixes the type. Only polymorphism inside a feature needs an explicit type property. |
| **Gateway authorization** | Every input shares one path, so a gateway that needs per-action rules must buffer and parse request bodies. That adds a body-size limit and needs a defence against spoofed keys. Path-based rules alone send every `handle-input` to whatever rule catches the path. | Ordinary path rules work: login paths public, session paths for any logged-in user, user administration for admins. No body parsing. |
| **Choosing the handler** | At runtime, by string key; an unknown key fails with "No action found for key". The service takes one `IRequestProcessor`, so an application with several features must build its own routing processor. | Spring MVC chooses by path, so a missing handler shows up as a 404. A reflection test can check that every interface method has a mapping. |
| **Logs and metrics** | Every call is logged as `/handle-input` or `/get-output`. Finding which action ran means correlating logs across services. | Each operation has its own path in logs, metrics, rate limits and traces. |
| **Validation** | Generic. `validate(GetOutputRequest)` requires a non-null `userId` for every view, so public screens (such as a login page) send a placeholder. | Each operation validates what it needs. Public operations don't need a `userId`. |
| **What a reply contains** | `HandleInputReply` carries the original `input` back to the caller. Any sensitive input field, such as a password, is returned unless each application marks it write-only. | Each reply type is designed explicitly, so nothing comes back unless it's declared. |
| **Screen-flow model** | A uniform model: `source->command->destination` maps naturally onto screen navigation and state-machine style controllers. | No longer built into the protocol, but the key can still be carried inside a feature's request. |
| **Client** | One generic client (`getOutput`/`handleInput`) serves every screen, in Java and TS. | The clients grow with each feature, though they could be generated from OpenAPI. |
| **Versioning** | Hard. A new or renamed property can make two subtypes ambiguous and break deduction for both. | Easy. Version an operation by adding a new path or a new request type. |
| **Testing** | Needs deduction tests for every subtype set, key-routing tests, and gateway body-matcher tests. | Ordinary controller and client tests, plus a mapping-completeness test. |
| **Fit with Strata** | Strata's native design; applications need no framework changes. | A framework change, or more application-side code if applications add typed endpoints themselves. |

## Pros and cons

| Design | Pros | Cons |
|---|---|---|
| **Shared endpoints** | • A new screen needs no interface, controller or client change<br>• One generic client in Java and TS<br>• A uniform screen-flow model<br>• Small API surface | • Implicit contract and a weak OpenAPI spec<br>• Fragile type deduction: marker properties, unidentifiable null outputs, every field required<br>• Per-action authorization at a gateway needs body parsing (size limits, spoofing defence, fragile rule order)<br>• A missing gateway rule silently falls to the catch-all<br>• Opaque logs and metrics<br>• `HandleInputReply` echoes the input, so sensitive fields can leak<br>• Generic validation forces placeholder values on public screens |
| **Typed operations** | • Explicit, self-documenting contract<br>• No type deduction<br>• Plain path-based authorization at a gateway; nothing needs to read bodies<br>• Per-operation logs, metrics and rate limits<br>• Replies designed for their purpose, with targeted validation<br>• Easy versioning<br>• Matches how Strata request-reply services such as User and Session are already built | • Each feature touches the interface, controller and both clients, so there's more code and more room for omissions<br>• The clients grow over time<br>• Requires a framework change<br>• Screen-flow keys are no longer central to the protocol |

## Evidence from Granite

Each of these happened during Granite's server-side sessions work, between 2026-10-22 and 2026-10-24.

| What happened | Root cause in the shared-endpoint design | Granite's workaround |
|---|---|---|
| A user with the auditor role couldn't log out (the request was denied at the gateway) | Logout became a `handle-input`. The gateway's path-based role rules couldn't tell it apart from other inputs, so it fell to the admin-only catch-all. | A gateway matcher that parses the body to find the `Session.Current->Logout->LoginView` key |
| Public login inputs needed gateway rules | Public and protected inputs share one path. | Body-reading public-input matcher, a 1 MiB body limit, and rejection of keys containing `->` inside a field |
| `LogoutInput` couldn't be `{}` | Deduction needs a property no sibling has. | A marker property `logout: true` |
| A refused Okta start couldn't return `RedirectOutput` with a null `url` | Deduction can't identify an output whose only identifying field is null. | Returned a different subtype (`SessionOutput` with status `UNAUTHENTICATED`) |
| The login password would have been returned to the browser | `HandleInputReply` carries the original `input`. | `LoginInput.password` made write-only for Jackson |
| The public login page had to send `userId: 0` | `validate(GetOutputRequest)` requires a non-null `userId`. | A placeholder `userId` in the SPA |
| An application with two features needed custom routing | `FrontEndControllerService` takes a single `IRequestProcessor`. | A prefix-routing processor in front of the feature processors |
| Diagnosing failures meant correlating logs across services | Every action is logged under the same two paths. | None; done by hand |

A typed design has its own failure mode. Granite's User service, which already uses typed operations, was missing its `destroy-users` controller mapping, and deleting users returned 404 until it was found. A reflection test that checks every interface method has a mapping now guards against it. Typed operations need that kind of test.

## Options

| Option | What changes | Fixes | Leaves |
|---|---|---|---|
| **A. Keep as is** | Nothing | — | Every problem above, repeated for each new application and screen |
| **B. Key in the URL, explicit type ids** | `POST /handle-input/{source}/{command}/{destination}` (and the same for `get-output`); `@JsonTypeInfo(use = NAME, property = "type")` in place of `DEDUCTION`; userId validation only where an action requires it | Gateway path authorization, deduction fragility, placeholder userIds, opaque logs | The implicit OpenAPI contract and the echoed input in `HandleInputReply` |
| **C. Typed per-feature operations** (recommended) | Applications define one operation per feature with concrete request and reply types. Strata could provide a base controller, client and processor for a feature so the wiring stays small, and keep the `source->command->destination` key inside the feature request. | All of the above, plus an explicit contract and purpose-built replies | More code per feature; needs a mapping-completeness test |

## Recommendation

1. **Adopt option C as the target design.** Typed per-feature operations remove whole categories of problems at the framework level instead of leaving each application to work around them. Strata can keep the extra code per feature small by providing generic building blocks: an abstract feature controller, a client, and a per-feature request processor keyed by `command->destination`.
2. **If option C is too large for now, do option B first.** It is mostly backward compatible at the routing level, and it fixes gateway authorization and type deduction, which are the most expensive problems in practice.
3. **Whichever option is chosen, stop echoing `input` in `HandleInputReply`,** or make it opt-in. Any application with a sensitive input field is exposed by default.
4. **With either B or C, ship a mapping-completeness test** pattern (every interface method has a controller mapping and a client method) as part of the Strata test fixtures.

## References

- **Strata source** (`StrataFrameworkSet/Strata.FrontEndController`, commit `eb66617`):
  - `Strata.FrontEndController.Service/.../requestreply/FrontEndInput.java` and `FrontEndOutput.java`: `@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)`
  - `Strata.FrontEndController.Server/.../application/FrontEndControllerService.java`: a single `IRequestProcessor`, and `validate(GetOutputRequest)` requiring `userId`
  - `HandleInputReply`: carries `input` alongside `output`
- **Granite:**
  - `Microservices/Granite.Gateway`: `PublicFrontEndInputMatcher`, `CurrentSessionFrontEndInputMatcher`, `FrontEndInputKeyWebFilter`
  - `Microservices/Granite.FrontEndController`: `FrontEndJacksonModule`, `PrefixRoutingRequestProcessor`, the session input/output types
  - `docs/superpowers/plans/2026-10-23-granite-server-side-sessions.md`: the server-side sessions roadmap
