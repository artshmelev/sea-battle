package model;

public class Message {

    private String prefix;
    private int[] data;
    private boolean isEmpty;

    public Message() {
        isEmpty = true;
    }

    public Message(String str) {
        prefix = str.substring(0, str.indexOf('_'));

        String[] strData = str.substring(str.indexOf('_')+1).split("_");
        data = new int[strData.length];
        for (int i = 0; i < strData.length; ++i) {
            data[i] = Integer.parseInt(strData[i]);
        }

        isEmpty = false;
    }

    public String getPrefix() {
        return prefix;
    }

    public int[] getData() {
        return data;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void clear() {
        isEmpty = true;
    }
}
