package nc.univ.tpfinal.tchat.server;

import nc.univ.tpfinal.tchat.ITchat;

import javafx.application.Platform;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * Processus serveur qui ecoute les connexion entrantes,
 * les messages entrant et les rediffuse au clients connectes
 *
 * @author mathieu.fabre
 */
public class Server extends Thread implements ITchat {

    /**
     * Interface graphique du serveur
     */

    // TODO A completer
    private ServerUI serverUI;
    private String ip;
    private int port;
    private ServerSocketChannel ssc;
    private Selector selector;
    private ByteBuffer buffer;

    /**
     * Constructeur
     * Lien avec l interface Graphique
     * Creation du selecteur et du socket serveur
     *
     * @param serverUI
     */
    public Server(ServerUI serverUI, String ip, int port) {
        this.serverUI = serverUI;
        this.ip = ip;
        this.port = port;
    }

    /**
     * Envoi un message de log a l'IHM
     */
    public void sendLogToUI(String message) {
        Platform.runLater(() -> serverUI.log(message));
    }

    /**
     * Process principal du server
     */
    public void run() {

        // TODO A completer
        try {
            buffer = ByteBuffer.allocate(ITchat.BUFFER_SIZE);
            ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress(ip, port));
            selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException ioe) {
        }

        while (ssc.isOpen()) {
            try {
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isAcceptable()) {
                        try (SocketChannel client = ssc.accept();) {
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ);
                            key.interestOps(SelectionKey.OP_ACCEPT);
                        } catch (Exception e) {
                        }
                    } else if (key.isReadable()) {
                        try (SocketChannel client = (SocketChannel) key.channel();) {
                            client.read(buffer);
                            broadcast(buffer);
                            key.interestOps(SelectionKey.OP_READ);
                        } catch (Exception e) {
                        }
                    }
                    keyIterator.remove();
                }
            } catch (IOException ioe) {
            }
        }
    }

    private void broadcast(ByteBuffer buff) {
        try {
            sendLogToUI(StandardCharsets.UTF_8.decode(buff).toString());
            for (SelectionKey key : selector.keys()) {
                if (key.isValid() && key.channel() instanceof SocketChannel) {
                    SocketChannel sChannel = (SocketChannel) key.channel();
                    sChannel.write(buff);
                    buff.rewind();
                }
            }
            buff.clear();
        } catch (IOException ioe) {
        }
    }
}
// TODO A completer
