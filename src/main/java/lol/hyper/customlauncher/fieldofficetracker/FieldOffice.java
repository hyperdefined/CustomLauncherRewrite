package lol.hyper.customlauncher.fieldofficetracker;

import org.jetbrains.annotations.NotNull;

public class FieldOffice implements Comparable<FieldOffice> {

    private final int area;
    private final int difficulty;
    private boolean open;
    private int totalAnnexes;

    /**
     * Creates a new field office object with information.
     * @param area The zone ID of where the field office is.
     * @param difficulty The difficulty of the field office.
     * @param open If the field office's doors are open.
     * @param totalAnnexes How many total annexes the field office has.
     */
    public FieldOffice(int area, int difficulty, boolean open, int totalAnnexes) {
        this.area = area;
        this.difficulty = difficulty;
        this.open = open;
        this.totalAnnexes = totalAnnexes;
    }

    /**
     * Is the field office open or closed?
     * @return "Open" if open, "Closed" if closed.
     */
    public String status() {
        return open ? "Open" : "Closed";
    }

    /**
     * Get the zone ID of the field office.
     * @return The zone ID.
     */
    public int getArea() {
        return area;
    }

    /**
     * Get the difficulty of the field office.
     * @return The difficulty.
     */
    public int getDifficulty() {
        return difficulty;
    }

    /**
     * Get how many total annexes are left for the field office.
     * @return Total annexes.
     */
    public int getTotalAnnexes() {
        return totalAnnexes;
    }

    /**
     * Set if the field office is open or not.
     * @param isOpen Is the field office open?
     */
    public void setOpen(boolean isOpen) {
        open = isOpen;
    }

    /**
     * Set how many total annexes are left of the field office.
     * @param totalAnnexes New total of annexes.
     */
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
