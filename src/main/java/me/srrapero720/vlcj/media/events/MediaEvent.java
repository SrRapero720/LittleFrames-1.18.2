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

package me.srrapero720.vlcj.media.events;

import me.srrapero720.vlcj.binding.internal.libvlc_instance_t;
import me.srrapero720.vlcj.media.Media;
import me.srrapero720.vlcj.media.MediaEventListener;
import me.srrapero720.vlcj.support.eventmanager.BaseEvent;

/**
 * Base implementation for media events.
 * <p>
 * Every instance of an event refers to an associated media component.
 */
abstract class MediaEvent extends BaseEvent<Media, MediaEventListener> {

    /**
     * Create a media list event.
     *
     * @param libvlcInstance native library instance
     * @param media media that the event relates to
     */
    protected MediaEvent(libvlc_instance_t libvlcInstance, Media media) {
        super(libvlcInstance, media);
    }

}
