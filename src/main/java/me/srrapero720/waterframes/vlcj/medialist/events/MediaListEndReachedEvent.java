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

package me.srrapero720.waterframes.vlcj.medialist.events;

import me.srrapero720.waterframes.vlcj.binding.internal.libvlc_instance_t;
import me.srrapero720.waterframes.vlcj.medialist.MediaList;
import me.srrapero720.waterframes.vlcj.medialist.MediaListEventListener;

/**
 * Encapsulation of a media list end reached event.
 */
final class MediaListEndReachedEvent extends MediaListEvent {

    /**
     * Create a media list event.
     *
     * @param libvlcInstance native library instance
     * @param mediaList media list the event relates to
     */
    MediaListEndReachedEvent(libvlc_instance_t libvlcInstance, MediaList mediaList) {
        super(libvlcInstance, mediaList);
    }

    @Override
    public void notify(MediaListEventListener listener) {
        listener.mediaListEndReached(component);
    }

}
