/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009-2022 Caprica Software Limited.
 */

package me.srrapero720.waterframes.vlcj.player.list.events;

import me.srrapero720.waterframes.vlcj.binding.internal.libvlc_instance_t;
import me.srrapero720.waterframes.vlcj.player.list.MediaListPlayer;
import me.srrapero720.waterframes.vlcj.player.list.MediaListPlayerEventListener;
import me.srrapero720.waterframes.vlcj.support.eventmanager.BaseEvent;

/**
 * Base implementation for media list player events.
 * <p>
 * Every instance of an event refers to an associated media list player.
 */
abstract class MediaListPlayerEvent extends BaseEvent<MediaListPlayer, MediaListPlayerEventListener> {

    /**
     * Create a media player event.
     *
     * @param libvlcInstance native library instance
     * @param mediaListPlayer media player that the event relates to
     */
    protected MediaListPlayerEvent(libvlc_instance_t libvlcInstance, MediaListPlayer mediaListPlayer) {
        super(libvlcInstance, mediaListPlayer);
    }

}
