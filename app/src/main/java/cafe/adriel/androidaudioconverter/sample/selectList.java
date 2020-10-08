package cafe.adriel.androidaudioconverter.sample;

public class selectList {

    public int getcardImage() {
        return cardImage;
    }

    public void setcardImage(int cardImage) {
        this.cardImage = cardImage;
    }

    public String gettvName() {
        return tvName;
    }

    public void settvName(String tvName) {
        this.tvName = tvName;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int color) {
        this.background = background;
    }


    String tvName;
    int cardImage,background;
    public selectList(int cardImage, String tvName,int background){
        this.cardImage=cardImage;
        this.tvName=tvName;
        this.background=background;

    }
}
