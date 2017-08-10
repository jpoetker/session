package com.github.jpoetker.spike.session;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.query.SortQuery;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.types.RedisClientInfo;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration;

@Configuration
@EnableScheduling
public class CustomRedisHttpSessionConfiguration extends RedisHttpSessionConfiguration {
    final List<Object> whitelistedKeys = Arrays.asList("maxInactiveInterval", "creationTime", "lastAccessedTime", "sessionAttr:lastUpdatedDate", "sessionAttr:increment");

    public CustomRedisHttpSessionConfiguration() {
        super();
        super.setRedisNamespace("jpoetker");
    }

    @Bean
    @Override
    public RedisOperationsSessionRepository sessionRepository(@Qualifier("sessionRedisTemplate") RedisOperations<Object, Object> sessionRedisTemplate,
                                                              ApplicationEventPublisher applicationEventPublisher) {
        return super.sessionRepository(whitelistEntriesWrapper(sessionRedisTemplate), applicationEventPublisher);
    }


    private RedisOperations<Object, Object> whitelistEntriesWrapper(final RedisOperations<Object, Object> redisTemplate) {

        // Wraps the supplied redis operations object with an implementation that overrides then entries() method
        // to instead use the multiGet method to pull only white listed session attribute values

        // This approach doesn't change the bean when called above, just what is handed to the redis session repository
        return new RedisOperationsWrapper<Object, Object>(redisTemplate) {
            @Override
            public BoundHashOperations boundHashOps(Object key) {
                return new BoundHashOperationsWrapper<Object, Object, Object>(super.boundHashOps(key)) {
                    @Override
                    public Map<Object, Object> entries() {

                        List<Object> values = super.multiGet(whitelistedKeys);
                        Map<Object, Object> entries = new HashMap<>();
                        for(int i =0; i < whitelistedKeys.size(); i++) {
                            Object value = values.get(i);
                            if (value != null) {
                                entries.put(whitelistedKeys.get(i), value);
                            }
                        }
                        return entries;
                    }
                };
            }
        };
    }

    private static class RedisOperationsWrapper<K, V> implements RedisOperations<K, V> {
        private final RedisOperations<K, V> delegate;

        public RedisOperationsWrapper(RedisOperations<K, V> delegate) {
            this.delegate = delegate;
        }

        @Override
        public <T> T execute(RedisCallback<T> action) {
            return delegate.execute(action);
        }

        @Override
        public <T> T execute(SessionCallback<T> session) {
            return delegate.execute(session);
        }

        @Override
        public List<Object> executePipelined(RedisCallback<?> action) {
            return delegate.executePipelined(action);
        }

        @Override
        public List<Object> executePipelined(RedisCallback<?> action, RedisSerializer<?> resultSerializer) {
            return delegate.executePipelined(action, resultSerializer);
        }

        @Override
        public List<Object> executePipelined(SessionCallback<?> session) {
            return delegate.executePipelined(session);
        }

        @Override
        public List<Object> executePipelined(SessionCallback<?> session, RedisSerializer<?> resultSerializer) {
            return delegate.executePipelined(session, resultSerializer);
        }

        @Override
        public <T> T execute(RedisScript<T> script, List<K> keys, Object... args) {
            return delegate.execute(script, keys, args);
        }

        @Override
        public <T> T execute(RedisScript<T> script, RedisSerializer<?> argsSerializer,
                             RedisSerializer<T> resultSerializer, List<K> keys, Object... args) {
            return delegate.execute(script, argsSerializer, resultSerializer, keys, args);
        }

        @Override
        public <T extends Closeable> T executeWithStickyConnection(RedisCallback<T> callback) {
            return delegate.executeWithStickyConnection(callback);
        }

        @Override
        public Boolean hasKey(K key) {
            return delegate.hasKey(key);
        }

        @Override
        public void delete(K key) {
            delegate.delete(key);
        }

        @Override
        public void delete(Collection<K> keys) {
            delegate.delete(keys);
        }

