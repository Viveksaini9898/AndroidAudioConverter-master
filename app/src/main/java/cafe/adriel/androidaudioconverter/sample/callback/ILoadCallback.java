package cafe.adriel.androidaudioconverter.sample.callback;

public interface ILoadCallback {
    
    void onSuccess();
    
    void onFailure(Exception error);
    
}