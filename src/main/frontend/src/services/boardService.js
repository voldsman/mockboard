import api, {setOwnerToken} from "@/api.js";

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
        setOwnerToken(ownerToken)
        return api.get(getBoardPath(boardId))
    },
    createBoard() {
        return api.post(createBoardPath, {})
    },
    deleteBoard(boardId, ownerToken) {
        setOwnerToken(ownerToken)
        return api.delete(deleteBoardPath(boardId));
    },
    getMockRules(boardId, ownerToken) {
        setOwnerToken(ownerToken)
        return api.get(getBoardMockRulesPath(boardId))
    },
    createMockRule(boardId, ownerToken, mockRuleData) {
        setOwnerToken(ownerToken)
        return api.post(createBoardMockRulePath(boardId), mockRuleData);
    },
    updateMockRule(boardId, ownerToken, mockRuleId, mockRuleData) {
        setOwnerToken(ownerToken)
        return api.put(updateBoardMockRulePath(boardId, mockRuleId), mockRuleData)
    },
    deleteMockRule(boardId, ownerToken, mockRuleId) {
        setOwnerToken(ownerToken)
        return api.delete(deleteBoardMockRulePath(boardId, mockRuleId));
    },

    getWebhooks(boardId, ownerToken) {
        setOwnerToken(ownerToken)
        return api.get(getBoardWebhooksPath(boardId))
    }
}

export default BoardService;