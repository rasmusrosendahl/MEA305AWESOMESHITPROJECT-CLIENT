package Network;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import mainGame.PlayerStats;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

public class Network extends Listener {

    Client client;
    int port = 23820;
    ServerData serverData;
    InetAddress ip;

    public static Boolean isConnected = false;
    public static ArrayList<String> serverListOfTypes;
    public static ArrayList<Integer> serverListOfYieldNumbers;

    public void connect() {
        client = new Client(16384,2048);
        client.getKryo().register(PlayerStats.class);
        client.getKryo().register(int[].class);
        client.getKryo().register(ServerData.class);
        client.getKryo().register(ClientData.class);
        client.getKryo().register(boolean[].class);
        client.getKryo().register(String[].class);

        client.addListener(this);

        ip = client.discoverHost(port,port);
        client.start();
        try {
            client.connect(5000, ip, port, port);
            isConnected = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void received(Connection c, Object o) {
        if (o instanceof PacketAddPlayer) {
            PacketAddPlayer packet = (PacketAddPlayer) o;
            PlayerStats newPlayerStats = new PlayerStats();
            GameClient.players.put(packet.ID, newPlayerStats);
        }
        if (o instanceof PacketRemovePlayer) {
            PacketRemovePlayer packet = (PacketRemovePlayer) o;
            GameClient.players.remove(packet.ID);
        }
        if (o instanceof ServerData){
            serverData = (ServerData) o;
            serverData.unpack();
            //PlayerStats.names = receivedPacket.names;
            //PlayerStats.lobbyReadyAll = receivedPacket.lobbyReadyAll;
            for (int i =0; i<PlayerStats.names.length; i++){
                //System.out.println(PlayerStats.names[i]);
                System.out.println(PlayerStats.ID);
            }
        }
        if (o instanceof ServerData)
        {
            serverData = (ServerData) o;
            serverListOfTypes = serverData.listOfTileTypes;
            serverListOfYieldNumbers = serverData.listOfYieldNumbers;
        }
    }
}
