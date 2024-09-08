import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.flink.api.common.state.MapState;

public class MapStateImpl<K, V> implements MapState<K, V> {

    private Hashtable<K, V> hashtable;

    public MapStateImpl() {
        hashtable = new Hashtable<>();
    }

    @Override
    public V get(K key) {
        return hashtable.get(key);
    }

    @Override
    public void put(K key, V value) {
        hashtable.put(key, value);
    }

    @Override
    public void putAll(Map<K, V> map) {
        hashtable.putAll(map);
    }

    @Override
    public void remove(K key) {
        hashtable.remove(key);
    }

    @Override
    public boolean contains(K key) {
        return hashtable.containsKey(key);
    }

    @Override
    public Iterable<Map.Entry<K, V>> entries() {
        return hashtable.entrySet();
    }

    @Override
    public Iterable<K> keys() {
        List<K> keysList = Collections.list(hashtable.keys());
        return keysList;
    }

    @Override
    public Iterable<V> values() {
        return hashtable.values();
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return hashtable.entrySet().iterator();
    }

    @Override
    public boolean isEmpty() {
        return hashtable.isEmpty();
    }

    @Override
    public void clear() {
        hashtable.clear();
    }
}
