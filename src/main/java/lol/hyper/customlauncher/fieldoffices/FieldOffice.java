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

import org.jetbrains.annotations.NotNull;

public class FieldOffice implements Comparable<FieldOffice> {

    private final int area;
    private final int difficulty;
    private boolean open;
    private int totalAnnexes;

    /**
     * Creates a new field office object with information.
     *
     * @param area The zone ID of where the field office is.
     * @param difficulty The difficulty of the field office.
     * @param totalAnnexes How many total annexes the field office has.
     */
    public FieldOffice(int area, int difficulty, int totalAnnexes) {
        this.area = area;
        this.difficulty = difficulty;
        this.totalAnnexes = totalAnnexes;
    }

    /**
     * Is the field office open or closed?
     *
     * @return "Open" if open, "Closed" if closed.
     */
    public String status() {
        return open ? "Open" : "Closed";
    }

    /**
     * Get the zone ID of the field office.
     *
     * @return The zone ID.
     */
    public int getArea() {
        return area;
    }

    /**
     * Get the difficulty of the field office.
     *
     * @return The difficulty.
     */
    public int getDifficulty() {
        return difficulty;
    }

    /**
     * Get how many total annexes are left for the field office.
     *
     * @return Total annexes.
     */
    public int getTotalAnnexes() {
        return totalAnnexes;
    }

    /**
     * Set if the field office is open or not.
     *
     * @param isOpen Is the field office open?
     */
    public void setOpen(boolean isOpen) {
        open = isOpen;
    }

    /**
     * Set how many total annexes are left of the field office.
     *
     * @param totalAnnexes New total of annexes.
     */
    public void setTotalAnnexes(int totalAnnexes) {
        this.totalAnnexes = totalAnnexes;
    }

    @Override
    public int compareTo(@NotNull FieldOffice fieldOffice) {
        return Integer.compare(this.area, fieldOffice.area);
    }

    @Override
    public String toString() {
        return FieldOfficeTracker.zonesToStreets.get(area);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof FieldOffice fieldOffice)) {
            return false;
        }

        return fieldOffice.getArea() == this.getArea();
    }
}
