package lol.hyper.customlauncher.fieldofficetracker;

import org.jetbrains.annotations.NotNull;

public class FieldOffice implements Comparable<FieldOffice> {

    private final int area;
    private final int difficulty;
    private boolean open;
    private int totalAnnexes;

    public FieldOffice(int area, int difficulty, boolean open, int totalAnnexes) {
        this.area = area;
        this.difficulty = difficulty;
        this.open = open;
        this.totalAnnexes = totalAnnexes;
    }

    public String isOpen() {
        return open ? "Yes" : "No";
    }

    public int getArea() {
        return area;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getTotalAnnexes() {
        return totalAnnexes;
    }

    public void setOpen(boolean isOpen) {
        open = isOpen;
    }

    public void setTotalAnnexes(int totalAnnexes) {
        this.totalAnnexes = totalAnnexes;
    }

    @Override
    public int compareTo(@NotNull FieldOffice fieldOffice) {
        return (FieldOfficeTracker.zonesToStreets
                .get(area)
                .compareTo(FieldOfficeTracker.zonesToStreets.get(fieldOffice.getArea())));
    }

    @Override
    public String toString() {
        return FieldOfficeTracker.zonesToStreets.get(area);
    }
}
