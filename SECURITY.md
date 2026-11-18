# Security Policy

## Supported version

Security fixes are made against the latest code on the main branch. AURELION is experimental and does not currently promise long-term support for older APK versions.

## Reporting a vulnerability

Please do not publish an exploitable vulnerability in a public issue.

Use GitHub's private vulnerability reporting / Security Advisories for this repository when available. Include:
- A concise description of the issue and its impact.
- Affected Android versions and AURELION version/commit.
- Reproduction steps or a proof of concept that does not expose real user data.
- Any suggested mitigation.

Do not include passwords, signing keys, personal data, or private device identifiers in reports.

## Project security practices

- Release signing keys and passwords must stay outside Git and be stored as repository Actions secrets only when needed for CI.
- CI actions are pinned to immutable commits.
- The app currently has no Internet permission or network service.
- Dependency updates are monitored through Dependabot.
- Permission changes, new dependencies, and network-connected features require a privacy/security review.
