package lol.hyper.customlauncher.invasiontracker;

import lol.hyper.customlauncher.accounts.JSONManager;
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

        invasionTracker.logger.info("Reading " + INVASION_URL + " for current invasions...");

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
                Invasion newInvasion =
                        new Invasion(cogType, cogsDefeated, cogsTotal, district, megaInvasion);
                newInvasion.endTime =
                        Instant.parse(temp.getString("EstimatedCompletion"))
                                .atZone(ZoneId.systemDefault());
                invasionTracker.invasions.put(district, newInvasion);
                invasionTracker.showNotification(newInvasion, true);
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
            String key = pair.getKey() + "/" + pair.getValue().getCogType();
            if (!invasionsJSON.has(key)) {
                invasionTracker.showNotification(pair.getValue(), false);
                it.remove();
            }
        }

        invasionTracker.calls++;
    }
}
