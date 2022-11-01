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

package lol.hyper.customlauncher.invasiontracker;

import lol.hyper.customlauncher.accounts.JSONManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.Map;

public class InvasionTask implements ActionListener {

    final String INVASION_URL = "https://api.toon.plus/invasions";
    private final InvasionTracker invasionTracker;
    private final Logger logger = LogManager.getLogger(this);

    public InvasionTask(InvasionTracker invasionTracker) {
        this.invasionTracker = invasionTracker;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // grab the invasions object in the request
        // that hold all the invasions
        JSONObject invasionsJSON = JSONManager.requestJSON(INVASION_URL);
        if (invasionsJSON == null) {
            invasionTracker.isDown = true;
            invasionTracker.invasionTaskTimer.stop();
            return;
        }
        invasionTracker.isDown = false;

        logger.info("Reading " + INVASION_URL + " for current invasions...");

        // iterate through each of the invasions (separate JSONs)
        Iterator<String> keys = invasionsJSON.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String district = key.substring(0, key.indexOf('/'));
            // if we do not have that invasion stored, create a new invasion object
            // and add it to the list
            if (!invasionTracker.invasions.containsKey(district)) {
                JSONObject temp = invasionsJSON.getJSONObject(key);
                String cogType = temp.getString("Type");
                int cogsDefeated = temp.getInt("CurrentProgress");
                int cogsTotal = temp.getInt("MaxProgress");
                boolean megaInvasion = temp.getBoolean("MegaInvasion");
                Invasion newInvasion = new Invasion(district, cogType, cogsTotal, megaInvasion);
                newInvasion.updateCogsDefeated(cogsDefeated);
                newInvasion.endTime =
                        Instant.parse(temp.getString("EstimatedCompletion"))
                                .atZone(ZoneId.systemDefault());
                invasionTracker.invasions.put(district, newInvasion);
                invasionTracker.showNotification(newInvasion, true);
                logger.info(
                        "Tracking new invasion for "
                                + district
                                + ". Cogs: "
                                + cogsDefeated
                                + "/"
                                + cogsTotal
                                + ". ETA: "
                                + newInvasion.endTime);
            } else {
                if (!invasionTracker.invasions.containsKey(district)) {
                    return; // JUST IN CASE
                }
                // if we already have it saved, update the information that we have saved already
                // we want to update the total cogs defeated and the end time
                Invasion tempInv = invasionTracker.invasions.get(district);
                JSONObject temp = invasionsJSON.getJSONObject(key);
                // ignore mega invasion cog count
                if (!temp.getBoolean("MegaInvasion")) {
                    int cogsDefeated = temp.getInt("CurrentProgress");
                    logger.info(
                            "Updating invasion details for "
                                    + district
                                    + ". Cogs: "
                                    + tempInv.getCogsDefeated()
                                    + " -> "
                                    + cogsDefeated
                                    + ". ETA: "
                                    + tempInv.endTime);
                    tempInv.updateCogsDefeated(cogsDefeated);
                    tempInv.endTime =
                            Instant.parse(temp.getString("EstimatedCompletion"))
                                    .atZone(ZoneId.systemDefault());
                }
            }
        }

        // we look at the current invasion list and see if any invasions
        // are not on the invasion JSON (aka that invasion is gone)
        Iterator<Map.Entry<String, Invasion>> it = invasionTracker.invasions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Invasion> pair = it.next();
            String cogType = pair.getValue().getCogType();
            String district = pair.getKey();
            // <district>/<cog name>
            String key = district + "/" + cogType;
            if (!invasionsJSON.has(key)) {
                invasionTracker.showNotification(pair.getValue(), false);
                String savedDuration = (System.nanoTime() - pair.getValue().getCacheStartTime())/ 1000000000 + " seconds.";
                it.remove();
                logger.info("Removing saved invasion for " + district + ". Tracked for " + savedDuration);
            }
        }
        invasionTracker.runs++;
        invasionTracker.lastFetched = System.currentTimeMillis();
    }
}
