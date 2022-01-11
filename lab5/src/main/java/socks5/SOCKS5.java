package socks5;

public class SOCKS5 {
    public static final byte SOCKS5_VERSION = 0x05;

    public static final byte MIN_AUTH_NUMBER = 0x01;
    public static final byte NO_AUTH = 0x00;

    public static final byte IPV4_ADDRESS = 0x01;
    public static final byte DOMAIN_NAME = 0x03;

    public static final byte TCP_STREAM_CONNECTION = 0x01;

    public static final byte RESERVED = 0x00;

    public static final byte REQUEST_GRANTED = 0x00;

    public static final int MAX_GREETING_MESSAGE_LENGTH = 1 + 1 + 255;
    public static final int MAX_CONNECTION_REQUEST_MESSAGE_LENGTH = 1 + 1 + 1 + 1 + 256 + 2;
}
