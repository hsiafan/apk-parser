package net.dongliu.apk.parser.bean;

/**
 * Icon path, and density
 */
public class IconPath {
    private final String path;
    private final int density;

    public IconPath(final String path, final int density) {
        this.path = path;
        this.density = density;
    }

    /**
     * The icon path in apk file
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Return the density this icon for. 0 means default icon.
     * see {@link net.dongliu.apk.parser.struct.resource.Densities} for more density values.
     */
    public int getDensity() {
        return this.density;
    }

    @Override
    public String toString() {
        return "IconPath{" +
                "path='" + this.path + '\'' +
                ", density=" + this.density +
                '}';
    }
}
