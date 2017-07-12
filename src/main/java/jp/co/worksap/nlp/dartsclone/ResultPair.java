package jp.co.worksap.nlp.dartsclone;

public class ResultPair {

    public int value;
    public int length;

    public ResultPair(int value, int length) {
        this.value = value;
        this.length = length;
    }

    public void set(int value, int length) {
        this.value = value;
        this.length = length;
    }
}
