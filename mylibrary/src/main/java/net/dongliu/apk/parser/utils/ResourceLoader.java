package net.dongliu.apk.parser.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * methods for load resources.
 *
 * @author dongliu
 */
public class ResourceLoader {


    /**
     * load system attr ids for parse binary xml.
     */
    public static Map<Integer, String> loadSystemAttrIds() {
        try (final BufferedReader reader = ResourceLoader.toReader("/r_values.ini")) {
            final Map<Integer, String> map = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] items = line.trim().split("=");
                if (items.length != 2) {
                    continue;
                }
                final String name = items[0].trim();
                final Integer id = Integer.valueOf(items[1].trim());
                map.put(id, name);
            }
            return map;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<Integer, String> loadSystemStyles() {
        final Map<Integer, String> map = new HashMap<>();
        try (final BufferedReader reader = ResourceLoader.toReader("/r_styles.ini")) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                final String[] items = line.split("=");
                if (items.length != 2) {
                    continue;
                }
                final Integer id = Integer.valueOf(items[1].trim());
                final String name = items[0].trim();
                map.put(id, name);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }


    private static BufferedReader toReader(final String path) {
        return new BufferedReader(new InputStreamReader(
                ResourceLoader.class.getResourceAsStream(path)));
    }
}
