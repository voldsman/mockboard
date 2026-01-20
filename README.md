<p align="center">

  <img src="/public/readme_logo.png" alt="MockBoard.dev Logo" width="150"/>

  <h1 align="center">MockBoard.dev</h1>

</p>


<p align="center">

  <strong>Self-Hosted API Mocking Without the Overhead</strong>

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

**MockBoard.dev** is a lightweight, self-hostable tool for mocking HTTP APIs during local development and testing.

I built it because I needed to mock REST responses for local development
without managing complex infrastructure or hand-coding endpoints with fake data.

It is designed to do one thing well: Fake REST API responses (**JSON in / JSON out**).

<p align="center">

  <img src="/public/screenshot_1.png" alt="screenshoot"/>
</p>

### 📖 How this came to be
This started as a SaaS idea I was working on with some folks using Elixir. 
When the team fell apart, I still needed the tool, so I rebuilt the parts that actually mattered to me using Java (Spring Boot) + Vue.

I use this daily. My friend uses it too and keeps telling me what's broken or missing, so I add features when I have time.

Figured I would put it out there in case it helps someone else who needs a simple, containerized mock server.

### 🏗 Architecture
Main goal: Keep it lightweight. Minimal dependencies. 

It's simple to use, but under the hood it's designed to handle a lot of traffic on cheap hardware.

- Java 21 Virtual Threads  - handles tons of concurrent requests
- Cache-first (Caffeine) - reads/writes hit the cache, not the database, so responses are fast
- Async persistence - changes get queued and written to H2 in the background
- Custom event queue - UI changes go through FIFO, mock executions get deduplicated to avoid spam

>You might ask - why such architecture for a tool?
> 
>This architecture works both for solo use when you deploy locally, but also allows working in small teams if needed, so you can host it as a web version.

### 🛠️ Usage
#### How to Mock an API
1. **Create a Board** → Get a unique URL: e.g., `http://localhost:8000/m/21gDw5rJ68BDCzZzjumF7XM9`
2. **Add Endpoints** → Define what paths you want to mock (e.g., `/users`, `/api/data`)
3. **Make Requests** → Append your endpoint to the board URL: `curl http://localhost:8000/m/21gDw5rJ68BDCzZzjumF7XM9/users`
4. **See Results** → Watch requests appear live in the UI

**Key Points**
- Board ID is in the URL path
- One unique board URL per browser session
- Supports all HTTP methods (GET, POST, PUT, DELETE, etc.)
- Use template variables for dynamic data: `{{user.email}}`, `{{system.uuid}}`

**Example**
Create endpoint `/api/users` with response:
```json
{
  "id": "{{system.uuid}}",
  "name": "{{user.fullName}}"
}
```
Call it: `curl http://localhost:8000/m/21gDw5rJ68BDCzZzjumF7XM9/api/users`

Get dynamic data back every time. Simple.

---
### ✨ Features

### Response Delay (up to 10 seconds by default)
Add artificial delays to mock responses for testing timeouts, loading states, and slow network conditions.

Useful for:
- Testing timeout handling
- Simulating slow APIs
- Debugging loading indicators
- Testing race conditions

Configure the delay (in milliseconds) per endpoint - it applies to every request.


### Dynamic Response Templates
When creating mock responses, you can use template variables inside `{{}}` to generate realistic fake data. Mockboard.dev uses [Datafaker](https://github.com/datafaker-net/datafaker) under the hood.

#### Example:
```json
{
  "id": "{{system.uuid}}",
  "user": {
    "name": "{{user.fullName}}",
    "email": "{{user.email}}",
    "phone": "{{user.phoneNumber}}"
  },
  "address": "{{address.full}}",
  "bio": "{{content.paragraph}}"
}
```

#### Available Template Variables:
| Category | Variable | Example Output |
|----------|----------|----------------|
| **Personal Data** | `{{user.fullName}}` | John Smith |
| | `{{user.firstName}}` | John |
| | `{{user.lastName}}` | Smith |
| | `{{user.email}}` | john.smith@example.com |
| | `{{user.username}}` | jsmith42 |
| | `{{user.phoneNumber}}` | +1-555-123-4567 |
| | `{{user.avatar}}` | https://robohash.org/abc123 |
| **Address** | `{{address.full}}` | 123 Main St, New York, NY 10001 |
| | `{{address.city}}` | New York |
| | `{{address.street}}` | 123 Main St |
| | `{{address.zipCode}}` | 10001 |
| | `{{address.country}}` | United States |
| | `{{address.countryCode}}` | US |
| **Content** | `{{content.char}}` | a |
| | `{{content.word}}` | lorem |
| | `{{content.sentence}}` | Lorem ipsum dolor sit amet. |
| | `{{content.paragraph}}` | Lorem ipsum dolor sit... |
| **System** | `{{system.int}}` | 42 |
| | `{{system.long}}` | 9223372036854775807 |
| | `{{system.double}}` | 3.141592653589793 |
| | `{{system.bool}}` | true |
| | `{{system.uuid}}` | 550e8400-e29b-41d4-a716-446655440000 |
**More variables coming soon!** Open an issue if you need specific data types.

### Live Request Monitoring (SSE)
Watch incoming requests in real-time via Server-Sent Events (SSE). Open a board in the UI and see requests appear instantly - no polling, no refresh.

**Limitations (configurable):**
- Max 1 SSE connection per board (keeps resource usage low)
- Latest connection wins if multiple users/tabs open the same board

Perfect for debugging, monitoring webhooks, or watching integration tests in action.

---
## 🚀 Hosting
### Self-hosted version
All you need to do 
```shell
git clone https://github.com/voldpix/mockboard.git
cd mockboard
docker compose up -d --build
```
**Note:** The first build might take some time, as it prioritizes offline dependencies. Follow-up launches will be quick.

#### Configuration
Most limits are configurable via [Constants.java](src/main/java/dev/mockboard/Constants.java). You can adjust them either through the docker compose file or by updating the class directly.

#### Note on UI customization:
The Vue frontend is currently built and bundled into Spring Boot (`src/main/resources/static`), which makes UI-specific changes a bit tricky. If you need to modify the UI:
> Make your changes in the Vue app
> 
> Build: `npm run build` (outputs to Spring Boot resources - see [vite.config.js](src/main/frontend/vite.config.js)
)
>
> Run `docker compose up -d --build`
<br><br>Note: I'm working on making this easier.

### Web version (mockboard.dev) - Coming soon
A hosted version with hard limits and rate limiting.  I'll be running it on a cheap server and have no desire to invest in infrastructure.

Main use case: you come in, create endpoints, test what you need, and leave. Hence, the idea of deleting saved data at 3 AM UTC. A simple tool that might come in handy when needed, and you'll know where to find it.

---
## 🤝 Contributing
Contributions are welcome, but please understand this is a **personal project** built to solve specific problems. It's not a product roadmap or feature request queue.

### Guidelines:
**Dependencies** - Adding new dependencies will likely result in a closed PR. The core principle is staying lightweight.

**Discuss first** - For anything beyond bug fixes, open an issue to discuss before writing code. This saves everyone time.

**Philosophy** - The goal is simple: mock APIs quickly without complexity. Features that drift from this will be declined.

### What's welcome:
- Bug fixes and performance improvements
- Documentation updates
- Small features that maintain simplicity

If you need features I won't add, fork it - that's what open source is for. No hard feelings.