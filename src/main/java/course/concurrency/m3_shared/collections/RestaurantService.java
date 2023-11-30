package course.concurrency.m3_shared.collections;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

public class RestaurantService {

    private Map<String, Restaurant> restaurantMap = new ConcurrentHashMap<>() {{
        put("A", new Restaurant("A"));
        put("B", new Restaurant("B"));
        put("C", new Restaurant("C"));
    }};

    private Map<String, LongAdder> stat = new HashMap<>() {{
        put("A", new LongAdder());
        put("B", new LongAdder());
        put("C", new LongAdder());
    }};;

    public Restaurant getByName(String restaurantName) {
        addToStat(restaurantName);
        return restaurantMap.get(restaurantName);
    }

    public void addToStat(String restaurantName) {
        stat.get(restaurantName).increment();
    }

    public Set<String> printStat() {
        return stat.entrySet().stream().map(entry -> entry.getKey() + " - " + entry.getValue().sum()).collect(Collectors.toSet());
    }
}
