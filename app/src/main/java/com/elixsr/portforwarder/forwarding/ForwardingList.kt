package com.elixsr.portforwarder.forwarding

import java.util.*

class ForwardingList() {
    var list: LinkedList<Forwarder> = LinkedList();
    fun addForwarder(forwarder: Forwarder): Unit {
        list.add(forwarder)
    }
}