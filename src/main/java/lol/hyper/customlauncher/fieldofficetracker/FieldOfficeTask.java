package lol.hyper.customlauncher.fieldofficetracker;

import lol.hyper.customlauncher.accounts.JSONManager;
import org.json.JSONObject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Map;

public class FieldOfficeTask implements ActionListener {

    final String FIELD_OFFICE_URL = "https://www.toontownrewritten.com/api/fieldoffices";

    private final FieldOfficeTracker fieldOfficeTracker;

    public FieldOfficeTask(FieldOfficeTracker fieldOfficeTracker) {
        this.fieldOfficeTracker = fieldOfficeTracker;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // grab the field offices object in the request
        // each field office is stored under the JSONObject "fieldOffices"
        JSONObject fieldOfficeRoot = JSONManager.requestJSON(FIELD_OFFICE_URL);
        if (fieldOfficeRoot == null) {
            fieldOfficeTracker.isDown = true;
            fieldOfficeTracker.fieldOfficeTaskTimer.stop();
            return;
        }

        fieldOfficeTracker.isDown = false;

        JSONObject fieldOfficeJSON = fieldOfficeRoot.getJSONObject("fieldOffices");

        fieldOfficeTracker.logger.info(
                "Reading " + FIELD_OFFICE_URL + " for current field offices...");

        // go through all the field offices from the API
        Iterator<String> keys = fieldOfficeJSON.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            // each field office json is named the zone ID
            JSONObject zoneJSON = fieldOfficeJSON.getJSONObject(key);
            // update field office data
            if (fieldOfficeTracker.fieldOffices.containsKey(Integer.valueOf(key))) {
                FieldOffice office = fieldOfficeTracker.fieldOffices.get(Integer.parseInt(key));
                office.setOpen(zoneJSON.getBoolean("open"));
                office.setTotalAnnexes(zoneJSON.getInt("annexes"));
            } else {
                // new field office
                int difficulty = zoneJSON.getInt("difficulty") + 1; // they zero index this
                int totalAnnexes = zoneJSON.getInt("annexes");
                boolean open = zoneJSON.getBoolean("open");
                FieldOffice office =
                        new FieldOffice(Integer.parseInt(key), difficulty, open, totalAnnexes);
                // add it to our master list
                fieldOfficeTracker.fieldOffices.put(Integer.parseInt(key), office);
                fieldOfficeTracker.showNotification(office, true);
            }
        }

        // we look at the current field office list and see if any of them
        // are not on the field office JSON (aka that field office is gone)
        Iterator<Map.Entry<Integer, FieldOffice>> it =
                fieldOfficeTracker.fieldOffices.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, FieldOffice> pair = it.next();
            String key = String.valueOf(pair.getKey());
            if (!fieldOfficeJSON.has(key)) {
                fieldOfficeTracker.showNotification(pair.getValue(), false);
                it.remove();
            }
        }

        fieldOfficeTracker.calls++;
    }
}
