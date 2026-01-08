import {defineStore} from 'pinia'

export const useBoardStore = defineStore("boardStore", {
    state: () => ({
        board: null,
        mockRules: []
    }),
    getters: {
        getBoard() {
            return this.board
        },
        getMockRules() {
            return this.mockRules
        }
    },
    actions: {
        initializeBoardStore(boardModel) {
            console.log(`Initializing boardStore for ${boardModel.id}`);
            this.board = boardModel;
            this.board.updateLastInteraction()
        },

        clearBoardStore() {
            this.board = null
            localStorage.clear()
        }
    }
})
