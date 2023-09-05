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

package lol.hyper.customlauncher.invasions;

import java.time.ZonedDateTime;

public class Invasion implements Comparable<Invasion> {

    /**
     * What cog the invasion is.
     */
    private final String cogType;
    /**
     * How many cogs the invasion is total.
     */
    private final int cogsTotal;
    /**
     * The district of the invasion.
     */
    private final String district;
    /**
     * The end time reported by the API.
     */
    public ZonedDateTime endTime;
    /**
     * The current cog defeated count.
     */
    private int cogsDefeated;
    /**
     * Is the invasion a mega invasion?
     */
    private final boolean megaInvasion;
    /**
     * Stores when we started to track this invasion locally.
     */
    private final long cacheStartTime;

    /**
     * Creates a new invasion.
     *
     * @param district     The district.
     * @param cogType      The cog type.
     * @param cogsTotal    How many cogs total.
     * @param megaInvasion Is it a mega invasion?
     */
    public Invasion(String district, String cogType, int cogsTotal, boolean megaInvasion) {
        this.cogType = cogType;
        this.district = district;
        this.cogsTotal = cogsTotal;
        this.megaInvasion = megaInvasion;
        this.cacheStartTime = System.nanoTime();
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

    /**
     * Updates the cogs defeated.
     *
     * @param newAmount The amount.
     */
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

    @Override
    public int hashCode() {
        return district.hashCode();
    }

    /**
     * Get when we started tracking the invasion.
     *
     * @return The timestamp.
     */
    public long getCacheStartTime() {
        return cacheStartTime;
    }

    /**
     * Is the invasion a mega invasion?
     *
     * @return Yes/No
     */
    public boolean isMegaInvasion() {
        return megaInvasion;
    }
}
