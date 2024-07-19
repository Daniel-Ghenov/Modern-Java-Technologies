package bg.sofia.uni.fmi.mjt.photoalbum.image;

import java.awt.image.BufferedImage;

public class Image {
    private String name;
    private BufferedImage data;

    public Image(String name, BufferedImage data) {
        this.name = name;
        this.data = data;
    }

    public String getExtension() {
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return name.substring(dotIndex + 1);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BufferedImage getData() {
        return data;
    }

    public void setData(BufferedImage data) {
        this.data = data;
    }
}