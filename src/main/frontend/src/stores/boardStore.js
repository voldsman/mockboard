import {defineStore} from 'pinia'
import boardService from "@/services/boardService.js";
import {BoardModel} from "@/models/boardModel.js";
import constants from "@/constants.js";
import {MockRuleModel} from "@/models/mockRuleModel.js";
import {WebhookModel} from "@/models/webhookModel.js";

export const useBoardStore = defineStore("boardStore", {
    state: () => ({
        board: null,
        mockRules: [],
        webhooks: [],
    }),
    getters: {
        hasActiveSession: (state) => !!state.board && !!state.board.id && !!state.board.ownerToken,
        mockUsageCount: (state) => {
            return `${state.mockRules.length} / ${constants.MAX_MOCKS}`;
        },
        canAddMoreMocks: (state) => {
            return state.mockRules.length < constants.MAX_MOCKS
        },
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

        async deleteBoardById() {
            try {
                const result = await boardService.deleteBoard(this.board.id, this.board.ownerToken)
                return result.status === 204;
            } catch (err) {
                console.error(`Failed to delete board ${mockRuleId}`, err)
                throw err
            }
        },

        async fetchMockRules() {
            try {
                const result = await boardService.getMockRules(this.board.id, this.board.ownerToken)
                this.mockRules = result.data.map(mr => new MockRuleModel(mr))
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
        },

        async updateMockRuleById(mockRuleId, mockRuleData) {
            try {
                const result = await boardService.updateMockRule(this.board.id,
                    this.board.ownerToken,
                    mockRuleId,
                    mockRuleData
                )

                if (result.status === 200 && result.data.id) {
                    const index = this.mockRules.findIndex(r => r.id === mockRuleId)
                    if (index !== -1) {
                        this.mockRules[index] = new MockRuleModel({
                            ...mockRuleData,
                            id: mockRuleId
                        });
                    }

                    return result;
                }
                console.warn(`Received unexpected status: ${result.status}`, result.data);
                // for later: handle errors
                // throw new Error(`Unexpected response status: ${result.status}`);
            } catch (err) {
                console.error("Failed to update mock rule", err)
                throw err
            }
        },

        async deleteMockRuleById(mockRuleId) {
            try {
                const result = await boardService.deleteMockRule(this.board.id, this.board.ownerToken, mockRuleId)
                if (result.status === 204) {
                    this.mockRules = this.mockRules.filter(rule => rule.id !== mockRuleId)
                    return
                }
                console.warn(`Received unexpected status code: ${result.status}`)
            } catch (err) {
                console.error(`Failed to delete mock rule ${mockRuleId}`, err)
                throw err
            }
        },

        async fetchWebhooks() {
            try {
                const result = await boardService.getWebhooks(this.board.id, this.board.ownerToken)
                this.webhooks = result.data.map(mr => new WebhookModel(mr))
            } catch (err) {
                console.error("Failed to fetch webhooks", err)
            }
        },

        async processReceivedWebhook(webhookData) {
            const existingIndex = this.webhooks.findIndex(w => w.id === webhookData.id);
            if (existingIndex !== -1) {
                this.webhooks.splice(existingIndex, 1);
            }

            this.webhooks.unshift(webhookData)
            if (this.webhooks.length > constants.MAX_WEBHOOKS) {
                this.webhooks.splice(constants.MAX_WEBHOOKS);
            }
        }
    }
})
