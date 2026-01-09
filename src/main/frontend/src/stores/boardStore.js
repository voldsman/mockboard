import {defineStore} from 'pinia'
import boardService from "@/services/boardService.js";
import {BoardModel} from "@/models/boardModel.js";
import constants from "@/constants.js";
import {MockRuleModel} from "@/models/mockRuleModel.js";

export const useBoardStore = defineStore("boardStore", {
    state: () => ({
        board: null,
        mockRules: []
    }),
    getters: {
        hasActiveSession: (state) => !!state.board && !!state.board.id && !!state.board.ownerToken,
    },
    actions: {
        clearBoardStore() {
            this.board = null
            this.mockRules = []
            localStorage.removeItem(constants.BOARD_DATA)
        },
        async restoreSession() {
            const localModel = BoardModel.fromLS(constants.BOARD_DATA)
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

        async fetchMockRules() {
            try {
                const result = await boardService.getMockRules(this.board.id, this.board.ownerToken);
                this.mockRules = result.data.map(mr => new MockRuleModel(mr));
                // todo: use flag like: this.rulesLoaded = true
            } catch (err) {
                console.error("Failed to fetch mock rules", err)
            }
        },

        async createNewMockRule(mockRuleData) {
            try {
                const result = await boardService.createMockRule(this.board.id, this.board.ownerToken, mockRuleData)

                const mockId = result.data.id;
                const newMockRule = new MockRuleModel({
                    ...mockRuleData,
                    id: mockId
                })
                this.mockRules.unshift(newMockRule)
                return newMockRule
            } catch (err) {
                console.error("Failed to create mock rule", err)
                throw err
            }
        }
    }
})
