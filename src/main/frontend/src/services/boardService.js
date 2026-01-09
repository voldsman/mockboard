import api from "@/api.js";

const getBoardPath = (boardId) => `api/boards/${boardId}`;
const createBoardPath = `api/boards`;
const getBoardMockRulesPath = (boardId) => `api/boards/${boardId}/mocks`;
const createBoardMockRulePath = (boardId) => `api/boards/${boardId}/mocks`;

const BoardService = {
    getBoard(boardId, ownerToken) {
        return api.get(getBoardPath(boardId), {
            ...api.attachOwnerHeader(ownerToken),
        })
    },
    createBoard() {
        return api.post(createBoardPath, {})
    },
    getMockRules(boardId, ownerToken) {
        return api.get(getBoardMockRulesPath(boardId), {
            ...api.attachOwnerHeader(ownerToken),
        })
    },
    createMockRule(boardId, ownerToken, mockRuleData) {
        return api.post(createBoardMockRulePath(boardId), mockRuleData, {
            headers: {
                ...api.attachOwnerHeader(ownerToken).headers,
                'Content-Type': 'application/json'
            }
        });
    }
}

export default BoardService;