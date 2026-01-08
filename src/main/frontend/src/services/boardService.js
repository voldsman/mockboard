import api from "@/api.js";

const getBoardPath = (boardId) => `api/boards/${boardId}`;
const createBoardPath = `api/boards`;
const getBoardMockRulesPath = (boardId) => `api/boards/${boardId}/mocks`;

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

}

export default BoardService;