        @Override
        public DataType type(K key) {
            return delegate.type(key);
        }

        @Override
        public Set<K> keys(K pattern) {
            return delegate.keys(pattern);
        }

        @Override
        public K randomKey() {
            return delegate.randomKey();
        }

        @Override
        public void rename(K oldKey, K newKey) {
            delegate.rename(oldKey, newKey);
        }

        @Override
        public Boolean renameIfAbsent(K oldKey, K newKey) {
            return delegate.renameIfAbsent(oldKey, newKey);
        }

        @Override
        public Boolean expire(K key, long timeout, TimeUnit unit) {
            return delegate.expire(key, timeout, unit);
        }

        @Override
        public Boolean expireAt(K key, Date date) {
            return delegate.expireAt(key, date);
        }

        @Override
        public Boolean persist(K key) {
            return delegate.persist(key);
        }

        @Override
        public Boolean move(K key, int dbIndex) {
            return delegate.move(key, dbIndex);
        }

        @Override
        public byte[] dump(K key) {
            return delegate.dump(key);
        }

        @Override
        public void restore(K key, byte[] value, long timeToLive, TimeUnit unit) {
            delegate.restore(key, value, timeToLive, unit);
        }

        @Override
        public Long getExpire(K key) {
            return delegate.getExpire(key);
        }

        @Override
        public Long getExpire(K key, TimeUnit timeUnit) {
            return delegate.getExpire(key, timeUnit);
        }

        @Override
        public List<V> sort(SortQuery<K> query) {
            return delegate.sort(query);
        }

        @Override
        public <T> List<T> sort(SortQuery<K> query, RedisSerializer<T> resultSerializer) {
            return delegate.sort(query, resultSerializer);
        }

        @Override
        public <T> List<T> sort(SortQuery<K> query, BulkMapper<T, V> bulkMapper) {
            return delegate.sort(query, bulkMapper);
        }

        @Override
        public <T, S> List<T> sort(SortQuery<K> query, BulkMapper<T, S> bulkMapper,
                                   RedisSerializer<S> resultSerializer) {
            return delegate.sort(query, bulkMapper, resultSerializer);
        }

        @Override
        public Long sort(SortQuery<K> query, K storeKey) {
            return delegate.sort(query, storeKey);
        }

        @Override
        public void watch(K key) {
            delegate.watch(key);
        }

        @Override
        public void watch(Collection<K> keys) {
            delegate.watch(keys);
        }

        @Override
        public void unwatch() {
            delegate.unwatch();
        }

        @Override
        public void multi() {
            delegate.multi();
        }

        @Override
        public void discard() {
            delegate.discard();
        }

        @Override
        public List<Object> exec() {
            return delegate.exec();
        }

        @Override
        public List<Object> exec(RedisSerializer<?> valueSerializer) {
            return delegate.exec(valueSerializer);
        }

        @Override
        public List<RedisClientInfo> getClientList() {
            return delegate.getClientList();
        }

        @Override
        public void killClient(String host, int port) {
            delegate.killClient(host, port);
        }

        @Override
        public void slaveOf(String host, int port) {
            delegate.slaveOf(host, port);
        }

        @Override
        public void slaveOfNoOne() {
            delegate.slaveOfNoOne();
        }

        @Override
        public void convertAndSend(String destination, Object message) {
            delegate.convertAndSend(destination, message);
        }

        @Override
        public ValueOperations<K, V> opsForValue() {
            return delegate.opsForValue();
        }

        @Override
        public BoundValueOperations<K, V> boundValueOps(K key) {
            return delegate.boundValueOps(key);
        }

        @Override
        public ListOperations<K, V> opsForList() {
            return delegate.opsForList();
        }

        @Override
        public BoundListOperations<K, V> boundListOps(K key) {
            return delegate.boundListOps(key);
        }

        @Override
        public SetOperations<K, V> opsForSet() {
            return delegate.opsForSet();
        }

