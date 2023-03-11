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

package me.srrapero720.waterframes.vlcj.player.base.events;

import me.srrapero720.waterframes.vlcj.binding.internal.libvlc_event_t;
import me.srrapero720.waterframes.vlcj.binding.internal.media_player_program_selection_changed;
import me.srrapero720.waterframes.vlcj.player.base.MediaPlayer;
import me.srrapero720.waterframes.vlcj.player.base.MediaPlayerEventListener;

/**
 * Encapsulation of a media player elementary stream updated event.
 */
final class MediaPlayerProgramSelectedEvent extends MediaPlayerEvent {

    private final int unselectedId;

    private final int selectedId;

    MediaPlayerProgramSelectedEvent(MediaPlayer mediaPlayer, libvlc_event_t event) {
        super(mediaPlayer);

        this.unselectedId = ((media_player_program_selection_changed) event.u.getTypedValue(media_player_program_selection_changed.class)).i_unselected_id;
        this.selectedId = ((media_player_program_selection_changed) event.u.getTypedValue(media_player_program_selection_changed.class)).i_selected_id;
    }

    @Override
    public void notify(MediaPlayerEventListener listener) {
        listener.programSelected(mediaPlayer, unselectedId, selectedId);
    }

}