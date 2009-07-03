/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.storageprovider;

import java.util.Map;
import java.util.TreeMap;

import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.ItemVisitor;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.SubscriberVisitor;
import org.apache.vysper.xmpp.modules.extension.xep0060_pubsub.model.PayloadItem;
import org.apache.vysper.xmpp.xmlfragment.XMLElement;

/**
 * This storage provider keeps all objects in memory and looses its content when
 * removed from memory. This is the default storage provider for leaf nodes.
 * 
 * @author The Apache MINA Project (http://mina.apache.org)
 */
public class LeafNodeInMemoryStorageProvider implements LeafNodeStorageProvider {

    // stores subscribers, access via subid
    protected Map<String, Entity> subscribers;
    // stores messages, access via itemid
    protected Map<String, PayloadItem> messages;

    /**
     * Initialize the storage maps.
     */
    public LeafNodeInMemoryStorageProvider() {
        this.subscribers = new TreeMap<String, Entity>();
        this.messages = new TreeMap<String, PayloadItem>();
    }

    /**
     * Add a subscriber with given subID.
     */
    public void addSubscriber(String nodeName, String subscriptionID, Entity subscriber) {
        subscribers.put(subscriptionID, subscriber);
    }

    /**
     * Check if a subscriber is already known.
     */
    public boolean containsSubscriber(String nodeName, Entity subscriber) {
        return subscribers.containsValue(subscriber);
    }

    /**
     * Check if a subscriptionId is already known.
     */
    public boolean containsSubscriber(String nodeName, String subscriptionId) {
        return subscribers.containsKey(subscriptionId);
    }

    /**
     * Retrieve a subscriber via its subsriptionId.
     */
    public Entity getSubscriber(String nodeName, String subscriptionId) {
        return subscribers.get(subscriptionId);
    }

    /**
     * Remove a subscriber via its subscriptionId.
     */
    public boolean removeSubscription(String nodeName, String subscriptionId) {
        return subscribers.remove(subscriptionId) != null;
    }

    /**
     * Remove a subscriber via its JID. This removes all subscriptions of the JID.
     */
    public boolean removeSubscriber(String nodeName, Entity subscriber) {
        return subscribers.values().remove(subscriber);
    }

    /**
     * Count how often a given subscriber is subscribed.
     */
    public int countSubscriptions(String nodeName, Entity subscriber) {
        int count = 0;
        for(Entity sub : subscribers.values()) {
            if(subscriber.equals(sub)) {
                ++count;
            }
        }
        return count;
    }

    /**
     * Count how many subscriptions this node has.
     */
    public int countSubscriptions(String nodeName) {
        return subscribers.size();
    }

    /**
     * Add a message to the storage.
     */
    public void addMessage(String nodeName, String itemID, XMLElement payload) {
        messages.put(itemID, new PayloadItem(payload, itemID));
    }

    /**
     * Accept method (see visitor pattern) to visit all subscribers of this node.
     */
    public void acceptForEachSubscriber(String nodeName, SubscriberVisitor subscriberVisitor) {
        for(Entity sub : subscribers.values()) {
            subscriberVisitor.visit(nodeName, sub);
        }
    }

    /**
     * The in-memory storage provider does not need initialization beyond creating the objects.
     */
    public void initialize() {
        // empty
    }

    public void acceptForEachItem(String nodeName, ItemVisitor iv) {
        for(String itemID : messages.keySet()) {
            iv.visit(itemID, messages.get(itemID));
        }
    }
}