        @Override
        public BoundSetOperations<K, V> boundSetOps(K key) {
            return delegate.boundSetOps(key);
        }

        @Override
        public ZSetOperations<K, V> opsForZSet() {
            return delegate.opsForZSet();
        }

        @Override
        public HyperLogLogOperations<K, V> opsForHyperLogLog() {
            return delegate.opsForHyperLogLog();
        }

        @Override
        public BoundZSetOperations<K, V> boundZSetOps(K key) {
            return delegate.boundZSetOps(key);
        }

        @Override
        public <HK, HV> HashOperations<K, HK, HV> opsForHash() {
            return delegate.opsForHash();
        }

        @Override
        public <HK, HV> BoundHashOperations<K, HK, HV> boundHashOps(K key) {
            return delegate.boundHashOps(key);
        }

        @Override
        public GeoOperations<K, V> opsForGeo() {
            return delegate.opsForGeo();
        }

        @Override
        public BoundGeoOperations<K, V> boundGeoOps(K key) {
            return delegate.boundGeoOps(key);
        }

        @Override
        public ClusterOperations<K, V> opsForCluster() {
            return delegate.opsForCluster();
        }

        @Override
        public RedisSerializer<?> getKeySerializer() {
            return delegate.getKeySerializer();
        }

        @Override
        public RedisSerializer<?> getValueSerializer() {
            return delegate.getValueSerializer();
        }

        @Override
        public RedisSerializer<?> getHashKeySerializer() {
            return delegate.getHashKeySerializer();
        }

        @Override
        public RedisSerializer<?> getHashValueSerializer() {
            return delegate.getHashValueSerializer();
        }
    }

    private static class BoundHashOperationsWrapper<H, HK, HV> implements BoundHashOperations<H, HK, HV> {
        private final BoundHashOperations<H, HK, HV> delegate;

        public BoundHashOperationsWrapper(BoundHashOperations<H, HK, HV> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Long delete(Object... keys) {
            return delegate.delete(keys);
        }

        @Override
        public Boolean hasKey(Object key) {
            return delegate.hasKey(key);
        }

        @Override
        public HV get(Object key) {
            return delegate.get(key);
        }

        @Override
        public List<HV> multiGet(Collection<HK> keys) {
            return delegate.multiGet(keys);
        }

        @Override
        public Long increment(HK key, long delta) {
            return delegate.increment(key, delta);
        }

        @Override
        public Double increment(HK key, double delta) {
            return delegate.increment(key, delta);
        }

        @Override
        public Set<HK> keys() {
            return delegate.keys();
        }

        @Override
        public Long size() {
            return delegate.size();
        }

        @Override
        public void putAll(Map<? extends HK, ? extends HV> m) {
            delegate.putAll(m);
        }

        @Override
        public void put(HK key, HV value) {
            delegate.put(key, value);
        }

        @Override
        public Boolean putIfAbsent(HK key, HV value) {
            return delegate.putIfAbsent(key, value);
        }

        @Override
        public List<HV> values() {
            return delegate.values();
        }

        @Override
        public Map<HK, HV> entries() {
            return delegate.entries();
        }

        @Override
        public Cursor<Map.Entry<HK, HV>> scan(ScanOptions options) {
            return delegate.scan(options);
        }

        @Override
        public RedisOperations<H, ?> getOperations() {
            return delegate.getOperations();
        }

        @Override
        public H getKey() {
            return delegate.getKey();
        }

        @Override
        public DataType getType() {
            return delegate.getType();
        }

        @Override
        public Long getExpire() {
            return delegate.getExpire();
        }

        @Override
        public Boolean expire(long timeout, TimeUnit unit) {
            return delegate.expire(timeout, unit);
        }

        @Override
        public Boolean expireAt(Date date) {
            return delegate.expireAt(date);
        }

        @Override
        public Boolean persist() {
            return delegate.persist();
        }

        @Override
        public void rename(H newKey) {
            delegate.rename(newKey);
        }
    }
}