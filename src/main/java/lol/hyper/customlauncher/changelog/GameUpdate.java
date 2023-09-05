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

/**
 * @param id      The ID of the update.
 * @param version The version of the update.
 * @param notes   The update changelog.
 * @param date    The date of the update.
 */
public record GameUpdate(int id, String version, String notes, String date) implements Comparable<GameUpdate> {

    /**
     * Creates a game update.
     *
     * @param id      The update ID.
     * @param version The update version.
     * @param notes   The update release notes.
     * @param date    The update date.
     */
    public GameUpdate {
    }

    /**
     * Get the ID of the update.
     *
     * @return The number ID.
     */
    @Override
    public int id() {
        return id;
    }

    /**
     * Get the release notes of the update.
     *
     * @return The notes formatted.
     */
    @Override
    public String notes() {
        return notes;
    }

    /**
     * Get the version of the update.
     *
     * @return The version of the update.
     */
    @Override
    public String version() {
        return version;
    }

    /**
     * Get the date of the update.
     *
     * @return The date of the update.
     */
    @Override
    public String date() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof GameUpdate gameUpdate)) {
            return false;
        }

        return gameUpdate.version.equals(this.version);
    }

    @Override
    public int compareTo(GameUpdate other) {
        return Integer.compare(this.id, other.id());
    }

    @Override
    public String toString() {
        return version;
    }

    @Override
    public int hashCode() {
        return id * version.hashCode() * notes.hashCode();
    }
}
