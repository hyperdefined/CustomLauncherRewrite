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

package lol.hyper.customlauncher.changelog;

import lol.hyper.customlauncher.tools.JSONUtils;
import lol.hyper.customlauncher.tools.PopUpWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

public class GameUpdateTracker {

    /**
     * The GameUpdateTracker logger.
     */
    private final Logger logger = LogManager.getLogger(this);
    /**
     * Stores the updates. Sorted in reverse order, so it's newest -> oldest.
     */
    public final SortedSet<GameUpdate> allGameUpdates = new TreeSet<>(Comparator.comparingInt(GameUpdate::id).reversed());
    /**
     * The file for saving updates.
     */
    private final File savedUpdatesFile = new File("config", "savedUpdates.json");

    /**
     * Creates a GameUpdateTracker instance.
     */
    public GameUpdateTracker() {
        if (!savedUpdatesFile.exists()) {
            new PopUpWindow(null, "I am going to fetch release note information. This will take a bit.");
            getAllNotes();
        } else {
            // we have notes saved, see if we need to update it
            JSONObject savedUpdatesJSON = new JSONObject(JSONUtils.readFile(savedUpdatesFile));
            // store which IDs we have saved
            Set<Integer> savedUpdatesList = new HashSet<>();
            for (String updateID : savedUpdatesJSON.keySet()) {
                savedUpdatesList.add(Integer.valueOf(updateID));
            }
            JSONArray updateList = fetchUpdates();
            if (updateList == null) {
                logger.warn("Unable to fetch game updates! API returned null on response.");
                return;
            }
            // compare the remote list to our list
            Set<Integer> newUpdates = new HashSet<>();
            for (JSONObject update : getUpdates(updateList)) {
                int id = update.getInt("noteId");
                if (!savedUpdatesList.contains(id)) {
                    newUpdates.add(id);
                }
            }
            // there's an update on the list we don't have
            if (!newUpdates.isEmpty()) {
                logger.info("Adding new updates: " + newUpdates);
                for (int updateID : newUpdates) {
                    JSONObject updateNotesJSON = JSONUtils.requestJSON("https://www.toontownrewritten.com/api/releasenotes/" + updateID);
                    if (updateNotesJSON == null) {
                        logger.warn(updateID + " returned null response from " + "https://www.toontownrewritten.com/api/releasenotes/" + updateID);
                        continue;
                    }
                    JSONObject newUpdate = new JSONObject();
                    newUpdate.put("version", updateNotesJSON.getString("slug"));
                    newUpdate.put("date", updateNotesJSON.getString("date"));
                    newUpdate.put("notes", updateNotesJSON.getString("body"));
                    savedUpdatesJSON.put(String.valueOf(updateID), newUpdate);
                }
                JSONUtils.writeFile(savedUpdatesJSON, savedUpdatesFile);
            }

            // add all updates to the list
            for (String updateID : savedUpdatesJSON.keySet()) {
                JSONObject update = savedUpdatesJSON.getJSONObject(updateID);
                GameUpdate gameUpdate = new GameUpdate(Integer.parseInt(updateID), update.getString("version"), update.getString("notes"), update.getString("date"));
                allGameUpdates.add(gameUpdate);
            }
        }
    }

    /**
     * Download all updates. This is only called if the file isn't there.
     */
    private void getAllNotes() {
        logger.info("Fetching game updates...");
        JSONArray updateList = fetchUpdates();
        if (updateList == null) {
            logger.warn("Unable to fetch game updates! API returned null on response.");
            return;
        }
        for (JSONObject update : getUpdates(updateList)) {
            int id = update.getInt("noteId");
            String version = update.getString("slug");
            String date = update.getString("date");
            String notes;
            JSONObject updateNotesJSON = JSONUtils.requestJSON("https://www.toontownrewritten.com/api/releasenotes/" + id);
            if (updateNotesJSON == null) {
                notes = null;
            } else {
                notes = updateNotesJSON.getString("body");
            }
            GameUpdate newUpdate = new GameUpdate(id, version, notes, date);
            allGameUpdates.add(newUpdate);
        }

        JSONObject savedUpdatesJSON = new JSONObject();
        for (GameUpdate gameUpdate : allGameUpdates) {
            JSONObject gameUpdateJSON = new JSONObject();
            gameUpdateJSON.put("version", gameUpdate.version());
            gameUpdateJSON.put("date", gameUpdate.date());
            gameUpdateJSON.put("notes", gameUpdate.notes());
            savedUpdatesJSON.put(String.valueOf(gameUpdate.id()), gameUpdateJSON);
        }
        JSONUtils.writeFile(savedUpdatesJSON, savedUpdatesFile);
    }

    /**
     * This will read TTR's API into a JSONArray.
     *
     * @return The JSONArray containing all updates.
     */
    private JSONArray fetchUpdates() {
        JSONArray updateList = JSONUtils.requestJSONArray("https://www.toontownrewritten.com/api/releasenotes");
        if (updateList == null) {
            logger.warn("Unable to fetch game updates! API returned null on response.");
            return null;
        }
        return updateList;
    }

    /**
     * Get all individual updates from TTR.
     *
     * @param updateArray The array from TTR's api.
     * @return A Set that contains all updates as JSONObjects.
     */
    private Set<JSONObject> getUpdates(JSONArray updateArray) {
        Set<JSONObject> updates = new HashSet<>();
        for (int i = 0; i < updateArray.length(); i++) {
            JSONObject update = updateArray.getJSONObject(i);
            updates.add(update);
        }
        return updates;
    }
}
