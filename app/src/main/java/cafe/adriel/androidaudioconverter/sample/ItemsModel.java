package cafe.adriel.androidaudioconverter.sample;

import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import java.io.Serializable;
public class ItemsModel{
    private String name;
    private Bitmap images;
    private Uri songs_uri;
    private String duration;
    int size;
    public ItemsModel(String name, Bitmap images,Uri uri,String duration,int size) {
        this.name = name;
        this.images = images;
        this.songs_uri=uri;
        this.duration=duration;
        this.size=size;
    }
    public String getDuration() {
        return duration;
    }
    public void setDuration(String duration) {
        this.duration = duration;
    }
    public int getSize() {
        return size;
    }
    public void setSize(String duration) {
        this.duration = duration;
    }
    public Uri getUri() {
        return songs_uri;
    }
    public void setUri(Uri uri) {
        this.songs_uri = uri;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Bitmap getImages() {
        return images;
    }
    public void setImages(Bitmap images) {
        this.images = images;
    }
}