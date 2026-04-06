# Security Policy

## Supported Versions

| Version | Supported          |
|---------|--------------------|
| 0.1.x   | :white_check_mark: |

## Reporting a Vulnerability

If you discover a security vulnerability in Anchor DI, please report it responsibly.

**Do not open a public GitHub issue for security vulnerabilities.**

Instead, please email **debdutsaha.dev@gmail.com** with:

1. A description of the vulnerability
2. Steps to reproduce
3. Affected versions
4. Any potential impact assessment

You can expect:

- **Acknowledgment** within 48 hours
- **Status update** within 7 days
- **Fix timeline** communicated once the issue is triaged

Once a fix is available, we will:

1. Release a patched version
2. Publish a security advisory via GitHub
3. Credit the reporter (unless anonymity is requested)

## Scope

Anchor DI is a compile-time dependency injection library. Security-relevant areas include:

- **Code generation (KSP):** Ensuring generated code does not introduce injection vectors
- **Runtime container:** Thread safety of singleton/scoped caches
- **ProGuard rules:** Ensuring consumer rules do not inadvertently expose internals

Thank you for helping keep Anchor DI safe.
