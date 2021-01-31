public class StaticWaiter {
    static private Object obj = new Object();

    public static void staticWait(int time) {
        synchronized (obj) {
            try {
                obj.wait(time);
            } catch (Exception e) {
            }
        }
    }

    public static void staticNotify() {
        synchronized (obj) {
            obj.notify();
        }
    }
}
