package nc.univ.tpfinal.tchat.client;

import nc.univ.tpfinal.tchat.ITchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Client de tchat
 */
public class Client extends Thread implements ITchat {

	private ClientUI clientUI;
	private String hostname;
	private int port;
	private String nickname;
	private SocketChannel sc;
	private Selector selector;
	private ByteBuffer buffer;

	/**
	 * Constructeur
	 */
	public Client(ClientUI clientUI, String hostname, int port, String nickname) {
		this.clientUI = clientUI;
		this.hostname = hostname;
		this.port = port;
		this.nickname = nickname;
	}

	/**
	 * Ajoute un message et passe le channel en
	 * mode ecriture
	 */
	public void addMessage(String message) {
		// TODO A completer
		message = nickname + ": " + message;
		buffer.put(message.getBytes());
		try {
			sc.write(buffer);
		} catch (IOException e) {
		}
	}

	/**
	 * Process principal du thread
	 * on ecoute
	 */
	public void run() {

		// TODO A completer
		try {
			buffer = ByteBuffer.allocate(ITchat.BUFFER_SIZE);
			sc = SocketChannel.open();
			sc.configureBlocking(false);
			sc.connect(new InetSocketAddress(hostname, port));
			selector = Selector.open();
			sc.register(selector, SelectionKey.OP_READ);
		} catch (IOException ioe) {
		}

		while (sc.isOpen()) {
			try {
				selector.select(1000);
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
				while (keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();
					if (key.isConnectable()) {
						try (SocketChannel channel = (SocketChannel) key.channel();) {
							channel.configureBlocking(false);
							channel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
						} catch (Exception e) {
						}
					} else if (key.isReadable()) {
						try (SocketChannel client = (SocketChannel) key.channel();) {
							client.read(buffer);
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

}
