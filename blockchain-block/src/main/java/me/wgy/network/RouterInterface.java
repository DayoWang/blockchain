package me.wgy.network;

/**
 * Interface for objects that determine the next hop for a message to be routed to in the
 * peer-to-peer network. Given the identifier of a peer, the router object should be able to return
 * information regarding the peer node to which the message should be forwarded next.
 *
 * @author Nadeem Abdul Hamid
 **/
public interface RouterInterface {

  PeerInfo route(String peerid);
}
