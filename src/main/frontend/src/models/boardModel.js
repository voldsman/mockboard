import constants from "@/constants.js";

export class BoardModel {
    constructor(data = {}) {
        // server data
        this.id = data.id || null
        this.ownerToken = data.ownerToken || null
        this.timestamp = data.timestamp ? new Date(data.timestamp).getTime() : null
        // local properties
        this.lastInteraction = data.lastInteraction || null
    }

    toJSON() {
        return {
            id: this.id,
            ownerToken: this.ownerToken,
            timestamp: this.timestamp,
            lastInteraction: this.lastInteraction,
        }
    }

    toLS() {
        const stringified = JSON.stringify(this.toJSON());
        localStorage.setItem(constants.BOARD_DATA, stringified)
    }

    updateLastInteraction() {
        this.lastInteraction = Date.now();
        this.toLS();
    }

    shouldShowOverlay() {
        if (!this.lastInteraction) return true;

        const now = Date.now();
        const ttl = constants.REASK_OVERLAY_TTL;
        return (now - this.lastInteraction) > ttl;
    }

    isExpired() {
        if (!this.timestamp || this.timestamp === 0) return true;

        const created = new Date(this.timestamp);
        if (isNaN(created.getTime())) return true;

        const now = new Date();

        const currentUTCHour = now.getUTCHours();
        const currentUTCDate = new Date(Date.UTC(
            now.getUTCFullYear(),
            now.getUTCMonth(),
            now.getUTCDate(),
            3, 0, 0
        ));
        const lastWipeTime = currentUTCHour >= 3
            ? currentUTCDate
            : new Date(currentUTCDate.getTime() - 24 * 60 * 60 * 1000);
        return created.getTime() < lastWipeTime.getTime();
    }

    static fromLS(key) {
        const raw = localStorage.getItem(key)
        if (!raw) return null
        try {
            const data = JSON.parse(raw)
            const board = new BoardModel(data)
            if (!board.id || !board.ownerToken) {
                console.warn('Invalid board data in localStorage')
                return null
            }

            return board
        } catch (error) {
            console.error('Failed to parse board data:', error)
            return null
        }
    }
}