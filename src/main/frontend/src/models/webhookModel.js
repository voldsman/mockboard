export class WebhookModel {
    constructor(data = {}) {
        this.id = data.id || null;
        this.boardId = data.boardId || null;
        this.method = data.method || null;
        this.path = data.path || null;
        this.fullUrl = data.fullUrl || null;
        this.queryParams = data.queryParams || null;
        this.headers = data.headers || null;
        this.body = data.body || null;
        this.contentType = data.contentType || null;
        this.statusCode = data.statusCode || null;
        this.matched = data.matched || null;
        this.timestamp = data.timestamp || null;
        this.processingTimeMs = data.processingTimeMs || null;
    }
}