import {defineStore} from 'pinia'
import boardService from "@/services/boardService.js";
import {BoardModel} from "@/models/boardModel.js";
import constants from "@/constants.js";

export const useBoardStore = defineStore("boardStore", {
    state: () => ({
        board: null,
        mockRules: []
    }),
    getters: {
        hasActiveSession: (state) => !!state.board && !!state.board.id && !!state.board.ownerToken,
        getBoard() {
            return this.board
        },
        getMockRules() {
            return this.mockRules
        }
    },
    actions: {
        clearBoardStore() {
            this.board = null
            this.mockRules = []
            localStorage.clear()
        },
        async restoreSession() {
            const localModel = BoardModel.fromLS(constants.BOARD_DATA)
            console.log('localModel', localModel)
            if (!localModel || !localModel.id || !localModel.ownerToken || localModel.isExpired()) {
                this.clearBoardStore()
                return null
            }

            try {
                const result = await boardService.getBoard(localModel.id, localModel.ownerToken)
                const serverModel = new BoardModel(result.data)
                serverModel.lastInteraction = localModel.lastInteraction
                return serverModel
            } catch (err) {
                console.warn("Session expired on server or invalid data", err)
                this.clearBoardStore()
                return null
            }
        },

        setBoard(boardModel) {
            this.board = boardModel
            this.board.updateLastInteraction()
            console.log(`Initialized boardStore for ${boardModel.id}`);
        },

        async createNewBoard() {
            try {
                const result = await boardService.createBoard()
                const newBoard = new BoardModel(result.data)
                this.setBoard(newBoard)
                return {
                    status: result.status,
                    data: newBoard
                }
            } catch (err) {
                console.error("Failed to create board", err)
                const status = err.response?.status || 500
                const message = err.response?.data?.message || 'Server Error'
                throw { status, message }
            }
        },

        async fetchMockRules(boardId, ownerToken) {

        }
    }
})
