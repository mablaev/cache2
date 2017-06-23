import junit.framework.TestCase;


/**
 * JUnit test case for a CacheMap implementation.
 * <p/>
 * Feel free to add more methods.
 */
public class CacheMapTest extends TestCase {
    private CacheMap<Integer, String> cache;
    final static long TIME_TO_LIVE = 1000;

    final static long START_TIME = 1000L;
    final static long TM_500_AFTER_START = START_TIME + 500L;
    final static long TM_1300_AFTER_START = START_TIME + 1300L;
    final static long TM_2000_AFTER_START = START_TIME + 2000L;


    public void setUp() throws Exception {
        Clock.setTime(START_TIME);

        //TODO instantiate cache object
        cache = new CacheMapImpl<>();

        cache.setTimeToLive(TIME_TO_LIVE);
    }

    public void testExpiry() throws Exception {
        cache.put(1, "apple");
        assertEquals("apple", cache.get(1));
        assertFalse(cache.isEmpty());

        Clock.setTime(TM_2000_AFTER_START);

        assertNull(cache.get(1));
        assertTrue(cache.isEmpty());
    }

    public void testSize() throws Exception {
        assertEquals(0, cache.size());
        cache.put(1, "apple");
        assertEquals(1, cache.size());

        Clock.setTime(TM_2000_AFTER_START);

        assertEquals(0, cache.size());
    }

    public void testPartialExpiry() throws Exception {
        //Add an apple, it will expire at 2000
        cache.put(1, "apple");

        Clock.setTime(TM_500_AFTER_START);

        cache.put(2, "orange");

        assertEquals("apple", cache.get(1));
        assertEquals("orange", cache.get(2));
        assertEquals(2, cache.size());

        //Set time to 2300 and check that only the apple has disappeared
        Clock.setTime(TM_1300_AFTER_START);

        assertNull(cache.get(1));
        assertEquals("orange", cache.get(2));
        assertEquals(1, cache.size());
    }

    public void testPutReturnValue() {
        assertEquals(0, cache.size());

        cache.put(1, "apple");
        assertNotNull(cache.put(1, "banana"));

        Clock.setTime(TM_2000_AFTER_START);

        assertNull(cache.put(1, "mango"));
    }

    public void testRemove() throws Exception {
        assertNull(cache.remove(new Integer(1)));

        cache.put(new Integer(1), "apple");

        assertEquals("apple", cache.remove(new Integer(1)));

        assertNull(cache.get(new Integer(1)));
        assertEquals(0, cache.size());
    }

    public void testRemoveExpired() throws Exception {
        assertEquals(0, cache.size());

        assertNull(cache.remove(new Integer(1)));

        cache.put(new Integer(1), "apple");

        Clock.setTime(TM_2000_AFTER_START);

        assertNull(cache.remove(new Integer(1)));
    }

    public void testReplace() throws Exception {
        cache.put(1, "apple");
        cache.put(2, "orange");

        Clock.setTime(TM_500_AFTER_START);

        cache.put(1, "orange");

        Clock.setTime(TM_1300_AFTER_START);

        assertEquals(1, cache.size());
    }

    public void testContainsKeyAndContainsValueNonExpired() {

        assertFalse(cache.containsKey(1));
        assertFalse(cache.containsValue("apple"));
        assertFalse(cache.containsKey(2));
        assertFalse(cache.containsValue("orange"));

        cache.put(1, "apple");

        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsValue("apple"));
        assertFalse(cache.containsKey(2));
        assertFalse(cache.containsValue("orange"));
    }

    public void testContainsKeyAndContainsValueExpired() {

        cache.put(1, "apple");

        assertTrue(cache.containsKey(1));
        assertTrue(cache.containsValue("apple"));
        assertFalse(cache.containsKey(2));
        assertFalse(cache.containsValue("orange"));

        Clock.setTime(TM_2000_AFTER_START);

        assertFalse(cache.containsKey(1));
        assertFalse(cache.containsValue("apple"));
        assertFalse(cache.containsKey(2));
        assertFalse(cache.containsValue("orange"));
    }
}
