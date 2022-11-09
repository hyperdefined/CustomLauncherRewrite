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

    private final String districtName;
    private int population;

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
}
