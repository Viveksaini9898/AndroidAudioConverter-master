package cafe.adriel.androidaudioconverter.sample;

import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import java.io.Serializable;
public class ItemsModel{
    private String name;
    private Uri albumArtUri;
    private Uri songs_uri;
    private String duration;
    int size;
    public ItemsModel(String name, Uri albumArtUri,Uri uri,String duration,int size) {
        this.name = name;
        this.albumArtUri = albumArtUri;
        this.songs_uri=uri;
        this.duration=duration;
        this.size=size;
    }
    public String getDuration() {
        return duration;
    }

    public String getDurationInUIFormat() {
        return getTimeConversionInMinsec(Integer.parseInt(duration));
    }

    private String getTimeConversionInMinsec(int millisec){
        int x =(int) Math.ceil(millisec / 1000f);
        int min = x % 3600 / 60;
        int sec = x % 60;
        return String.format("%02d:%02d", min, sec);
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
    public double getSize() {
        return (double) size;
    }
   /*public void setSize(int size) {
        this.size = size;
    }*/
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
    public Uri getAlbumArtUri() {
        return albumArtUri;
    }
    public void setImages(Uri images) {
        this.albumArtUri = images;
    }
}