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

package me.srrapero720.waterframes.vlcj.media.events;

import me.srrapero720.waterframes.vlcj.binding.internal.libvlc_event_t;
import me.srrapero720.waterframes.vlcj.binding.internal.libvlc_instance_t;
import me.srrapero720.waterframes.vlcj.binding.internal.libvlc_picture_list_t;
import me.srrapero720.waterframes.vlcj.binding.internal.libvlc_picture_t;
import me.srrapero720.waterframes.vlcj.binding.internal.media_attached_thumbnails_found;
import me.srrapero720.waterframes.vlcj.binding.support.types.size_t;
import me.srrapero720.waterframes.vlcj.media.Media;
import me.srrapero720.waterframes.vlcj.media.MediaEventListener;
import me.srrapero720.waterframes.vlcj.media.Picture;

import java.util.ArrayList;
import java.util.List;

import static me.srrapero720.waterframes.vlcj.binding.lib.LibVlc.libvlc_picture_list_at;
import static me.srrapero720.waterframes.vlcj.binding.lib.LibVlc.libvlc_picture_list_count;

/**
 * Encapsulation of a media attached thumbnails found event.
 */
final class MediaAttachedThumbnailsFoundEvent extends MediaEvent {

    private final libvlc_picture_list_t thumbnails;

    /**
     * Create a media event.
     *
     * @param libvlcInstance native library instance
     * @param media component the event relates to
     * @param event native event
     */
    MediaAttachedThumbnailsFoundEvent(libvlc_instance_t libvlcInstance, Media media, libvlc_event_t event) {
        super(libvlcInstance, media);
        this.thumbnails = ((media_attached_thumbnails_found) event.u.getTypedValue(media_attached_thumbnails_found.class)).thumbnails;
    }

    @Override
    public void notify(MediaEventListener listener) {
        int count = libvlc_picture_list_count(thumbnails).intValue();
        List<Picture> pictures = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            libvlc_picture_t picture = libvlc_picture_list_at(thumbnails, new size_t(i));
            pictures.add(new Picture(picture));
        }
        listener.mediaAttachedThumbnailsFound(component, pictures);
    }
}
