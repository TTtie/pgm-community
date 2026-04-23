# Contributing

We're excited to have contributors working on Community. Before diving into implementation details, it helps to align on what this project is trying to be and how changes should fit into it.

## Scope

Community is a companion to [PGM](https://github.com/PGMDev/PGM), focused on community management and features that support PGM servers without needing to live in PGM itself. That includes moderation, assistance, social features, polls, map parties, utility commands, and network-aware quality-of-life tooling.

Community should complement PGM rather than replace it. Gameplay rules, match flow, and core PvP mechanics belong in PGM. Community can integrate with PGM where that improves the player or staff experience, but new features should still make sense as community features first.

## Philosophy

1. **Simplicity**

   Favor code that is easy to read, easy to maintain, and easy to configure. Avoid unnecessary abstractions, avoid adding dependencies without a strong reason, and prefer straightforward solutions to clever ones.

   Community is a large plugin with many features behind configuration flags. Keep new work predictable for server owners and avoid surprising interactions between unrelated systems.

2. **Collaboration**

   Share ideas early. If you are planning a larger change, open a discussion or draft pull request before polishing the implementation. Early feedback on design is usually more valuable than late feedback on code style.

   Smaller pull requests are easier to review and safer to merge. If a change can be split into incremental pieces, do that.

3. **Practical Integration**

   Community should work well on its own, and it should also integrate cleanly with the rest of the PGM ecosystem. Some features can optionally use PGM, the Database plugin, Environment, or Redis-backed network messaging, but contributions should preserve sensible local defaults and graceful behavior when optional integrations are unavailable.

   In practice, this means:
   - Keep optional integrations optional.
   - Prefer configuration over hard requirements.
   - Do not assume every server is running the full network stack.

## Development Setup

Community uses Gradle and Java 21.

- [Java 21](https://docs.oracle.com/en/java/javase/21/install/overview-jdk-installation.html) - required to build and run the plugin
- [Gradle](https://gradle.org/install/) - optional if you use the included wrapper, but useful to have installed locally

The repository is a multi-module Gradle project:

- `core` contains most plugin logic
- `util` contains shared platform abstractions
- `platform/platform-modern` and `platform/platform-sportpaper` contain platform-specific implementations

## Workflow

1. Clone the repository.

```bash
git clone git@github.com:PGMDev/Community.git
cd Community
```

2. Make your changes.

If the change is non-trivial, consider opening a draft pull request before finishing the implementation.

3. Run the formatter.

Community uses [Spotless](https://github.com/diffplug/spotless) with [palantir-java-format](https://github.com/palantir/palantir-java-format) in Google style.

```bash
./gradlew spotlessApply
```

4. Build the plugin.

```bash
./gradlew build
```

This produces the main plugin jar at `build/libs/Community.jar`.

5. Test your changes.

⚠️ **Please test your changes before opening a pull request.** ⚠️

Do not assume a successful compile is enough. At minimum, make sure the project builds cleanly and that every affected feature still behaves correctly in-game.

If your change touches optional integrations such as PGM hooks, database persistence, or network messaging, test both the integrated path and the fallback behavior. If your change affects commands, menus, permissions, configuration loading, or cross-server behavior, verify those paths explicitly instead of relying on code inspection alone.

Contributors are expected to test their work **before** requesting review.

6. Commit your work.

Commit your changes, using the `-S` and `-s` tag to [sign](https://help.github.com/en/github/authenticating-to-github/signing-commits) and [certify](https://developercertificate.org) the origin of your code.

```bash
git commit -S -s -m "Add concise description of the change"
```

7. Open a pull request for review and feedback.

## Pull Request Guidance

- Keep pull requests focused on one change.
- Prefer incremental changes to large rewrites.
- Explain the problem being solved, not just the code that changed.
- Include config or migration notes when behavior changes.
- Mention any integration assumptions, especially around PGM, Redis, or the Database plugin.

## Coding Notes

- Use Java 21 language features where they improve clarity, but do not force newer patterns where simple code is better.
- Respect existing formatting and project conventions.
- Avoid introducing new libraries unless the benefit is clear and discussed first.
- Default to feature flags and safe config-driven behavior for anything that affects server operations.
- Preserve compatibility with optional dependencies and soft-depend behavior declared in `plugin.yml`.

## Questions

If you are unsure whether a change belongs in Community or in PGM, ask before building it. That usually saves time for both contributors and reviewers.
