# Rate Limiter

A distributed rate-limiting service built with **Java 21**, **Spring Boot 4**, and **Redis**, implementing the **Token Bucket algorithm** across three progressively sophisticated approaches -- from in-memory to atomic Redis Lua scripts.

## Why This Project

Rate limiting is a critical component in production APIs, protecting services from abuse and ensuring fair resource allocation. This project explores the real-world tradeoffs between simplicity, scalability, and correctness by building the same algorithm three different ways.

## Architecture

```
Client Request
      |
      v
  [Spring Interceptor] ── extracts X-USER-ID header
      |
      v
  [Rate Limiter Service] ── checks token bucket
      |         |
   allowed    denied
      |         |
      v         v
  Controller   429 Too Many Requests
```

**Per-user isolation** -- each user gets their own token bucket (10 tokens, refills at 1/sec), identified via the `X-USER-ID` HTTP header.

## Implementations

### 1. In-Memory Token Bucket
> `RateLimiterService.java`

- Uses `ConcurrentHashMap` with synchronized access
- Zero external dependencies -- good for single-instance deployments
- Tradeoff: state is lost on restart and not shared across instances

### 2. Redis-Based Token Bucket
> `RedisRateLimiterService.java`

- Stores bucket state as Redis hashes (`rate_limit:{userId}`)
- Enables distributed rate limiting across multiple application instances
- Auto-expiring keys (TTL: 120s) for cleanup
- Tradeoff: multiple Redis round-trips per request create a small race condition window

### 3. Redis + Lua Script (Atomic)
> `RedisLuaRateLimiterService.java` + `tokenBucket.lua`

- Entire token bucket logic executes atomically inside Redis via a Lua script
- Eliminates TOCTOU race conditions from the multi-step Redis approach
- Single network round-trip per request
- **Production-grade approach** -- this is how services like Stripe and Cloudflare handle rate limiting at scale

## Tech Stack

| Layer         | Technology                  |
|---------------|-----------------------------|
| Language      | Java 21                     |
| Framework     | Spring Boot 4.0             |
| Cache/Store   | Redis (Spring Data Redis)   |
| Build Tool    | Gradle 9.3                  |
| Scripting     | Lua (Redis server-side)     |

## Project Structure

```
rate-limiter/src/main/java/com/self/rate/limiter/
├── controller/
│   └── Controller.java               # REST endpoint (GET /api/resource)
├── interceptor/
│   └── RateLimitInterceptor.java      # Pre-request rate limit check
├── ratelimiter/
│   └── TokenBucket.java               # Core token bucket algorithm (POJO)
├── service/
│   ├── RateLimiterService.java        # In-memory implementation
│   ├── RedisRateLimiterService.java   # Redis hash implementation
│   └── RedisLuaRateLimiterService.java# Redis Lua (atomic) implementation
├── config/
│   ├── RedisConfig.java               # Redis template bean
│   ├── RedisLuaConfig.java            # Lua script loader
│   └── WebConfig.java                 # Interceptor registration
└── resources/
    └── scripts/
        └── tokenBucket.lua            # Atomic rate-limit logic
```

## Getting Started

**Prerequisites:** Java 21+, Redis running on `localhost:6379`

```bash
cd rate-limiter
./gradlew bootRun
```

**Test it:**

```bash
# Should return 200 "Request allowed"
curl -H "X-USER-ID: user1" http://localhost:8080/api/resource

# After 10 rapid requests -- returns 429 Too Many Requests
for i in {1..12}; do
  curl -s -o /dev/null -w "%{http_code}\n" -H "X-USER-ID: user1" http://localhost:8080/api/resource
done
```

## Key Design Decisions

- **Interceptor pattern** over annotation-based limiting -- keeps controllers clean and rate limiting transparent
- **Header-based identity** (`X-USER-ID`) -- simulates API key / auth-token based identification used in production API gateways
- **Progressive complexity** -- each implementation builds on the lessons of the previous one, demonstrating why atomic operations matter in distributed systems

## What I Learned

- The gap between "works on one machine" and "works in a distributed system" is where most production bugs live
- Redis Lua scripts are the sweet spot for atomic multi-step operations that need to stay close to the data
- Token bucket is preferred over fixed-window counters because it handles bursty traffic more gracefully while still enforcing long-term rate limits
