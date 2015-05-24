package model;

public class IpAddress {

    private String host;
    private int port;
    private boolean isEmpty;

    public IpAddress() {
        isEmpty = true;
    }

    public IpAddress(String host, int port) {
        this.host = host;
        this.port = port;
        isEmpty = false;
    }

    public IpAddress(String address) throws Exception {
        String[] data = address.split(":");
        host = data[0];
        port = Integer.parseInt(data[1]);
        isEmpty = false;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isEmpty() {
        return isEmpty;
    }
}
