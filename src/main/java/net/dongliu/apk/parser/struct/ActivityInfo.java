package net.dongliu.apk.parser.struct;

/**
 * Activity constants.
 * see
 * http://developer.android.com/reference/android/content/pm/ActivityInfo.html
 * http://developer.android.com/guide/topics/manifest/activity-element.html
 *
 * @author dongliu
 */
public class ActivityInfo {
    public static enum ScreenOrienTation {
        behind(0x00000003),
        fullSensor(0x0000000a),
        fullUser(0x0000000d),
        landscape(0x00000000),
        locked(0x0000000e),
        nosensor(0x00000005),
        portrait(0x00000001),
        reverseLandscape(0x00000008),
        reversePortrait(0x00000009),
        sensor(0x00000004),
        sensorLandscape(0x00000006),
        sensorPortrait(0x00000007),
        unspecified(0xffffffff),
        user(0x00000002),
        userLandscape(0x0000000b),
        userPortrait(0x0000000c);

        private int value;

        private ScreenOrienTation(int value) {
            this.value = value;
        }

        public static ScreenOrienTation valueOf(int value) {
            for (ScreenOrienTation s : ScreenOrienTation.values()) {
                if (s.value == value) {
                    return s;
                }
            }
            return null;
        }
    }
}
