package org.apache.vysper.xmpp.server.s2s;
import java.io.File;

import org.apache.vysper.mina.TCPEndpoint;
import org.apache.vysper.storage.StorageProviderRegistry;
import org.apache.vysper.storage.inmemory.MemoryStorageProviderRegistry;
import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.addressing.EntityImpl;
import org.apache.vysper.xmpp.authorization.AccountManagement;
import org.apache.vysper.xmpp.protocol.NamespaceURIs;
import org.apache.vysper.xmpp.server.ServerRuntimeContext;
import org.apache.vysper.xmpp.server.XMPPServer;
import org.apache.vysper.xmpp.stanza.Stanza;
import org.apache.vysper.xmpp.stanza.StanzaBuilder;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;


public class Server2Server {

    public static void main(String[] args) throws Exception {
        Entity localServer = EntityImpl.parseUnchecked(args[0]);
        Entity localUser = EntityImpl.parseUnchecked(args[1]);
        Entity remoteServer = EntityImpl.parseUnchecked(args[2]);
        Entity remoteUser = EntityImpl.parseUnchecked(args[3]);

        String keystorePath;
        String keystorePassword;
        if(args.length > 4) {
            keystorePath = args[4];
            keystorePassword = args[5];
        } else {
            keystorePath = "src/main/config/bogus_mina_tls.cert";
            keystorePassword = "boguspw";            
        }
        
        XMPPServer server = new XMPPServer(localServer.getDomain());

        StorageProviderRegistry providerRegistry = new MemoryStorageProviderRegistry();
        final AccountManagement accountManagement = (AccountManagement) providerRegistry
        .retrieve(AccountManagement.class);

        if (!accountManagement.verifyAccountExists(localUser)) {
            accountManagement.addUser(localUser.getFullQualifiedName(), "password1");
        }

        // S2S endpoint
        TCPEndpoint s2sEndpoint = new TCPEndpoint();
        s2sEndpoint.setPort(5269);
        server.addEndpoint(s2sEndpoint);
        
        // C2S endpoint
        server.addEndpoint(new TCPEndpoint());
        
        server.setStorageProviderRegistry(providerRegistry);
        server.setTLSCertificateInfo(new File(keystorePath), keystorePassword);
        
        server.start();
        
        // enable server connection to use ping
        //server.addModule(new XmppPingModule());

        ServerRuntimeContext serverRuntimeContext = server.getServerRuntimeContext();
        
        Thread.sleep(2000);

//        XMPPServerConnectorRegistry registry = serverRuntimeContext.getServerConnectorRegistry();
//        
//        XMPPServerConnector connector = registry.getConnector(remoteServer);
//        
//        Stanza stanza = new StanzaBuilder("message", NamespaceURIs.JABBER_SERVER)
//            .addAttribute("from", localUser.getFullQualifiedName())
//            .addAttribute("to", remoteUser.getFullQualifiedName())
//            .startInnerElement("body", NamespaceURIs.JABBER_SERVER)
//            .addText("Hello world")
//            .endInnerElement()
//            .build();
//            
//        connector.write(stanza);
        
        
        ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration("localhost", 5222);
        connectionConfiguration.setKeystorePath(keystorePath);
        connectionConfiguration.setTruststorePath(keystorePath);
        connectionConfiguration.setTruststorePassword(keystorePassword);
        XMPPConnection client = new XMPPConnection(connectionConfiguration);

        client.connect();
        client.login(localUser.getNode(), "password1");
        
        Thread.sleep(3000);
        
        //Message msg = new Message(remoteUser.getFullQualifiedName());
        Message msg = new Message("foo@sadfsdgdsfgdfsgfd.com");
        msg.setBody("Hello world");
        
        client.sendPacket(msg);
        
        
        Thread.sleep(8000);
        client.disconnect();
        
        Thread.sleep(50000);
        
        server.stop();
    }
    
}
