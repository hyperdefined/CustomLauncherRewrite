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

public class Invasion implements Comparable<Invasion> {

    private final String cogType;
    private final int cogsDefeated;
    private final int cogsTotal;
    private final String district;
    private final Long timeStarted;

    public Invasion(String cogType, int cogsDefeated, int cogsTotal, String district) {
        this.cogType = cogType;
        this.cogsDefeated = cogsDefeated;
        this.cogsTotal = cogsTotal;
        this.district = district;
        this.timeStarted = System.currentTimeMillis();
    }

    /**
     * Get how many cogs total the invasion has.
     * @return Total cogs for invasion.
     */
    public int getCogsTotal() {
        return cogsTotal;
    }

    /**
     * Get how many cogs were defeated so far.
     * @return Cogs defeated.
     */
    public int getCogsDefeated() {
        return cogsDefeated;
    }

    /**
     * Get the district of the invasion.
     * @return The district.
     */
    public String getDistrict() {
        return district;
    }

    /**
     * Gets the start time of the invasion.
     * @return The time formatted as a long.
     */
    public Long getTimeStarted() {
        return timeStarted;
    }

    @Override
    public String toString() {
        return district + " - " + cogType;
    }

    @Override
    public int compareTo(Invasion invasion) {
        return (this.getDistrict().compareTo(invasion.getDistrict()));
    }
}
