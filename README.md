<p align="center">
  <img src="/src/main/frontend/public/logo.png" alt="MockBoard.dev Logo" width="150"/>
  <h1 align="center">MockBoard.dev</h1>
</p>

<p align="center">
  <strong>The Ephemeral, Self-Hosted API Mocking Tool</strong>
  <br>
  Built on Java 21 & Spring Boot 4
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring_Boot-4.0-brightgreen" alt="Spring Boot 4">
  <img src="https://img.shields.io/badge/Status-Alpha-red" alt="Status">
  <img src="https://img.shields.io/badge/License-MIT-blue" alt="License">
</p>

---

## ⚡ What is MockBoard.dev?

**MockBoard.dev** is a lightweight, high-performance tool for developers who need disposable mock APIs instantly.

It is designed for rapid prototyping, CI/CD pipelines, and local development where you need a fake backend *now*, but don't want to manage long-term infrastructure.

### 🕒 The "Cinderella" Feature
**MockBoard.dev is ephemeral by design.**
Every day at **3:00 AM UTC**, the server performs a hard reset. All mocks, data, and logs are wiped clean.
* **No maintenance:** You never need to clean up old test data.
* **Privacy-first:** Data exists only as long as you need it today.
* **Lightweight:** The database never grows out of control.

---

## 📖 The Story: From SaaS to Open Source

The original project started in 2025 as a collaborative initiative among a group of enthusiasts building a SaaS prototype in Elixir.

As external priorities shifted and the original team dispersed, the full SaaS version was put on hold. However, seeing value in the core utility, I decided to simplify the scope and rewrite the project as a lightweight, open-source tiil. I am currently continuing development independently, focusing on a self-hostable Java version.

**The V2 Rewrite:**
The current repository represents a complete rewrite using Java 21 and Spring Boot 4.
* **Why the switch?** To provide a containerized solution that is easier for the wider developer community to adopt, extend, and self-host without niche runtime dependencies.
* **The Result:** A strictly typed, high-performance system that leverages modern JVM advancements to match the concurrency levels of our original Elixir prototype.

---

## 🏗 Architecture & Tech Stack

This isn't just a CRUD app. MockBoard.dev utilizes a custom Event-Driven and Cache-First architecture (write-back pattern) to ensure sub-millisecond response times.

* **Core:** Java 21 + Spring Boot 4
* **Concurrency:** 100% Virtual Threads for handling high-volume HTTP and SSE connections.
* **Read Strategy (Cache-First):** All read operations are served directly from Caffeine.
* **Write Strategy (Async):**
    * Incoming writes are pushed to a Custom In-Memory Event Queue.
    * The queue asynchronously flushes data to persistence, decoupling ingestion speed from disk I/O.
* **Persistence:** H2 Database running in Server Mode.
* **Real-Time:** Server-Sent Events (SSE) for streaming request logs to the UI.
* **Routing:** Custom-built path matching engine (supporting wildcards like `/api/users/*/profile/*`).

---

## 🚀 Key Features

* **Smart Routing:** Define mocks with dynamic path parameters.
* **Real-Time Inspector:** Watch requests hit your mock endpoints live via the SSE dashboard.
* **Disposable Infrastructure:** Automated OS level background schedulers handle the daily 3:00 AM UTC wipe (database file removal & service restart).
* **Rate Limiting:** (Coming Soon) Built-in app-level protection against abuse.
* **JSON Templating:** (Coming Soon) Use `{{user.fullName}}` or `{{internet.email}}` to generate dynamic fake data.
* **Self-Hostable:** (Coming Soon) Distributed via Docker Compose.

---

## 🗺 Roadmap (Q1 2026)

Status: In active development in my free time. The public beta is scheduled for mid/end Q1 2026.

- [x] **Core Engine:** Spring Boot 4 + Java 21 setup
- [x] **Persistence:** H2 Server Mode + Custom Event Queue
- [x] **Routing:** Custom wildcard path matcher
- [X] **Real-time:** SSE Request Capture implementation
- [X] **UI:** Lightweight Vue.js Dashboard
- [ ] **Import/Export:** Support for Mock Rules sharing
- [ ] **Rate limiting:** App-level rate limiting by IP/apiKey 
- [ ] **Data Faking:** JSON Body Templates
- [ ] **Integration:** "Create Mock from Webhook" feature
- [ ] **Distribution:** Self hostable `docker-compose.yml`
- [ ] **Board sharing:** Allow to share board UI access

---
## 🤝 Contributing
Contributions are welcome; if you are interested, feel free to open a PR.
