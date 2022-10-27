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

import java.time.ZonedDateTime;

public class Invasion implements Comparable<Invasion> {

    private final String cogType;
    private final int cogsTotal;
    private final String district;
    public ZonedDateTime endTime;
    private int cogsDefeated;
    public final boolean megaInvasion;

    public Invasion(String district, String cogType, int cogsTotal, boolean megaInvasion) {
        this.cogType = cogType;
        this.district = district;
        this.cogsTotal = cogsTotal;
        this.megaInvasion = megaInvasion;
    }

    /**
     * Get how many cogs total the invasion has.
     *
     * @return Total cogs for invasion.
     */
    public int getCogsTotal() {
        return cogsTotal;
    }

    /**
     * Get how many cogs were defeated so far.
     *
     * @return Cogs defeated.
     */
    public int getCogsDefeated() {
        return cogsDefeated;
    }

    /**
     * Get the cog type.
     *
     * @return Cog type.
     */
    public String getCogType() {
        return cogType;
    }

    /**
     * Get the district of the invasion.
     *
     * @return The district.
     */
    public String getDistrict() {
        return district;
    }

    /** Update the amount of cogs defeated. */
    public void updateCogsDefeated(int newAmount) {
        cogsDefeated = newAmount;
    }

    @Override
    public String toString() {
        return district + " - " + cogType;
    }

    @Override
    public int compareTo(Invasion invasion) {
        return (this.getDistrict().compareTo(invasion.getDistrict()));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Invasion invasion)) {
            return false;
        }

        return invasion.getDistrict().equals(this.getDistrict());
    }
}
