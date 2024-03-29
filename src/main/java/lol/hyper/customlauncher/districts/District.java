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

public class District implements Comparable<District> {

    /**
     * The district name.
     */
    private final String districtName;
    /**
     * The population.
     */
    private int population;
    /**
     * The status of the district.
     */
    private String currentStatus;

    /**
     * Create a district.
     *
     * @param name The district name.
     */
    public District(String name) {
        this.districtName = name;
    }

    /**
     * Get the district name.
     *
     * @return The district name.
     */
    public String getDistrictName() {
        return districtName;
    }

    /**
     * Set the population of this district.
     *
     * @param population The number of toons.
     */
    public void setPopulation(int population) {
        this.population = population;
    }

    /**
     * Get the current population stored locally.
     *
     * @return The number of toons.
     */
    public int getPopulation() {
        return population;
    }

    @Override
    public String toString() {
        return districtName;
    }

    @Override
    public int compareTo(District district) {
        return (this.districtName.compareTo(district.getDistrictName()));
    }

    /**
     * Get the district's statue.
     *
     * @return The status.
     */
    public String getCurrentStatus() {
        return currentStatus;
    }

    /**
     * Set the district's statue.
     *
     * @param status The new status.
     */
    public void setCurrentStatus(String status) {
        currentStatus = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof District district)) {
            return false;
        }

        return district.getDistrictName().equalsIgnoreCase(this.getDistrictName());
    }

    @Override
    public int hashCode() {
        return districtName.hashCode();
    }
}
