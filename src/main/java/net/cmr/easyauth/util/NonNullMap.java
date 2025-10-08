package net.cmr.easyauth.util;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.lang.NonNull;
import org.springframework.lang.NonNullFields;
import org.springframework.lang.Nullable;

public class NonNullMap<K, V> implements Map<K, V> {
    
    private Map<K, V> internalMap;

    public NonNullMap(Map<K, V> map) {
        this.internalMap = map;
    }

    @Override
    public void clear() {
        internalMap.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return internalMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return internalMap.containsValue(value);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return internalMap.entrySet();
    }

    
    /**
     * Functions similarly to {@link Map#get(Object)}, except no value exists at the
     * specified location, a {@link NullValueException} is thrown.
     * @see {@link Map#get(Object)}
     * @throws NullValueException when a null value is read or no element exists at the given key
     */
    @Override
    public @NonNull V get(Object key) {
        V value = internalMap.get(key);
        if (value == null) {
            throw new NullValueException(key);
        }
        return value;
    }

    /**
     * @param key of the associated value
     * @return the value in the backing map, regardless of if it's null
     */
    public @Nullable V getNullable(Object key) {
        return internalMap.get(key);
    }

    @Override
    public boolean isEmpty() {
        return internalMap.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return internalMap.keySet();
    }

    @Override
    public V put(K key, V value) {
        if (value == null) {
            throw new NullValueException(key);
        }
        return internalMap.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m instanceof NonNullMap || !m.containsValue(null)) {
            internalMap.putAll(m);
        } else {
            @SuppressWarnings("unchecked")
            Optional<K> key = (Optional<K>) m.entrySet().stream().filter(e -> e.getValue() == null).map(Entry::getKey).findFirst();
            // Key will not be empty because there is a null value in the list
            throw new NullValueException(key.get());
        }
    }

    @Override
    public V remove(Object key) {
        return internalMap.remove(key);
    }

    @Override
    public int size() {
        return internalMap.size();
    }

    @Override
    public Collection<V> values() {
        return internalMap.values();
    }

    public static class NullValueException extends RuntimeException {
        public NullValueException(Object key) {
            super("Read null value from NonNullMap reading key \""+key.toString()+"\"");
        }
    }

}
