import api from "@/api.js";

const createBoardPath = `api/boards`;
const getBoardPath = (boardId) => `api/boards/${boardId}`;
const deleteBoardPath = (boardId) => `api/boards/${boardId}`;
const getBoardMockRulesPath = (boardId) => `api/boards/${boardId}/mocks`;
const createBoardMockRulePath = (boardId) => `api/boards/${boardId}/mocks`;
const updateBoardMockRulePath = (boardId, mockRuleId) => `api/boards/${boardId}/mocks/${mockRuleId}`;
const deleteBoardMockRulePath = (boardId, mockRuleId) => `api/boards/${boardId}/mocks/${mockRuleId}`;
const getBoardWebhooksPath = (boardId) => `api/boards/${boardId}/webhooks`;

const BoardService = {
    getBoard(boardId, ownerToken) {
        return api.get(getBoardPath(boardId), {
            headers: {
                ...api.attachOwnerHeader(ownerToken).headers,
                'Content-Type': 'application/json'
            }
        })
    },
    createBoard() {
        return api.post(createBoardPath, {})
    },
    deleteBoard(boardId, ownerToken) {
        return api.delete(deleteBoardPath(boardId), {
            headers: {
                ...api.attachOwnerHeader(ownerToken).headers,
                'Content-Type': 'application/json'
            }
        });
    },
    getMockRules(boardId, ownerToken) {
        return api.get(getBoardMockRulesPath(boardId), {
            headers: {
                ...api.attachOwnerHeader(ownerToken).headers,
                'Content-Type': 'application/json'
            }
        })
    },
    createMockRule(boardId, ownerToken, mockRuleData) {
        return api.post(createBoardMockRulePath(boardId), mockRuleData, {
            headers: {
                ...api.attachOwnerHeader(ownerToken).headers,
                'Content-Type': 'application/json'
            }
        });
    },
    updateMockRule(boardId, ownerToken, mockRuleId, mockRuleData) {
        return api.put(updateBoardMockRulePath(boardId, mockRuleId), mockRuleData, {
            headers: {
                ...api.attachOwnerHeader(ownerToken).headers,
                'Content-Type': 'application/json'
            }
        })
    },
    deleteMockRule(boardId, ownerToken, mockRuleId) {
        return api.delete(deleteBoardMockRulePath(boardId, mockRuleId), {
            headers: {
                ...api.attachOwnerHeader(ownerToken).headers,
                'Content-Type': 'application/json'
            }
        });
    },

    getWebhooks(boardId, ownerToken) {
        return api.get(getBoardWebhooksPath(boardId), {
            headers: {
                ...api.attachOwnerHeader(ownerToken).headers,
                'Content-Type': 'application/json'
            }
        })
    }
}

export default BoardService;