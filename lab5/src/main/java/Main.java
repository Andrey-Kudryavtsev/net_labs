import logger.MyLogger;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            MyLogger.getLogger().error("Not enough arguments");
            return;
        }
        SocksProxyServer server = new SocksProxyServer(Integer.parseInt(args[0]));
        server.start();
    }
}
