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

package lol.hyper.customlauncher.districts;

import lol.hyper.customlauncher.accounts.JSONManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;

public class DistrictTask implements ActionListener {

    final String DISTRICT_URL = "https://www.toontownrewritten.com/api/population";
    private final DistrictTracker districtTracker;
    private final Logger logger = LogManager.getLogger(this);

    public DistrictTask(DistrictTracker districtTracker) {
        this.districtTracker = districtTracker;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // grab the invasions object in the request
        // that hold all the invasions
        JSONObject districtsJSON = JSONManager.requestJSON(DISTRICT_URL);
        if (districtsJSON == null) {
            districtTracker.isDown = true;
            districtTracker.districtTaskTimer.stop();
            return;
        }

        districtTracker.isDown = false;

        logger.info("Reading " + DISTRICT_URL + " for current districts...");

        JSONObject districts = districtsJSON.getJSONObject("populationByDistrict");
        // iterate through each district
        Iterator<String> keys = districts.keys();
        while (keys.hasNext()) {
            String districtFromJSON = keys.next();
            // if we do not have that district stored, create a new district object
            // and add it to the list
            if (!districtTracker.districts.containsKey(districtFromJSON)) {
                int population = districts.getInt(districtFromJSON);
                District district = new District(districtFromJSON);
                district.setPopulation(population);
                districtTracker.districts.put(districtFromJSON, district);
            } else {
                if (!districtTracker.districts.containsKey(districtFromJSON)) {
                    return; // JUST IN CASE
                }
                // if we already have it saved, update the information that we have saved already
                // we want to update the total cogs defeated and the end time
                District tempDistrict = districtTracker.districts.get(districtFromJSON);
                int population = districts.getInt(districtFromJSON);
                tempDistrict.setPopulation(population);
            }
        }

        // we look at the current districts list and see if any districts
        // are not on the districts JSON (aka that district is gone)
        Iterator<Map.Entry<String, District>> it = districtTracker.districts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, District> pair = it.next();
            String key = pair.getKey();
            if (!districts.has(key)) {
                it.remove();
            }
        }
        districtTracker.lastFetched = System.currentTimeMillis();
    }
}
