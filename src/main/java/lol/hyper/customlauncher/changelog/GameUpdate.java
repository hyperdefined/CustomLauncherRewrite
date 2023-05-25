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

package lol.hyper.customlauncher.changelog;

public class GameUpdate implements Comparable<GameUpdate> {

    private final int id;
    private final String version;
    private final String notes;
    private final String date;

    /**
     * Stores information about an update. This has release notes, date, and version.
     *
     * @param id      The update ID.
     * @param version The update version.
     * @param notes   The update release notes.
     * @param date    The update date.
     */
    public GameUpdate(int id, String version, String notes, String date) {
        this.id = id;
        this.version = version;
        this.notes = notes;
        this.date = date;
    }

    /**
     * Get the ID of the update.
     *
     * @return The number ID.
     */
    public int id() {
        return id;
    }

    /**
     * Get the release notes of the update.
     *
     * @return The notes formatted.
     */
    public String notes() {
        return notes;
    }

    /**
     * Get the version of the update.
     *
     * @return The version of the update.
     */
    public String version() {
        return version;
    }

    /**
     * Get the date of the update.
     *
     * @return The date of the update.
     */
    public String date() {
        return date;
    }

    @Override
    public int compareTo(GameUpdate other) {
        return Integer.compare(this.id, other.id());
    }

    @Override
    public String toString() {
        return version;
    }
}
