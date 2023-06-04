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

import org.json.JSONObject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

public class DistrictTask implements ActionListener {
    private final DistrictTracker districtTracker;

    /**
     * Start tracking districts. This will read the API and update each population of all districts.
     *
     * @param districtTracker The tracker that will process this task.
     */
    public DistrictTask(DistrictTracker districtTracker) {
        this.districtTracker = districtTracker;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        districtTracker.isDown = false; // make sure to set this to false since we can read the API

        JSONObject rootJSON = new JSONObject(districtTracker.result);
        JSONObject populationByDistrict = rootJSON.getJSONObject("populationByDistrict");
        // iterate through each district
        Iterator<String> populationKeys = populationByDistrict.keys();
        while (populationKeys.hasNext()) {
            String districtFromJSON = populationKeys.next();
            // if we do not have that district stored, create a new district object
            // and add it to the list
            if (!districtTracker.districts.containsKey(districtFromJSON)) {
                int population = populationByDistrict.getInt(districtFromJSON);
                District district = new District(districtFromJSON);
                district.setPopulation(population);
                districtTracker.districts.put(districtFromJSON, district);
            } else {
                if (!districtTracker.districts.containsKey(districtFromJSON)) {
                    return; // JUST IN CASE
                }
                // if we already have it saved, update the population
                District tempDistrict = districtTracker.districts.get(districtFromJSON);
                int population = populationByDistrict.getInt(districtFromJSON);
                tempDistrict.setPopulation(population);
            }
        }

        JSONObject statusByDistrict = rootJSON.getJSONObject("statusByDistrict");
        // iterate through each district
        Iterator<String> statusKeys = statusByDistrict.keys();
        while (statusKeys.hasNext()) {
            String districtFromJSON = statusKeys.next();
            // only update the status of districts we track
            if (districtTracker.districts.containsKey(districtFromJSON)) {
                String status = statusByDistrict.getString(districtFromJSON);
                District tempDistrict = districtTracker.districts.get(districtFromJSON);
                tempDistrict.setCurrentStatus(status);
            }
        }
        districtTracker.lastFetched = System.currentTimeMillis();
    }
}
