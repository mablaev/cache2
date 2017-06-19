import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class CacheMapImpl<K, V> implements CacheMap<K, V> {
    public static final long DEFAULT_TIME_TO_LIVE = 60_000;

    private long timeToLive = DEFAULT_TIME_TO_LIVE;

    private final Map<K, TimedValue<V>> storage = new HashMap<>();

    @Override
    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    @Override
    public long getTimeToLive() {
        return timeToLive;
    }

    @Override
    public V put(K key, V value) {
        TimedValue<V> newValue = new TimedValue<>(value, Clock.getTime());
        TimedValue<V> oldValue = storage.put(key, newValue);

        V result = null;

        if (actual(oldValue)) {
            result = oldValue.getValue();
        }

        return result;
    }

    @Override
    public void clearExpired() {
        storage.entrySet().removeIf(entry -> !actual(entry.getValue()));
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        V value = get(key);
        return value != null;
    }

    private boolean actual(TimedValue<V> timedValue) {
        if (timedValue != null) {
            long elapsedTime = Clock.getTime() - timedValue.getCreatedTime();
            return elapsedTime <= timeToLive;
        }

        return false;
    }

    @Override
    public boolean containsValue(Object value) {

        Iterator<Map.Entry<K, TimedValue<V>>> it = storage.entrySet().iterator();

        boolean result = false;

        while (it.hasNext()) {
            Map.Entry<K, TimedValue<V>> entry = it.next();

            TimedValue<V> tmValue = entry.getValue();

            if (value.equals(tmValue.getValue())) {

                if (actual(tmValue)) {
                    result = true;
                    break;
                } else {
                    it.remove();
                }
            }
        }

        return result;
    }

    @Override
    public V get(Object key) {
        TimedValue<V> timedValue = storage.get(key);

        V result = null;

        if (actual(timedValue)) {
            result = timedValue.getValue();
        } else if (timedValue != null) {
            storage.remove(key);
        }
        return result;
    }

    @Override
    public boolean isEmpty() {

        Iterator<Map.Entry<K, TimedValue<V>>> it = storage.entrySet().iterator();

        boolean result = true;

        while (it.hasNext()) {
            Map.Entry<K, TimedValue<V>> entry = it.next();

            TimedValue<V> tmValue = entry.getValue();

            if (actual(tmValue)) {
                result = false;
                break;
            } else {
                it.remove();
            }
        }

        return result;
    }

    @Override
    public V remove(Object key) {
        TimedValue<V> removedValue = storage.remove(key);
        return actual(removedValue) ? removedValue.getValue() : null;
    }

    @Override
    public int size() {
        return (int) storage.values().stream()
                .filter(this::actual)
                .count();
    }

    private static class TimedValue<V> {
        private final V value;
        private final long createdTime;

        public TimedValue(V value, long createdTime) {
            this.value = value;
            this.createdTime = createdTime;
        }

        public V getValue() {
            return value;
        }

        public long getCreatedTime() {
            return createdTime;
        }
    }
}
