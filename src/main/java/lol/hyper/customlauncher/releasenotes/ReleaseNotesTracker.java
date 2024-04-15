/*
 * This file is part of CustomLauncherRewrite.
 *
 * CustomLauncherRewrite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CustomLauncherRewrite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CustomLauncherRewrite.  If not, see <https://www.gnu.org/licenses/>.
 */

package lol.hyper.customlauncher.releasenotes;

import lol.hyper.customlauncher.tools.JSONUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class ReleaseNotesTracker {

    /**
     * The GameUpdateTracker logger.
     */
    private final Logger logger = LogManager.getLogger(this);
    /**
     * Stores the updates. Sorted in reverse order, so it's newest -> oldest.
     */
    private final SortedSet<GameUpdate> allGameUpdates = new TreeSet<>(Comparator.comparingInt(GameUpdate::id).reversed());

    /**
     * Download all updates. This is only called if the file isn't there.
     */
    public void getAllReleaseNotes() {
        logger.info("Fetching game updates...");
        JSONArray updateList = JSONUtils.requestJSONArray("https://www.toontownrewritten.com/api/releasenotes/");
        if (updateList == null) {
            logger.warn("Unable to fetch game updates! API returned null on response.");
            return;
        }
        for (int i = 0; i < updateList.length(); i++) {
            JSONObject update = updateList.getJSONObject(i);
            int id = update.getInt("noteId");
            String version = update.getString("slug");
            String date = update.getString("date");
            GameUpdate newUpdate = new GameUpdate(id, version, date);
            allGameUpdates.add(newUpdate);
        }
        logger.info("Found " + updateList.length() + " total game updates");
    }

    /**
     * Get all game updates that were fetched from TTR.
     *
     * @return The set containing the updates.
     */
    public SortedSet<GameUpdate> getAllGameUpdates() {
        return allGameUpdates;
    }
}
