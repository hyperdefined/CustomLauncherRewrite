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

package lol.hyper.customlauncher.fieldoffices;

import lol.hyper.customlauncher.accounts.JSONManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;

public class FieldOfficeTask implements ActionListener {

    final String FIELD_OFFICE_URL = "https://www.toontownrewritten.com/api/fieldoffices";
    private final FieldOfficeTracker fieldOfficeTracker;
    private final Logger logger = LogManager.getLogger(this);

    /**
     * Start tracking field offices. This will read the API, store any new ones, and update any
     * saved ones.
     *
     * @param fieldOfficeTracker The tracker that will process this task.
     */
    public FieldOfficeTask(FieldOfficeTracker fieldOfficeTracker) {
        this.fieldOfficeTracker = fieldOfficeTracker;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Read the API and store whatever JSON is received
        JSONObject fieldOfficeRoot = JSONManager.requestJSON(FIELD_OFFICE_URL);
        if (fieldOfficeRoot == null) {
            fieldOfficeTracker.isDown = true;
            fieldOfficeTracker.fieldOfficeTaskTimer.stop();
            return;
        }

        fieldOfficeTracker.isDown = false;

        // each field office is under the fieldOffices JSON
        JSONObject fieldOfficeJSON = fieldOfficeRoot.getJSONObject("fieldOffices");

        logger.info("Reading " + FIELD_OFFICE_URL + " for current field offices...");

        // go through all the field offices from the API
        Iterator<String> keys = fieldOfficeJSON.keys();
        while (keys.hasNext()) {
            // each field office json is named the zone ID
            // so use this to identify the field office
            int fieldOfficeZone = Integer.parseInt(keys.next());
            JSONObject zoneJSON = fieldOfficeJSON.getJSONObject(String.valueOf(fieldOfficeZone));
            // update field office data if we already have it
            if (fieldOfficeTracker.fieldOffices.containsKey(fieldOfficeZone)) {
                FieldOffice office = fieldOfficeTracker.fieldOffices.get(fieldOfficeZone);
                office.setOpen(zoneJSON.getBoolean("open"));
                office.setTotalAnnexes(zoneJSON.getInt("annexes"));
            } else {
                // save the new field office
                int difficulty = zoneJSON.getInt("difficulty") + 1; // they zero index this
                int totalAnnexes = zoneJSON.getInt("annexes");
                boolean open = zoneJSON.getBoolean("open");
                FieldOffice office = new FieldOffice(fieldOfficeZone, difficulty, totalAnnexes);
                office.setOpen(open);
                // add it to our list
                fieldOfficeTracker.fieldOffices.put(fieldOfficeZone, office);
                fieldOfficeTracker.showNotification(office, true);
            }
        }

        // we look at the current field office list and see if any of them
        // are not on the field office JSON (aka that field office is gone)
        Iterator<Map.Entry<Integer, FieldOffice>> it =
                fieldOfficeTracker.fieldOffices.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, FieldOffice> pair = it.next();
            int key = pair.getKey();
            if (!fieldOfficeJSON.has(String.valueOf(key))) {
                fieldOfficeTracker.showNotification(pair.getValue(), false);
                it.remove();
            }
        }
        fieldOfficeTracker.runs++;
        fieldOfficeTracker.lastFetched = System.currentTimeMillis();
    }
}
