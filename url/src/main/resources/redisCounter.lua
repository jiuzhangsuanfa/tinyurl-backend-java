local nextId
nextId = redis.call('incr',KEYS[1])
return nextId