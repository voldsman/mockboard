export class MockRuleModel {
    constructor(data = {}) {
        this.id = data.id || null;
        this.boardId = data.boardId || null;
        this.method = data.method || null;
        this.path = data.path || null;
        this.headers = data.headers || null;
        this.body = data.body || null;
        this.statusCode = data.statusCode || null;
        this.delay = data.delay || null;
        this.timestamp = data.timestamp || null;
    }
}