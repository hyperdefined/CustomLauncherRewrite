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

import org.json.JSONObject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;

public class FieldOfficeTask implements ActionListener {
    private final FieldOfficeTracker fieldOfficeTracker;

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
        fieldOfficeTracker.isDown = false;

        // each field office is under the fieldOffices JSON
        JSONObject rootJSON = new JSONObject(fieldOfficeTracker.result);
        JSONObject fieldOfficeJSON = rootJSON.getJSONObject("fieldOffices");

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
