local key = KEYS[1]

local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local current_time = tonumber(ARGV[3])

local tokens = tonumber(redis.call("HGET", key, "tokens"))
local last_refill = tonumber(redis.call("HGET", key, "last_refill"))

if tokens == nil then
    tokens = capacity
    last_refill = current_time
end

local delta = math.max(0, current_time - last_refill)
local refill = delta * refill_rate

tokens = math.min(capacity, tokens + refill)

if tokens > 0 then
    tokens = tokens - 1

    redis.call("HSET", key, "tokens", tokens)
    redis.call("HSET", key, "last_refill", current_time)

    redis.call("EXPIRE", key, 120)

    return 1
else
    return 0
end
