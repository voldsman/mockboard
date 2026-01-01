# MockBoard (v2 - OSS Rewrite)

**Current Status:** Active Development / Refactoring (Target Release: Q1 2026)

## Overview
MockBoard is a high-performance, self-hosted API mocking platform.

The project initially began as a SaaS prototype built with Elixir to leverage the BEAM VM for concurrency. 

However, this repository represents the v2 architecture, a complete strategic rewrite in Spring Boot 4 & Java 21.

This migration was chosen to provide a lightweight, containerized solution that is easier for the wider developer community to adopt, extend, and run locally or in CI/CD pipelines without niche runtime dependencies.

## Key Features
- **Hybrid Performance:** Combines Caffeine lookups with MongoDB for persistence
- **Smart Routing:** High-speed in-memory engine supporting wildcard path matching (e.g., `/api/users/*/profile`)
- **Built-in Protection:** IP-based throttling for board creation and token-based rate limiting for API execution
- **Disposable by Design:** Automated background schedulers hard-delete stale data daily to keep the instance lightweight

## Roadmap (Q1 2026)
- [x] Core Mocking Engine (Spring Boot 4 & Java 21)
- [x] MongoDB Persistence & Caffeine Caching layers
- [x] Rate Limiting & Security (Owner Tokens)
- [x] Automated Data Cleanup
- [ ] Real-time request capture via SSE
- [ ] Allow to import/export mock rules
- [ ] Allow to use json body templates for data `faking` (e.g., `{{user.fullName}}`)
- [ ] Frontend UI: A lightweight Vue.js dashboard
- [ ] Allow to create a mock rule from the received webhook
- [ ] Docker Compose for easy self-hosting
