/*
 * Licensed under Apache License, Version 2.0 or LGPL 2.1, at your option.
 * --
 *
 * Copyright 2010 Rene Treffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * --
 *
 * Copyright (C) 2010 Rene Treffer
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package com.googlecode.asmack.contacts;

import org.w3c.dom.Node;

import com.googlecode.asmack.Stanza;
import com.googlecode.asmack.XMLUtils;
import com.googlecode.asmack.XMPPUtils;
import com.googlecode.asmack.XmppMalformedException;
import com.googlecode.asmack.connection.StanzaListener;
import com.googlecode.asmack.contacts.StatusUpdate.Presence;

/**
 * Stanza Broadcast Receiver listening for xmpp &lt;presence/&gt; updates.
 */
public class PresenceBroadcastReceiver implements StanzaListener {

    /**
     * The data mapper used to load/save contacts and metadata.
     */
    private final ContactDataMapper mapper;

    /**
     * Create a new presence broadcast receiver with a given data mapper
     * backend.
     * @param mapper The data mapper backend of this broadcast receiver.
     */
    public PresenceBroadcastReceiver(ContactDataMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Receive a single stanza intent, check for xmpp &lt;presence/&gt; and
     * store it to the database.
     * @param Context The current application context.
     * @param intent The event Intent.
     */
    @Override
    public void receive(Stanza stanza) {
        if (!"presence".equals(stanza.getName())) {
            return;
        }
        String accountJid = XMPPUtils.getBareJid(stanza.getVia());
        String jid = XMPPUtils.getBareJid(stanza.getAttribute("from").getValue());
        StatusUpdate update = null;
        if (stanza.getAttribute("type") != null) {
            if ("unavailable".equals(stanza.getAttribute("type").getValue())) {
                update = mapper.getStatusUpdate(accountJid, jid);
                update.setPresence(Presence.OFFLINE);
                mapper.persist(update);
            }
        }
        try {
            update = mapper.getStatusUpdate(accountJid, jid);
            update.setPresence(Presence.AVAILABLE);
            Node node = stanza.getDocumentNode();
            Node show = XMLUtils.getFirstChild(node, null, "show");
            if (show != null) {
                String presence = show.getTextContent();
                if ("away".equals(presence)) {
                    update.setPresence(Presence.AWAY);
                }
                if ("dnd".equals(presence)) {
                    update.setPresence(Presence.DO_NOT_DISTURB);
                }
            }
            Node status = XMLUtils.getFirstChild(node, null, "status");
            if (status != null) {
                update.setStatus(status.getTextContent());
            }
            mapper.persist(update);
        } catch (XmppMalformedException e) {
            e.printStackTrace();
        }
    }

}
